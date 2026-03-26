package com.bitchat.android.flutter

import android.content.Context
import android.util.Log
import com.bitchat.android.identity.SecureIdentityStateManager
import com.bitchat.android.service.MeshServiceHolder
import com.bitchat.android.crypto.EncryptionService
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
) : MethodChannel.MethodCallHandler, EventChannel.StreamHandler {

    private val methodChannel = MethodChannel(messenger, METHOD_CHANNEL_NAME)
    private val eventChannel = EventChannel(messenger, EVENT_CHANNEL_NAME)
    private val identityManager = SecureIdentityStateManager(context)

    private var eventSink: EventChannel.EventSink? = null

    init {
        methodChannel.setMethodCallHandler(this)
        eventChannel.setStreamHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            // 檢查是否已註冊（是否有身份密鑰）
            "isRegistered" -> {
                result.success(identityManager.hasIdentityData())
            }

            // 獲取個人資料
            "getProfile" -> {
                if (identityManager.hasIdentityData()) {
                    val keyPair = identityManager.loadStaticKey()
                    val fingerprint = keyPair?.second?.let { identityManager.generateFingerprint(it) }
                    val nickname = fingerprint?.let { identityManager.getCachedFingerprintNickname(it) }
                    
                    result.success(mapOf(
                        "fingerprint" to fingerprint,
                        "nickname" to nickname,
                        "peerId" to fingerprint?.take(16)
                    ))
                } else {
                    result.success(null)
                }
            }

            // 執行註冊（產生原生密鑰並儲存暱稱）
            "register" -> {
                val nickname = call.argument<String>("nickname") ?: "User"
                try {
                    // 1. 產生身份密鑰
                    val encryptionService = EncryptionService(context)
                    // 確保身份存在，如果不存在會自動產生
                    // Note: EncryptionService constructor calls initialize() which calls loadOrCreateEd25519KeyPair()
                    // But for Static Noise Key, we should ensure it's generated.
                    // Based on EncryptionService.kt, it uses NoiseEncryptionService which handles this in loadOrGenerateKeys()
                    
                    val fingerprint = encryptionService.getIdentityFingerprint()
                    identityManager.cacheFingerprintNickname(fingerprint, nickname)
                    
                    result.success(true)
                } catch (e: Exception) {
                    Log.e("BitchatBridge", "Registration failed", e)
                    result.error("REGISTRATION_FAILED", e.message, null)
                }
            }

            // 啟動 Mesh 服務
            "startMesh" -> {
                try {
                    val service = MeshServiceHolder.getOrCreate(context)
                    service.startServices()
                    result.success(true)
                } catch (e: Exception) {
                    result.error("START_FAILED", e.message, null)
                }
            }

            // 發送訊息
            "sendMessage" -> {
                val peerId = call.argument<String>("peerId")
                val text = call.argument<String>("text")
                val isPublic = call.argument<Boolean>("isPublic") ?: true

                val service = MeshServiceHolder.meshService
                if (service == null) {
                    result.error("SERVICE_NOT_RUNNING", "Mesh service is not initialized", null)
                    return
                }

                if (isPublic) {
                    service.sendMessage(text ?: "")
                } else if (peerId != null) {
                    val nickname = identityManager.getCachedFingerprintNickname(peerId) ?: "Peer"
                    service.sendPrivateMessage(text ?: "", peerId, nickname)
                }
                result.success(null)
            }

            // 獲取附近裝置 (Peers)
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
        emitEvent(mapOf("type" to "status", "status" to "bridge_ready"))
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }

    fun emitEvent(event: Map<String, Any?>) {
        eventSink?.success(event)
    }

    companion object {
        const val METHOD_CHANNEL_NAME = "com.bitchat/bridge/methods"
        const val EVENT_CHANNEL_NAME = "com.bitchat/bridge/events"
    }
}
