package org.ozinger.ika.test

import io.mockk.every
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.test.mock.declareMock
import org.ozinger.ika.definition.ModeDefinition
import org.ozinger.ika.definition.ServerId
import org.ozinger.ika.definition.UniversalUserId
import org.ozinger.ika.networking.Serializers
import org.ozinger.ika.state.ModeDefinitionProvider

class SerializationTests : AbstractTest() {
    @BeforeEach
    fun init() {
        declareMock<ModeDefinitionProvider> {
            every { channel } returns ModeDefinition("Ibeg,k,FJLfjl,ABCDGKMNOPQRSTcimnpstu")
            every { user } returns ModeDefinition(",,s,IRSZcikorwx")
            every { member } returns ModeDefinition(",qaohv,,")
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            ":012 FJOIN #test 1234 +Snstk 1234 :ov,012AAAAAA qaov,012AAAAAB",
            ":2KA FMODE #test 1234 +kKj secret 10:5",
            ":012 FMODE #test 1234 +bb baduser!*@* *!*@*.badhost.com",
        ]
    )
    fun `can handle various modes`(value: String) {
        val decoded = Serializers.decodePacketFromString(value)
        val encoded = Serializers.encodePacketToString(decoded)

        assertEquals(value, encoded)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "CAPAB START 1202",
            ":2KA FMODE #test 1234 +i",
            ":012AAAAAA AWAY :auto away",
        ]
    )
    fun `can handle various senders`(value: String) {
        val decoded = Serializers.decodePacketFromString(value)
        val encoded = Serializers.encodePacketToString(decoded)

        assertEquals(value, encoded)
    }

    @Test
    fun `can recognize direct sender`() {
        val value = "CAPAB START 1202"
        val packet = Serializers.decodePacketFromString(value)

        assertNull(packet.sender)
    }

    @Test
    fun `can recognize server sender`() {
        val value = ":2KA FMODE #test 1234 +i"
        val packet = Serializers.decodePacketFromString(value)

        assertEquals(packet.sender!!::class, ServerId::class)
    }

    @Test
    fun `can recognize user sender`() {
        val value = ":012AAAAAA AWAY :auto-away"
        val packet = Serializers.decodePacketFromString(value)

        assertEquals(packet.sender!!::class, UniversalUserId::class)
    }

    @Test
    fun `can recognize unacceptable sender`() {
        val value = ":#channel AWAY :auto-away"

        val ex = assertThrows<SerializationException> { Serializers.decodePacketFromString(value) }
        assertEquals("Only ServerId and UniversalUserId or null can be Packet.sender", ex.message)
    }

    @Test
    fun `can recognize invalid identifier`() {
        val value = ":1234 AWAY"

        val ex = assertThrows<SerializationException> { Serializers.decodePacketFromString(value) }
        assertEquals("Invalid identifier", ex.message)
    }
}