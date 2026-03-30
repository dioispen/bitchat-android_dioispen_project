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
import com.bitchat.android.protocol.MessageType
import com.bitchat.android.protocol.BitchatPacket
import com.bitchat.android.net.PacketUplinkManager
import com.bitchat.android.util.toHexString
import com.google.gson.Gson
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
    private val uplinkManager = PacketUplinkManager(context)
    private val gson = Gson()

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

        // 監聽來自 Mesh 的封包
        MeshServiceHolder.onPacketReceived = { packet: BitchatPacket ->
            if (packet.type == MessageType.HEALTH_REPORT.value) {
                try {
                    val payloadStr = String(packet.payload, Charsets.UTF_8)
                    val reportMap = gson.fromJson(payloadStr, Map::class.java)
                    emitEvent(mapOf(
                        "type" to "health_report",
                        "report" to reportMap,
                        "senderId" to packet.senderID.toHexString()
                    ))
                } catch (e: Exception) {
                    Log.e("BitchatBridge", "Failed to parse health report packet", e)
                }
            }
        }
    }

    private fun getStatusMap(): Map<String, Any> {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val bluetoothEnabled = bluetoothAdapter?.isEnabled ?: false
        
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
                             locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        
        val permissionsGranted = permissionManager.areRequiredPermissionsGranted()
        val notificationGranted = if (android.os.Build.VERSION.SDK_INT >= 33) {
            permissionManager.isPermissionGranted(android.Manifest.permission.POST_NOTIFICATIONS)
        } else true

        return mapOf(
            "type" to "system_status",
            "bluetoothEnabled" to bluetoothEnabled,
            "locationEnabled" to locationEnabled,
            "permissionsGranted" to permissionsGranted,
            "notificationGranted" to notificationGranted
        )
    }

    private fun emitStatusUpdate() {
        emitEvent(getStatusMap())
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getSystemStatus" -> {
                result.success(getStatusMap())
            }

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
                    MeshForegroundService.start(context)
                    val service = MeshServiceHolder.getOrCreate(context)
                    service.startServices()
                    result.success(true)
                } catch (e: Exception) {
                    result.error("START_FAILED", e.message, null)
                }
            }

            "sendHealthReport" -> {
                try {
                    val reportMap = call.arguments as Map<*, *>
                    val reportJson = gson.toJson(reportMap)
                    val payload = reportJson.toByteArray(Charsets.UTF_8)
                    
                    val service = MeshServiceHolder.meshService
                    if (service != null) {
                        val publicKey = identityManager.loadStaticKey()?.second
                        val senderIdHex = publicKey?.toHexString() ?: "0000000000000000"
                        
                        val packet = BitchatPacket(
                            type = MessageType.HEALTH_REPORT.value,
                            ttl = 3u,
                            senderID = senderIdHex,
                            payload = payload
                        )
                        
                        service.sendMessage(String(payload, Charsets.UTF_8))
                        uplinkManager.uplinkPacketIfNeeded(packet)
                        result.success(true)
                    } else {
                        result.error("SERVICE_NOT_READY", "Mesh service is not running", null)
                    }
                } catch (e: Exception) {
                    result.error("SEND_FAILED", e.message, null)
                }
            }

            "getNearbyPeers" -> {
                val service = MeshServiceHolder.meshService
                val peers = service?.getPeerNicknames() ?: emptyMap<String, String>()
                result.success(peers)
            }

            "sendMessage" -> {
                val text = call.argument<String>("text") ?: ""
                val service = MeshServiceHolder.meshService
                service?.sendMessage(text)
                result.success(null)
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
        MeshServiceHolder.onPacketReceived = null
    }

    companion object {
        const val METHOD_CHANNEL_NAME = "com.bitchat/bridge/methods"
        const val EVENT_CHANNEL_NAME = "com.bitchat/bridge/events"
    }
}
