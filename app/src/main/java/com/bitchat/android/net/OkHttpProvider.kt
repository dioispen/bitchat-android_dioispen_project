package com.bitchat.android.net

import android.content.Context
import com.bitchat.android.R
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Centralized OkHttp provider.
 */
object OkHttpProvider {
    private val httpClientRef = AtomicReference<OkHttpClient?>(null)
    private val wsClientRef = AtomicReference<OkHttpClient?>(null)

    fun reset() {
        httpClientRef.set(null)
        wsClientRef.set(null)
    }

    /**
     * Get HTTP client.
     * @param context Optional context to load the custom certificate from res/raw/cert.pem
     */
    fun httpClient(context: Context? = null): OkHttpClient {
        httpClientRef.get()?.let { return it }

        val builder = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            // 增加超時時間，適應 ngrok 或不穩定的內網環境
            .callTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            // 即使是用 ngrok，保留 hostnameVerifier 對於測試環境也比較安全
            .hostnameVerifier { _, _ -> true }

        if (context != null) {
            try {
                // 1. 載入 res/raw 中的 .pem 憑證
                val certInputStream = context.resources.openRawResource(R.raw.cert)
                val cf = CertificateFactory.getInstance("X.509")
                val ca = cf.generateCertificate(certInputStream)
                certInputStream.close()

                // 2. 建立 KeyStore
                val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                    load(null, null)
                    setCertificateEntry("ca", ca)
                }

                // 3. 建立 TrustManager
                val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
                    init(keyStore)
                }

                val sslContext = SSLContext.getInstance("TLS").apply {
                    init(null, tmf.trustManagers, null)
                }

                builder.sslSocketFactory(sslContext.socketFactory, tmf.trustManagers[0] as X509TrustManager)
            } catch (e: Exception) {
                // 如果沒找到 cert.pem 或失敗，會退回到系統預設驗證（適用於 ngrok 正式憑證）
                e.printStackTrace()
            }
        }

        val client = builder.build()
        httpClientRef.set(client)
        return client
    }

    fun webSocketClient(): OkHttpClient {
        wsClientRef.get()?.let { return it }
        val client = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
        wsClientRef.set(client)
        return client
    }
}
