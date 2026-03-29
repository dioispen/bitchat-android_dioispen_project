package com.bitchat.android.net

import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Centralized OkHttp provider using Cloudflare (HTTP/2 and HTTP/1.1) for network traffic.
 * Replaces the previous Tor-based implementation.
 */
object OkHttpProvider {
    private val httpClientRef = AtomicReference<OkHttpClient?>(null)
    private val wsClientRef = AtomicReference<OkHttpClient?>(null)

    fun reset() {
        httpClientRef.set(null)
        wsClientRef.set(null)
    }

    fun httpClient(): OkHttpClient {
        httpClientRef.get()?.let { return it }
        val client = baseBuilder()
            .callTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        httpClientRef.set(client)
        return client
    }

    fun webSocketClient(): OkHttpClient {
        wsClientRef.get()?.let { return it }
        val client = baseBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
        wsClientRef.set(client)
        return client
    }

    private fun baseBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            // 使用穩定版協議，避免 HTTP_3 造成的編譯錯誤
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
    }
}
