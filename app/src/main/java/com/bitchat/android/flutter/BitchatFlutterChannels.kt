package com.bitchat.android.flutter

import android.util.Log
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/**
 * 這個檔案就是「Flutter <-> Kotlin 溝通」的核心：
 *
 * - Flutter 端（Dart）對應：
 *   - MethodChannel: `com.bitchat/bridge/methods`
 *   - EventChannel : `com.bitchat/bridge/events`
 *
 * - Kotlin 端（本檔案）負責：
 *   - 註冊 Channel（把名稱綁到 messenger）
 *   - 實作 Flutter 呼叫 Kotlin 的方法（MethodChannel）
 *   - 提供 Kotlin 主動推事件到 Flutter（EventChannel 的 EventSink）
 *
 * 注意：
 * - MethodChannel 是「請求/回應」：Flutter 呼叫一次，Kotlin 回一次結果
 * - EventChannel 是「串流事件」：Kotlin 隨時可以推事件給 Flutter（例如：新訊息、peer 列表更新）
 */
class BitchatFlutterChannels(
    messenger: BinaryMessenger,
) : MethodChannel.MethodCallHandler, EventChannel.StreamHandler {

    // === Channel 名稱必須跟 Flutter 端完全一致 ===
    private val methodChannel = MethodChannel(messenger, METHOD_CHANNEL_NAME)
    private val eventChannel = EventChannel(messenger, EVENT_CHANNEL_NAME)

    // EventChannel 會在 Flutter 開始「訂閱事件流」時給你一個 sink
    // 之後你只要呼叫 sink.success(map) 就能把事件推到 Flutter
    private var eventSink: EventChannel.EventSink? = null

    init {
        // 把 Kotlin handler 綁到 channel 上
        methodChannel.setMethodCallHandler(this)
        eventChannel.setStreamHandler(this)
    }

    /**
     * Flutter -> Kotlin 的 MethodChannel 入口
     *
     * Flutter 端範例（Dart）：
     * `MethodChannel('com.bitchat/bridge/methods').invokeMethod('sendMessage', {...})`
     */
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "start" -> {
                // TODO: 你可以在這裡呼叫既有的 mesh service 開始（或確保已啟動）
                // 目前只回傳 success，讓 Flutter UI 先跑起來
                result.success(null)
            }

            "stop" -> {
                // TODO: 停止 mesh / 釋放資源（如果你的架構允許）
                result.success(null)
            }

            "sendMessage" -> {
                // Flutter 端會傳一個 Map，例如：
                // { "peerId": "...", "text": "..." }
                val peerId = call.argument<String>("peerId")
                val text = call.argument<String>("text")

                if (peerId.isNullOrBlank() || text.isNullOrBlank()) {
                    result.error(
                        "BAD_ARGS",
                        "peerId/text 不能為空",
                        mapOf("peerId" to peerId, "text" to text)
                    )
                    return
                }

                // TODO: 在這裡把訊息丟到你現有的 Chat/Mesh 邏輯（例如 BluetoothMeshService）
                // 先做 demo：Kotlin 收到後立刻回傳成功，並「回推一個 message 事件」給 Flutter 當作示範
                emitEvent(
                    mapOf(
                        "type" to "message",
                        "from" to "android-echo",
                        "text" to text,
                        "peerId" to peerId,
                    )
                )
                result.success(null)
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    /**
     * Flutter 開始訂閱 EventChannel 時會進來
     *
     * Flutter 端（Dart）：
     * `EventChannel('com.bitchat/bridge/events').receiveBroadcastStream()...`
     */
    override fun onListen(arguments: Any?, events: EventChannel.EventSink) {
        eventSink = events

        // 通常會在訂閱成功後先推一個「ready」或「status」事件，方便 UI 顯示狀態
        emitEvent(
            mapOf(
                "type" to "status",
                "status" to "connected",
                "platform" to "android",
            )
        )
    }

    /**
     * Flutter 取消訂閱 EventChannel 時會進來
     */
    override fun onCancel(arguments: Any?) {
        eventSink = null
    }

    /**
     * Kotlin -> Flutter：推送事件（Map）到 Dart
     *
     * 建議格式（你可以依需求擴充）：
     * - type = "message" / "status" / "peers"
     */
    fun emitEvent(event: Map<String, Any?>) {
        val sink = eventSink
        if (sink == null) {
            Log.d(TAG, "emitEvent dropped (no listeners): $event")
            return
        }
        sink.success(event)
    }

    companion object {
        private const val TAG = "BitchatFlutterChannels"

        // Flutter 端也必須使用同一個字串
        const val METHOD_CHANNEL_NAME = "com.bitchat/bridge/methods"
        const val EVENT_CHANNEL_NAME = "com.bitchat/bridge/events"
    }
}

