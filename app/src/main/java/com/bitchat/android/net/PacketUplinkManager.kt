package com.bitchat.android.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.bitchat.android.protocol.BitchatPacket
import com.bitchat.android.protocol.MessageType
import com.bitchat.android.protocol.HealthReportPayload
import com.bitchat.android.util.toHexString
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * 負責將接收到的 BLE 封包透過網路轉發至後端伺ui伺服器 (Gateway 功能)
 */
class PacketUplinkManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val gson = Gson()
    
    // 使用預設的 httpClient (不帶 context)，這樣它會信任系統標準憑證 (如 ngrok 使用的 Let's Encrypt)
    private val client by lazy { OkHttpProvider.httpClient() }

    companion object {
        private const val TAG = "PacketUplinkManager"
        private const val UPLINK_URL = "https://delphine-eisteddfodic-afflictively.ngrok-free.dev/health-report"
        private val MEDIA_TYPE_OCTET_STREAM = "application/octet-stream".toMediaType()
        private val MEDIA_TYPE_JSON = "application/json".toMediaType()
    }

    /**
     * 檢查是否有網際網路連線
     */
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * 將封包上傳至伺服器
     */
    fun uplinkPacketIfNeeded(packet: BitchatPacket) {
        if (!isInternetAvailable()) {
            return
        }

        scope.launch {
            try {
                val isHealthReport = packet.type == MessageType.HEALTH_REPORT.value
                
                // 如果是健康報告，則轉譯為 JSON 再上傳；其餘維持二進位格式
                val (requestBody, mediaType) = if (isHealthReport) {
                    val payloadObj = HealthReportPayload.decode(packet.payload)
                    val jsonString = if (payloadObj != null) {
                        gson.toJson(payloadObj)
                    } else {
                        // 若解碼失敗，嘗試直接作為字串處理（以防原始 payload 已是 JSON）
                        String(packet.payload, Charsets.UTF_8)
                    }
                    jsonString.toRequestBody(MEDIA_TYPE_JSON) to MEDIA_TYPE_JSON
                } else {
                    val binaryData = packet.toBinaryData() ?: return@launch
                    binaryData.toRequestBody(MEDIA_TYPE_OCTET_STREAM) to MEDIA_TYPE_OCTET_STREAM
                }
                
                Log.d(TAG, " 正在上傳封包 (Type: ${packet.type}, Format: ${if (isHealthReport) "JSON" else "Binary"}) 至伺服器...")

                val request = Request.Builder()
                    .url(UPLINK_URL)
                    .post(requestBody)
                    .addHeader("X-Packet-Type", packet.type.toString())
                    .addHeader("X-Sender-ID", packet.senderID.toHexString())
                    // ngrok 免費版必須帶上此 Header，否則 API 會收到 HTML 警告頁面而報錯
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d(TAG, " 封包上傳成功: ${response.code}")
                    } else {
                        Log.w(TAG, " 封包上傳失敗: ${response.code} - ${response.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 上傳封包時發生異常: ${e.message}")
            }
        }
    }
}
