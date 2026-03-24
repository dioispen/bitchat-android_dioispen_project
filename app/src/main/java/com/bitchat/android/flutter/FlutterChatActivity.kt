package com.bitchat.android.flutter

import android.os.Bundle
import com.bitchat.android.identity.SecureIdentityStateManager
import com.bitchat.android.service.MeshServiceHolder
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

        // 取得 meshService 與 identityManager
        val meshService = MeshServiceHolder.getOrCreate(applicationContext)
        val identityManager = SecureIdentityStateManager(applicationContext)

        // 這行就是「Kotlin 端把 Channel 綁到 Flutter engine」的地方
        // flutterEngine.dartExecutor.binaryMessenger 就是 messenger（通訊管道的底層）
        channels = BitchatFlutterChannels(
            flutterEngine.dartExecutor.binaryMessenger,
            meshService,
            identityManager
        )

        // 處理來自 Flutter 的請求
        channels?.onActionRequested = { action, params ->
            when (action) {
                "enableBluetooth" -> {
                    // TODO: 請求藍牙權限或開啟藍牙
                }
                "requestPermissions" -> {
                    // TODO: 請求必要權限
                }
            }
        }
    }

    override fun cleanUpFlutterEngine(flutterEngine: FlutterEngine) {
        // 如果你有長期持有的事件訂閱，可在這裡解除
        channels = null
        super.cleanUpFlutterEngine(flutterEngine)
    }
}
