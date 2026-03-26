package com.bitchat.android.flutter

import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

/**
 * 用來啟動 Flutter UI 的 Activity。
 */
class FlutterChatActivity : FlutterActivity() {

    private var channels: BitchatFlutterChannels? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // 傳遞 context 給 channels
        channels = BitchatFlutterChannels(this, flutterEngine.dartExecutor.binaryMessenger)
    }

    override fun cleanUpFlutterEngine(flutterEngine: FlutterEngine) {
        channels = null
        super.cleanUpFlutterEngine(flutterEngine)
    }
}
