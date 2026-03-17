package com.bitchat.android.flutter

import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

/**
 * 用來啟動 Flutter UI 的 Activity。
 *
 * 重點：覆寫 [configureFlutterEngine] 取得 engine 後，
 * 在這裡把 Channel（MethodChannel/EventChannel）註冊到 engine 的 messenger 上，
 * 讓 Flutter UI 可以跟 Kotlin 互通。
 */
class FlutterChatActivity : FlutterActivity() {

    private var channels: BitchatFlutterChannels? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 如果你想從 Android 傳初始路由/參數給 Flutter，可改用 cached engine + initialRoute
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // 這行就是「Kotlin 端把 Channel 綁到 Flutter engine」的地方
        // flutterEngine.dartExecutor.binaryMessenger 就是 messenger（通訊管道的底層）
        channels = BitchatFlutterChannels(flutterEngine.dartExecutor.binaryMessenger)

        // TODO: 你也可以把 app 的 mesh service / viewmodel / repository 注入進 channels，
        //       讓 channels 真的去呼叫 send/start/stop 等功能。
    }

    override fun cleanUpFlutterEngine(flutterEngine: FlutterEngine) {
        // 如果你有長期持有的事件訂閱，可在這裡解除
        channels = null
        super.cleanUpFlutterEngine(flutterEngine)
    }
}

