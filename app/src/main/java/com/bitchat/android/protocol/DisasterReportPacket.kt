package com.bitchat.android.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Health Report Payload - Synchronized with Flutter health_report.dart
 */
data class HealthReportPayload(
    val reporterId: String,   // 回報者 ID
    val name: String,         // 姓名
    val phone: String,        // 手機
    val bloodType: String?,   // 血型
    val status: String,       // '安全' / '輕傷' / '重傷'
    val description: String?, // 補充說明
    val lat: Double?,         // 緯度
    val lng: Double?,         // 經度
    val reportTime: String    // ISO8601 時間字串
) {
    fun encode(): ByteArray {
        // 使用簡單的字串長度編碼或 JSON。考量到與 Flutter 對接方便，這裡採用與 Flutter 相同的欄位結構。
        // 為求效能與穩定，我們定義一個二進位格式：
        val reporterIdBytes = reporterId.toByteArray()
        val nameBytes = name.toByteArray()
        val phoneBytes = phone.toByteArray()
        val bloodTypeBytes = bloodType?.toByteArray() ?: ByteArray(0)
        val statusBytes = status.toByteArray()
        val descriptionBytes = description?.toByteArray() ?: ByteArray(0)
        val reportTimeBytes = reportTime.toByteArray()

        val size = 1 + reporterIdBytes.size + 
                   1 + nameBytes.size + 
                   1 + phoneBytes.size + 
                   1 + bloodTypeBytes.size + 
                   1 + statusBytes.size + 
                   2 + descriptionBytes.size + 
                   16 + // Lat(8) + Lng(8)
                   1 + reportTimeBytes.size

        val buffer = ByteBuffer.allocate(size).apply { order(ByteOrder.BIG_ENDIAN) }
        
        // Write Fields
        buffer.put(reporterIdBytes.size.toByte())
        buffer.put(reporterIdBytes)
        
        buffer.put(nameBytes.size.toByte())
        buffer.put(nameBytes)
        
        buffer.put(phoneBytes.size.toByte())
        buffer.put(phoneBytes)
        
        buffer.put(bloodTypeBytes.size.toByte())
        buffer.put(bloodTypeBytes)
        
        buffer.put(statusBytes.size.toByte())
        buffer.put(statusBytes)
        
        buffer.putShort(descriptionBytes.size.toShort())
        buffer.put(descriptionBytes)
        
        buffer.putDouble(lat ?: 0.0)
        buffer.putDouble(lng ?: 0.0)
        
        buffer.put(reportTimeBytes.size.toByte())
        buffer.put(reportTimeBytes)
        
        return buffer.array()
    }

    companion object {
        fun decode(data: ByteArray): HealthReportPayload? {
            try {
                val buffer = ByteBuffer.wrap(data).apply { order(ByteOrder.BIG_ENDIAN) }
                
                val ridLen = buffer.get().toInt() and 0xFF
                val reporterId = String(ByteArray(ridLen).also { buffer.get(it) })
                
                val nLen = buffer.get().toInt() and 0xFF
                val name = String(ByteArray(nLen).also { buffer.get(it) })
                
                val pLen = buffer.get().toInt() and 0xFF
                val phone = String(ByteArray(pLen).also { buffer.get(it) })
                
                val btLen = buffer.get().toInt() and 0xFF
                val bloodType = if (btLen > 0) String(ByteArray(btLen).also { buffer.get(it) }) else null
                
                val sLen = buffer.get().toInt() and 0xFF
                val status = String(ByteArray(sLen).also { buffer.get(it) })
                
                val dLen = buffer.getShort().toInt() and 0xFFFF
                val description = if (dLen > 0) String(ByteArray(dLen).also { buffer.get(it) }) else null
                
                val latVal = buffer.getDouble()
                val lngVal = buffer.getDouble()
                val lat = if (latVal == 0.0) null else latVal
                val lng = if (lngVal == 0.0) null else lngVal
                
                val tLen = buffer.get().toInt() and 0xFF
                val reportTime = String(ByteArray(tLen).also { buffer.get(it) })
                
                return HealthReportPayload(
                    reporterId, name, phone, bloodType, status, description, lat, lng, reportTime
                )
            } catch (e: Exception) {
                return null
            }
        }
    }
}
