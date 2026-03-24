package com.bitchat.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bitchat.android.flutter.BitchatFlutterChannels
import com.bitchat.android.identity.SecureIdentityStateManager
import com.bitchat.android.mesh.BluetoothMeshService
import com.bitchat.android.onboarding.*
import com.bitchat.android.utils.DeviceUtils
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.pm.ActivityInfo
import androidx.core.content.ContextCompat

class MainActivity : FlutterFragmentActivity() {

    private lateinit var permissionManager: PermissionManager
    private lateinit var onboardingCoordinator: OnboardingCoordinator
    private lateinit var bluetoothStatusManager: BluetoothStatusManager
    private lateinit var locationStatusManager: LocationStatusManager
    private lateinit var batteryOptimizationManager: BatteryOptimizationManager
    
    private lateinit var meshService: BluetoothMeshService
    private lateinit var identityManager: SecureIdentityStateManager
    private val mainViewModel: MainViewModel by viewModels()
    
    private var channels: BitchatFlutterChannels? = null

    private val forceFinishReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
            if (intent.action == com.bitchat.android.util.AppConstants.UI.ACTION_FORCE_FINISH) {
                finishAffinity()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOrientationBasedOnDeviceType()
        
        // Register receiver for force finish signal with proper flags
        val filter = android.content.IntentFilter(com.bitchat.android.util.AppConstants.UI.ACTION_FORCE_FINISH)
        ContextCompat.registerReceiver(
            this, 
            forceFinishReceiver, 
            filter, 
            com.bitchat.android.util.AppConstants.UI.PERMISSION_FORCE_FINISH, 
            null, 
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        
        if (intent.getBooleanExtra("ACTION_QUIT_APP", false)) {
            finish()
            return
        }

        com.bitchat.android.service.AppShutdownCoordinator.cancelPendingShutdown()
        
        ensureDependenciesInitialized()
        try { com.bitchat.android.service.MeshForegroundService.start(applicationContext) } catch (_: Exception) { }
        initStatusManagers()
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.onboardingState.collect {
                    syncStateToFlutter()
                }
            }
        }

