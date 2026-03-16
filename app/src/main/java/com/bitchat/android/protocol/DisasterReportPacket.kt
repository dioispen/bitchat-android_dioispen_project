package com.bitchat.android.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Health assessment data structure
 */
data class HealthAssessment(
    val consciousness: Int, // 0: Alert, 1: Voice, 2: Pain, 3: Unresponsive
    val bleeding: Int,      // 0: None, 1: Minor, 2: Moderate, 3: Severe
    val breathing: Int      // 0: Normal, 1: Labored, 2: Rapid, 3: Absent
)

/**
 * Structured disaster report payload
 */
data class DisasterReportPayload(
    val messageID: String,
    val timestamp: Long,
    val severity: Int, // 0: Normal, 1: High, 2: Critical
    val latitude: Double,
    val longitude: Double,
    val health: HealthAssessment,
    val otherMessage: String
) {
    fun encode(): ByteArray {
        val msgBytes = otherMessage.toByteArray(Charsets.UTF_8)
        val idBytes = messageID.toByteArray(Charsets.UTF_8)
        
        // Size: ID length (1) + ID + TS (8) + Sev (1) + GPS (16) + Health (3) + Msg length (4) + Msg
        val buffer = ByteBuffer.allocate(1 + idBytes.size + 8 + 1 + 16 + 3 + 4 + msgBytes.size)
            .apply { order(ByteOrder.BIG_ENDIAN) }
        
        buffer.put(idBytes.size.toByte())
        buffer.put(idBytes)
        buffer.putLong(timestamp)
        buffer.put(severity.toByte())
        buffer.putDouble(latitude)
        buffer.putDouble(longitude)
        buffer.put(health.consciousness.toByte())
        buffer.put(health.bleeding.toByte())
        buffer.put(health.breathing.toByte())
        buffer.putInt(msgBytes.size)
        buffer.put(msgBytes)
        
        return buffer.array()
    }

    companion object {
        fun decode(data: ByteArray): DisasterReportPayload? {
            try {
                val buffer = ByteBuffer.wrap(data).apply { order(ByteOrder.BIG_ENDIAN) }
                
                val idLength = buffer.get().toInt() and 0xFF
                val idBytes = ByteArray(idLength)
                buffer.get(idBytes)
                val messageID = String(idBytes, Charsets.UTF_8)
                
                val timestamp = buffer.getLong()
                val severity = buffer.get().toInt()
                val latitude = buffer.getDouble()
                val longitude = buffer.getDouble()
                
                val health = HealthAssessment(
                    consciousness = buffer.get().toInt(),
                    bleeding = buffer.get().toInt(),
                    breathing = buffer.get().toInt()
                )
                
                val msgLength = buffer.getInt()
                val msgBytes = ByteArray(msgLength)
                buffer.get(msgBytes)
                val otherMessage = String(msgBytes, Charsets.UTF_8)
                
                return DisasterReportPayload(
                    messageID = messageID,
                    timestamp = timestamp,
                    severity = severity,
                    latitude = latitude,
                    longitude = longitude,
                    health = health,
                    otherMessage = otherMessage
                )
            } catch (e: Exception) {
                return null
            }
        }
    }
}
