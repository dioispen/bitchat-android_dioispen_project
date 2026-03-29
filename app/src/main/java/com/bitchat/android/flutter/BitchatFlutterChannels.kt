package com.bitchat.android.flutter

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.bluetooth.BluetoothAdapter
import android.location.LocationManager
import android.util.Log
import com.bitchat.android.identity.SecureIdentityStateManager
import com.bitchat.android.service.MeshServiceHolder
import com.bitchat.android.service.MeshForegroundService
import com.bitchat.android.crypto.EncryptionService
import com.bitchat.android.onboarding.PermissionManager
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/**
 * 核心橋樑：負責 Kotlin 原生功能與 Flutter UI 的通訊
 */
class BitchatFlutterChannels(
    private val context: Context,
    messenger: BinaryMessenger,
    private val activity: Activity? = null
) : MethodChannel.MethodCallHandler, EventChannel.StreamHandler {

    private val methodChannel = MethodChannel(messenger, METHOD_CHANNEL_NAME)
    private val eventChannel = EventChannel(messenger, EVENT_CHANNEL_NAME)
    private val identityManager = SecureIdentityStateManager(context)
    private val permissionManager = PermissionManager(context)

    private var eventSink: EventChannel.EventSink? = null

    // 監聽系統藍牙與位置狀態變更
    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            emitStatusUpdate()
        }
    }

    init {
        methodChannel.setMethodCallHandler(this)
        eventChannel.setStreamHandler(this)
        
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        context.registerReceiver(statusReceiver, filter)
    }

    private fun emitStatusUpdate() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val bluetoothEnabled = bluetoothAdapter?.isEnabled ?: false
        
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
                             locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        
        val permissionsGranted = permissionManager.areRequiredPermissionsGranted()
        val notificationGranted = if (android.os.Build.VERSION.SDK_INT >= 33) {
            permissionManager.isPermissionGranted(android.Manifest.permission.POST_NOTIFICATIONS)
        } else true

        emitEvent(mapOf(
            "type" to "system_status",
            "bluetoothEnabled" to bluetoothEnabled,
            "locationEnabled" to locationEnabled,
            "permissionsGranted" to permissionsGranted,
            "notificationGranted" to notificationGranted
        ))
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "checkPermissions" -> {
                val requiredGranted = permissionManager.areRequiredPermissionsGranted()
                val notificationGranted = if (android.os.Build.VERSION.SDK_INT >= 33) {
                    permissionManager.isPermissionGranted(android.Manifest.permission.POST_NOTIFICATIONS)
                } else true
                result.success(requiredGranted && notificationGranted)
            }

            "requestPermissions" -> {
                if (activity == null) {
                    result.error("NO_ACTIVITY", "Cannot request permissions without an activity", null)
                    return
                }
                val permissions = mutableListOf<String>()
                permissions.addAll(permissionManager.getRequiredPermissions())
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                activity.requestPermissions(permissions.toTypedArray(), 1001)
                result.success(true)
            }

            "isRegistered" -> {
                result.success(identityManager.hasIdentityData())
            }

            "startMesh" -> {
                try {
                    // 關鍵修正：確保在啟動 Mesh 時也嘗試啟動通知欄 Foreground Service
                    MeshForegroundService.start(context)

                    val service = MeshServiceHolder.getOrCreate(context)
                    service.startServices()
                    result.success(true)
                } catch (e: Exception) {
                    result.error("START_FAILED", e.message, null)
                }
            }

            "getNearbyPeers" -> {
                val service = MeshServiceHolder.meshService
                val peers = service?.getPeerNicknames() ?: emptyMap<String, String>()
                result.success(peers)
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink) {
        eventSink = events
        emitStatusUpdate()
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }

    fun emitEvent(event: Map<String, Any?>) {
        activity?.runOnUiThread {
            eventSink?.success(event)
        }
    }

    fun destroy() {
        try { context.unregisterReceiver(statusReceiver) } catch (e: Exception) {}
    }

    companion object {
        const val METHOD_CHANNEL_NAME = "com.bitchat/bridge/methods"
        const val EVENT_CHANNEL_NAME = "com.bitchat/bridge/events"
    }
}
