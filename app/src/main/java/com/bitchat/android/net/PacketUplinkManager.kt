package com.bitchat.android.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.bitchat.android.protocol.BitchatPacket
import com.bitchat.android.util.toHexString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * 負責將接收到的 BLE 封包透過網路轉發至後端伺服器 (Gateway 功能)
 */
class PacketUplinkManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val client = OkHttpProvider.httpClient()

    companion object {
        private const val TAG = "PacketUplinkManager"
        // TODO: 修改為您預計開發的 Server URL
        private const val UPLINK_URL = "http://172.20.10.2:8080/health-report"
        private val MEDIA_TYPE_OCTET_STREAM = "application/octet-stream".toMediaType()
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
                val binaryData = packet.toBinaryData() ?: return@launch
                
                Log.d(TAG, " 正在上傳封包 (Type: ${packet.type}, Size: ${binaryData.size} bytes) 至伺服器...")

                val request = Request.Builder()
                    .url(UPLINK_URL)
                    .post(binaryData.toRequestBody(MEDIA_TYPE_OCTET_STREAM))
                    .addHeader("X-Packet-Type", packet.type.toString())
                    .addHeader("X-Sender-ID", packet.senderID.toHexString())
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
