package org.ozinger.ika.serialization

import io.mockk.every
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.test.mock.declareMock
import org.ozinger.ika.AbstractTest
import org.ozinger.ika.definition.ModeDefinition
import org.ozinger.ika.definition.ServerId
import org.ozinger.ika.definition.UniversalUserId
import org.ozinger.ika.networking.Serializers
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class SerializationTests : AbstractTest() {
    @BeforeEach
    fun setUp() {
        declareMock<ModeDefs> {
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

        expectThat(encoded).isEqualTo(value)
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

        expectThat(encoded).isEqualTo(value)
    }

    @Test
    fun `can recognize direct sender`() {
        val value = "CAPAB START 1202"
        val packet = Serializers.decodePacketFromString(value)

        expectThat(packet.sender).isNull()
    }

    @Test
    fun `can recognize server sender`() {
        val value = ":2KA FMODE #test 1234 +i"
        val packet = Serializers.decodePacketFromString(value)

        expectThat(packet.sender).isA<ServerId>()
    }

    @Test
    fun `can recognize user sender`() {
        val value = ":012AAAAAA AWAY :auto-away"
        val packet = Serializers.decodePacketFromString(value)

        expectThat(packet.sender).isA<UniversalUserId>()
    }

    @Test
    fun `can recognize unacceptable sender`() {
        val value = ":#channel AWAY :auto-away"

        expectThrows<SerializationException> { Serializers.decodePacketFromString(value) }
            .get { message }.isEqualTo("Only ServerId and UniversalUserId or null can be Packet.sender")
    }

    @Test
    fun `can recognize invalid identifier`() {
        val value = ":1234 AWAY"

        expectThrows<SerializationException> { Serializers.decodePacketFromString(value) }
            .get { message }.isEqualTo("Invalid identifier")
    }
}