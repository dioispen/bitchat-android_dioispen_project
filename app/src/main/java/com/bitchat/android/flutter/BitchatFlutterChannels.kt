package com.bitchat.android.flutter

import android.util.Log
import com.bitchat.android.identity.SecureIdentityStateManager
import com.bitchat.android.mesh.BluetoothMeshDelegate
import com.bitchat.android.mesh.BluetoothMeshService
import com.bitchat.android.model.BitchatMessage
import com.bitchat.android.onboarding.OnboardingState
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/**
 * 橋接 Flutter 與 Kotlin 原生模組的核心類別
 */
class BitchatFlutterChannels(
    messenger: BinaryMessenger,
    private val meshService: BluetoothMeshService,
    private val identityManager: SecureIdentityStateManager
) : MethodChannel.MethodCallHandler, EventChannel.StreamHandler, BluetoothMeshDelegate {

    private val methodChannel = MethodChannel(messenger, METHOD_CHANNEL_NAME)
    private val eventChannel = EventChannel(messenger, EVENT_CHANNEL_NAME)
    private var eventSink: EventChannel.EventSink? = null

    // 回呼介面，讓 MainActivity 可以處理某些來自 Flutter 的請求（如權限或開關）
    var onActionRequested: ((String, Map<String, Any?>?) -> Unit)? = null

    init {
        methodChannel.setMethodCallHandler(this)
        eventChannel.setStreamHandler(this)
        // 將自己設為 mesh service 的 delegate 以接收訊息並轉發給 Flutter
        meshService.delegate = this
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getOnboardingState" -> {
                // 由 MainActivity 透過 emitEvent 推送狀態，或在此主動回傳
                result.success(null) 
            }
            "requestAction" -> {
                // 處理 Flutter 端的請求，例如 "enableBluetooth", "requestPermissions"
                val action = call.argument<String>("action")
                val params = call.argument<Map<String, Any?>>("params")
                if (action != null) {
                    onActionRequested?.invoke(action, params)
                    result.success(true)
                } else {
                    result.error("INVALID_ACTION", "Action is null", null)
                }
            }
            "getIdentityInfo" -> {
                val info = mapOf(
                    "fingerprint" to meshService.myPeerID,
                    "staticPublicKey" to (identityManager.loadStaticKey()?.second?.let { android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT) } ?: "")
                )
                result.success(info)
            }
            "sendMessage" -> {
                val peerId = call.argument<String>("peerId")
                val text = call.argument<String>("text")
                if (peerId != null && text != null) {
                    meshService.sendPrivateMessage(text, peerId, "Peer")
                    result.success(true)
                } else {
                    result.error("BAD_ARGS", "Missing peerId or text", null)
                }
            }
            "startMesh" -> {
                meshService.startServices()
                result.success(true)
            }
            else -> result.notImplemented()
        }
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink) {
        eventSink = events
        // 初始狀態推送
        emitEvent(mapOf("type" to "bridgeReady"))
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }

    fun emitEvent(event: Map<String, Any?>) {
        eventSink?.success(event)
    }

    // === BluetoothMeshDelegate Implementation ===

    override fun didReceiveMessage(message: BitchatMessage) {
        emitEvent(mapOf(
            "type" to "messageReceived",
            "message" to mapOf(
                "id" to message.id,
                "text" to message.text,
                "senderId" to message.senderPeerID,
                "timestamp" to message.timestamp,
                "isPrivate" to message.isPrivate
            )
        ))
    }

    override fun didUpdatePeerList(peers: List<String>) {
        emitEvent(mapOf(
            "type" to "peerListUpdated",
            "peers" to peers
        ))
    }

    override fun didReceiveChannelLeave(channel: String, fromPeer: String) {}
    override fun didReceiveDeliveryAck(messageID: String, recipientPeerID: String) {}
    override fun didReceiveReadReceipt(messageID: String, recipientPeerID: String) {}
    override fun didReceiveVerifyChallenge(peerID: String, payload: ByteArray, timestampMs: Long) {}
    override fun didReceiveVerifyResponse(peerID: String, payload: ByteArray, timestampMs: Long) {}
    override fun decryptChannelMessage(encryptedContent: ByteArray, channel: String): String? = null
    override fun getNickname(): String? = null
    override fun isFavorite(peerID: String): Boolean = false

    companion object {
        const val METHOD_CHANNEL_NAME = "com.bitchat/bridge/methods"
        const val EVENT_CHANNEL_NAME = "com.bitchat/bridge/events"
    }
}