        if (mainViewModel.onboardingState.value == OnboardingState.CHECKING) {
            checkOnboardingStatus()
        }
    }

    private fun ensureDependenciesInitialized() {
        if (!::identityManager.isInitialized) {
            identityManager = SecureIdentityStateManager(this)
        }
        if (!::meshService.isInitialized) {
            meshService = com.bitchat.android.service.MeshServiceHolder.getOrCreate(applicationContext)
        }
        if (!::permissionManager.isInitialized) {
            permissionManager = PermissionManager(this)
        }
        
        // Initialize Tor provider to ensure proxy is available for Nostr relays
        try {
            com.bitchat.android.net.ArtiTorManager.getInstance().init(application)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to initialize ArtiTorManager: ${e.message}")
        }
    }

    private fun setOrientationBasedOnDeviceType() {
        requestedOrientation = if (DeviceUtils.isTablet(this)) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        ensureDependenciesInitialized()
        channels = BitchatFlutterChannels(flutterEngine.dartExecutor.binaryMessenger, meshService, identityManager)
        channels?.onActionRequested = { action, params ->
            handleFlutterAction(action, params)
        }
    }

    private fun handleFlutterAction(action: String, params: Map<String, Any?>?) {
        when (action) {
            "enableBluetooth" -> {
                mainViewModel.updateBluetoothLoading(true)
                bluetoothStatusManager.requestEnableBluetooth()
            }
            "enableLocation" -> {
                mainViewModel.updateLocationLoading(true)
                locationStatusManager.requestEnableLocation()
            }
            "disableBatteryOptimization" -> {
                mainViewModel.updateBatteryOptimizationLoading(true)
                batteryOptimizationManager.requestDisableBatteryOptimization()
            }
            "requestPermissions" -> {
                mainViewModel.updateOnboardingState(OnboardingState.PERMISSION_REQUESTING)
                onboardingCoordinator.requestPermissions()
            }
            "requestBackgroundLocation" -> onboardingCoordinator.requestBackgroundLocation()
            "skipBackgroundLocation" -> onboardingCoordinator.skipBackgroundLocation()
            "retryOnboarding" -> {
                mainViewModel.updateOnboardingState(OnboardingState.CHECKING)
                checkOnboardingStatus()
            }
            "openSettings" -> onboardingCoordinator.openAppSettings()
        }
    }

    private fun syncStateToFlutter() {
        channels?.emitEvent(mapOf(
            "type" to "onboardingStateChanged",
            "state" to mainViewModel.onboardingState.value.name,
            "bluetoothStatus" to mainViewModel.bluetoothStatus.value.name,
            "locationStatus" to mainViewModel.locationStatus.value.name,
            "batteryStatus" to mainViewModel.batteryOptimizationStatus.value.name,
            "errorMessage" to mainViewModel.errorMessage.value,
            "isBluetoothLoading" to mainViewModel.isBluetoothLoading.value,
            "isLocationLoading" to mainViewModel.isLocationLoading.value,
            "isBatteryLoading" to mainViewModel.isBatteryOptimizationLoading.value
        ))
    }

    private fun initStatusManagers() {
        bluetoothStatusManager = BluetoothStatusManager(this, this, ::handleBluetoothEnabled, ::handleBluetoothDisabled)
        locationStatusManager = LocationStatusManager(this, this, ::handleLocationEnabled, ::handleLocationDisabled)
        batteryOptimizationManager = BatteryOptimizationManager(this, this, ::handleBatteryOptimizationDisabled, ::handleBatteryOptimizationFailed)
        onboardingCoordinator = OnboardingCoordinator(this, permissionManager, ::handleOnboardingComplete, {
            mainViewModel.updateOnboardingState(OnboardingState.BACKGROUND_LOCATION_EXPLANATION)
        }, ::handleOnboardingFailed)
    }

    private fun checkOnboardingStatus() {
        lifecycleScope.launch {
            delay(500)
            checkBluetoothAndProceed()
        }
    }

    private fun checkBluetoothAndProceed() {
        if (permissionManager.isFirstTimeLaunch()) {
            proceedWithPermissionCheck()
            return
        }
        mainViewModel.updateBluetoothStatus(bluetoothStatusManager.checkBluetoothStatus())
        when (mainViewModel.bluetoothStatus.value) {
            BluetoothStatus.ENABLED -> checkLocationAndProceed()
            else -> {
                mainViewModel.updateOnboardingState(OnboardingState.BLUETOOTH_CHECK)
                mainViewModel.updateBluetoothLoading(false)
            }
        }
    }

    private fun proceedWithPermissionCheck() {
        lifecycleScope.launch {
            delay(200)
            if (permissionManager.isFirstTimeLaunch() || !permissionManager.areRequiredPermissionsGranted()) {
                mainViewModel.updateOnboardingState(OnboardingState.PERMISSION_EXPLANATION)
            } else if (permissionManager.needsBackgroundLocationPermission() && !permissionManager.isBackgroundLocationGranted() && !BackgroundLocationPreferenceManager.isSkipped(this@MainActivity)) {
                mainViewModel.updateOnboardingState(OnboardingState.BACKGROUND_LOCATION_EXPLANATION)
            } else {
                mainViewModel.updateOnboardingState(OnboardingState.INITIALIZING)
                initializeApp()
            }
        }
    }

    private fun handleBluetoothEnabled() {
        mainViewModel.updateBluetoothLoading(false)
        mainViewModel.updateBluetoothStatus(BluetoothStatus.ENABLED)
        checkLocationAndProceed()
    }

    private fun handleBluetoothDisabled(message: String) {
        mainViewModel.updateBluetoothLoading(false)
        mainViewModel.updateBluetoothStatus(bluetoothStatusManager.checkBluetoothStatus())
        if (message.contains("Permission")) {
            proceedWithPermissionCheck()
        } else {
            mainViewModel.updateOnboardingState(OnboardingState.BLUETOOTH_CHECK)
        }
    }

    private fun checkLocationAndProceed() {
        if (permissionManager.isFirstTimeLaunch()) {
            proceedWithPermissionCheck()
            return
        }
        mainViewModel.updateLocationStatus(locationStatusManager.checkLocationStatus())
        when (mainViewModel.locationStatus.value) {
            LocationStatus.ENABLED -> checkBatteryOptimizationAndProceed()
            else -> {
                mainViewModel.updateOnboardingState(OnboardingState.LOCATION_CHECK)
                mainViewModel.updateLocationLoading(false)
            }
        }
    }

    private fun handleLocationEnabled() {
        mainViewModel.updateLocationLoading(false)
        mainViewModel.updateLocationStatus(LocationStatus.ENABLED)
        checkBatteryOptimizationAndProceed()
    }

    private fun handleLocationDisabled(message: String) {
        mainViewModel.updateLocationLoading(false)
        mainViewModel.updateLocationStatus(locationStatusManager.checkLocationStatus())
        mainViewModel.updateOnboardingState(OnboardingState.LOCATION_CHECK)
    }

    private fun checkBatteryOptimizationAndProceed() {
        if (permissionManager.isFirstTimeLaunch() || BatteryOptimizationPreferenceManager.isSkipped(this)) {
            proceedWithPermissionCheck()
            return
        }
        val status = if (batteryOptimizationManager.isBatteryOptimizationDisabled()) BatteryOptimizationStatus.DISABLED else BatteryOptimizationStatus.ENABLED
        mainViewModel.updateBatteryOptimizationStatus(status)
        if (status == BatteryOptimizationStatus.DISABLED) {
            proceedWithPermissionCheck()
        } else {
            mainViewModel.updateOnboardingState(OnboardingState.BATTERY_OPTIMIZATION_CHECK)
            mainViewModel.updateBatteryOptimizationLoading(false)
        }
    }

    private fun handleBatteryOptimizationDisabled() {
        mainViewModel.updateBatteryOptimizationLoading(false)
        mainViewModel.updateBatteryOptimizationStatus(BatteryOptimizationStatus.DISABLED)
        proceedWithPermissionCheck()
    }

    private fun handleBatteryOptimizationFailed(message: String) {
        mainViewModel.updateBatteryOptimizationLoading(false)
        mainViewModel.updateOnboardingState(OnboardingState.BATTERY_OPTIMIZATION_CHECK)
    }

    private fun handleOnboardingComplete() {
        val bt = bluetoothStatusManager.checkBluetoothStatus()
        val loc = locationStatusManager.checkLocationStatus()
        if (bt != BluetoothStatus.ENABLED) {
            mainViewModel.updateOnboardingState(OnboardingState.BLUETOOTH_CHECK)
        } else if (loc != LocationStatus.ENABLED) {
            mainViewModel.updateOnboardingState(OnboardingState.LOCATION_CHECK)
        } else {
            mainViewModel.updateOnboardingState(OnboardingState.INITIALIZING)
            initializeApp()
        }
    }

    private fun handleOnboardingFailed(message: String) {
        mainViewModel.updateErrorMessage(message)
        mainViewModel.updateOnboardingState(OnboardingState.ERROR)
    }

    private fun initializeApp() {
        lifecycleScope.launch {
            try {
                // Ensure Tor is started before initializing components that depend on network/Tor
                val torManager = com.bitchat.android.net.ArtiTorManager.getInstance()
                torManager.init(application)
                if (com.bitchat.android.net.TorPreferenceManager.get(this@MainActivity) == com.bitchat.android.net.TorMode.ON) {
                    Log.i("MainActivity", "Starting Tor proxy...")
                    torManager.applyMode(application, com.bitchat.android.net.TorMode.ON)
                }

                delay(1000)
                com.bitchat.android.nostr.PoWPreferenceManager.init(this@MainActivity)
                com.bitchat.android.nostr.LocationNotesInitializer.initialize(this@MainActivity)
                
                if (!permissionManager.areAllPermissionsGranted()) {
                    handleOnboardingFailed("Permissions revoked.")
                    return@launch
                }

                meshService.startServices()
                delay(500)
                mainViewModel.updateOnboardingState(OnboardingState.COMPLETE)
            } catch (e: Exception) {
                handleOnboardingFailed("Initialization failed: ${e.message}")
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("ACTION_QUIT_APP", false)) {
            finish()
            return
        }
        com.bitchat.android.service.AppShutdownCoordinator.cancelPendingShutdown()
    }

    override fun onResume() {
        super.onResume()
        if (mainViewModel.onboardingState.value == OnboardingState.COMPLETE) {
            val bt = bluetoothStatusManager.checkBluetoothStatus()
            val loc = locationStatusManager.checkLocationStatus()
            if (bt != BluetoothStatus.ENABLED) mainViewModel.updateOnboardingState(OnboardingState.BLUETOOTH_CHECK)
            else if (loc != LocationStatus.ENABLED) mainViewModel.updateOnboardingState(OnboardingState.LOCATION_CHECK)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(forceFinishReceiver) } catch (_: Exception) { }
        try { locationStatusManager.cleanup() } catch (_: Exception) { }
    }
}
