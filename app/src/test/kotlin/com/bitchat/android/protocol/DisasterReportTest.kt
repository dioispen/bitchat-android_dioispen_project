package com.bitchat.android.protocol

import org.junit.Assert.*
import org.junit.Test

class DisasterReportTest {

    @Test
    fun testPayloadEncodingAndDecoding() {
        // 1. Prepare sample data
        val originalHealth = HealthAssessment(
            consciousness = 0, // Alert
            bleeding = 2,      // Moderate
            breathing = 1      // Labored
        )
        
        val originalPayload = DisasterReportPayload(
            messageID = "TEST-ID-999",
            timestamp = 1625097600000L,
            severity = 2, // Critical
            latitude = 25.0330,
            longitude = 121.5654,
            health = originalHealth,
            otherMessage = "Need immediate assistance and water."
        )

        // 2. Encode to binary
        val encoded = originalPayload.encode()
        assertNotNull(encoded)
        assertTrue(encoded.size > 0)

        // 3. Decode back to object
        val decoded = DisasterReportPayload.decode(encoded)
        
        // 4. Verify all fields match
        assertNotNull(decoded)
        assertEquals(originalPayload.messageID, decoded?.messageID)
        assertEquals(originalPayload.timestamp, decoded?.timestamp)
        assertEquals(originalPayload.severity, decoded?.severity)
        assertEquals(originalPayload.latitude, decoded?.latitude)
        assertEquals(originalPayload.longitude, decoded?.longitude)
        assertEquals(originalPayload.health.consciousness, decoded?.health?.consciousness)
        assertEquals(originalPayload.health.bleeding, decoded?.health?.bleeding)
        assertEquals(originalPayload.health.breathing, decoded?.health?.breathing)
        assertEquals(originalPayload.otherMessage, decoded?.otherMessage)
    }

    @Test
    fun testBitchatPacketIntegration() {
        val payload = DisasterReportPayload(
            messageID = "MSG-001",
            timestamp = System.currentTimeMillis(),
            severity = 1,
            latitude = 25.0,
            longitude = 121.0,
            health = HealthAssessment(0, 0, 0),
            otherMessage = "Relay test"
        )
        
        val senderID = ByteArray(8) { 0x01.toByte() }
        val encodedPayload = payload.encode()
        
        // Create a BitchatPacket containing the disaster report
        // Using Type 0x30 for DISASTER_REPORT as defined in MessageType
        val packet = BitchatPacket(
            version = 1u,
            type = MessageType.DISASTER_REPORT.value,
            senderID = senderID,
            recipientID = null, // Broadcast
            timestamp = System.currentTimeMillis().toULong(),
            payload = encodedPayload,
            ttl = 7u
        )

        // Use toInt() to avoid UByte boxing issues with JUnit's assertEquals(Object, Object)
        assertEquals(0x30, MessageType.DISASTER_REPORT.value.toInt())
        assertEquals(MessageType.DISASTER_REPORT.value.toInt(), packet.type.toInt())

        // Decode inner disaster payload
        val decodedDisaster = DisasterReportPayload.decode(packet.payload)
        assertNotNull(decodedDisaster)
        assertEquals("MSG-001", decodedDisaster?.messageID)
        assertEquals(1, decodedDisaster?.severity)

        // Verify formatted content string (as used in MessageHandler)
        val expectedContentPrefix = "[DISASTER REPORT]"
        val formattedContent = "[DISASTER REPORT] Severity: ${decodedDisaster?.severity}\nLocation: ${decodedDisaster?.latitude}, ${decodedDisaster?.longitude}"
        assertTrue(formattedContent.startsWith(expectedContentPrefix))
    }

    @Test
    fun testEdgeCaseEmptyMessage() {
        val payload = DisasterReportPayload(
            messageID = "ID",
            timestamp = 0L,
            severity = 0,
            latitude = -90.0,
            longitude = 180.0,
            health = HealthAssessment(3, 3, 3),
            otherMessage = ""
        )
        
        val encoded = payload.encode()
        val decoded = DisasterReportPayload.decode(encoded)
        
        assertEquals("", decoded?.otherMessage)
        assertEquals(-90.0, decoded?.latitude ?: 0.0, 0.0)
    }

    @Test
    fun testInvalidDataDecoding() {
        val junkData = byteArrayOf(0x01, 0x02, 0x03)
        val decoded = DisasterReportPayload.decode(junkData)
        assertNull(decoded)
    }
}
