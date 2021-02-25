package org.ozinger.ika.handler.state

import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import org.ozinger.ika.AbstractPacketTest
import org.ozinger.ika.command.*
import org.ozinger.ika.definition.Mode
import org.ozinger.ika.definition.ModeModification
import org.ozinger.ika.definition.User
import org.ozinger.ika.enumeration.OperType
import org.ozinger.ika.networking.Packet
import org.ozinger.ika.store.UserStore
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

class UserStateHandlerTests : AbstractPacketTest() {
    private val userStore: UserStore by inject()
    private val timestamp = LocalDateTime.now(ZoneOffset.UTC)

    private fun connectLocalUser1(): User {
        assumeAsReceived(
            Packet(
                configuration.server.id, UID(
                    userId = localUser1Id,
                    timestamp = timestamp,
                    nickname = "User1",
                    host = "1.1.1.1",
                    displayedHost = "1.1.1.1",
                    ident = "user1",
                    ipAddress = "1.1.1.1",
                    signonAt = timestamp,
                    modeModification = ModeModification(),
                    realname = "User #1"
                )
            )
        )
        return userStore.get(localUser1Id)
    }

    private fun connectRemoteUser2(): User {
        assumeAsReceived(
            Packet(
                remoteServerId, UID(
                    userId = remoteUser2Id,
                    timestamp = timestamp,
                    nickname = "User2",
                    host = "2.2.2.2",
                    displayedHost = "2.2.2.2",
                    ident = "user2",
                    ipAddress = "2.2.2.2",
                    signonAt = timestamp,
                    modeModification = ModeModification(),
                    realname = "User #2"
                )
            )
        )
        return userStore.get(remoteUser2Id)
    }

    @Test
    fun `local user1 connected`() {
        val expected = User(
            id = localUser1Id,
            timestamp = timestamp,
            nickname = "User1",
            host = "1.1.1.1",
            displayedHost = "1.1.1.1",
            ident = "user1",
            ipAddress = "1.1.1.1",
            signonAt = timestamp,
            realname = "User #1"
        )
        val actual = connectLocalUser1()

        expectThat(actual)
            .isEqualTo(expected)
            .get(User::isLocal).isTrue()
    }

    @Test
    fun `remote user2 connected`() {
        val expected = User(
            id = remoteUser2Id,
            timestamp = timestamp,
            nickname = "User2",
            host = "2.2.2.2",
            displayedHost = "2.2.2.2",
            ident = "user2",
            ipAddress = "2.2.2.2",
            signonAt = timestamp,
            realname = "User #2"
        )
        val actual = connectRemoteUser2()

        expectThat(actual)
            .isEqualTo(expected)
            .get(User::isLocal).isFalse()
    }

    @Test
    fun `user1 mask check`() {
        val user1 = connectLocalUser1()
        expectThat(user1.mask).isEqualTo("User1!user1@1.1.1.1")
    }

    @Test
    fun `user1 nickname changed`() {
        val user1 = connectLocalUser1()
        assumeAsReceived(Packet(user1.id, NICK("User1-1", timestamp)))
        expectThat(user1.nickname).isEqualTo("User1-1")
    }

    @Test
    fun `user1 displayedHost changed`() {
        val user1 = connectLocalUser1()
        assumeAsReceived(Packet(user1.id, FHOST("1.1.1.2")))
        expectThat(user1.displayedHost).isEqualTo("1.1.1.2")
    }

    @Test
    fun `user1 realname changed`() {
        val user1 = connectLocalUser1()
        assumeAsReceived(Packet(user1.id, FNAME("User #1-1")))
        expectThat(user1.realname).isEqualTo("User #1-1")
    }

    @Test
    fun `user1 metadata changed`() {
        val user1 = connectLocalUser1()

        assumeAsReceived(Packet(remoteServerId, METADATA(user1.id, "accountname", "user_1")))
        expectThat(user1.metadata)
            .hasSize(1)
            .hasEntry("accountname", "user_1")

        assumeAsReceived(Packet(remoteServerId, METADATA(user1.id, "accountname")))
        expectThat(user1.metadata).isEmpty()
    }

    @Test
    fun `user1 away status changed`() {
        val user1 = connectLocalUser1()

        assumeAsReceived(Packet(user1.id, AWAY("auto-away")))
        expectThat(user1) {
            get(User::isOnAway).isTrue()
            get(User::awayReason).isEqualTo("auto-away")
        }

        assumeAsReceived(Packet(user1.id, AWAY()))
        expectThat(user1.isOnAway).isFalse()
    }

    @Test
    fun `user1 operator authenticated`() {
        val user1 = connectLocalUser1()

        expectThat(user1) {
            get(User::isOperator).isFalse()
            get(User::modes).doesNotContain(Mode('o'))
        }

        assumeAsReceived(Packet(user1.id, OPERTYPE(OperType.NETADMIN)))

        expectThat(user1) {
            get(User::isOperator).isTrue()
            get(User::modes).containsExactly(Mode('o'))
        }
    }

    @Test
    fun `user1 changed itself's mode`() {
        val user1 = connectLocalUser1()

        expectThat(user1.modes).isEmpty()

        assumeAsReceived(Packet(user1.id, MODE(user1.id, ModeModification(adding = setOf(Mode('x'))))))

        expectThat(user1.modes)
            .hasSize(1)
            .elementAt(0)
            .isEqualTo(Mode('x'))
    }

    @Test
    fun `user1 changed user2's mode`() {
        val user1 = connectLocalUser1()
        val user2 = connectRemoteUser2()

        expectThat(user2.modes).isEmpty()

        assumeAsReceived(Packet(user1.id, MODE(user2.id, ModeModification(adding = setOf(Mode('x'))))))

        expectThat(user2.modes)
            .hasSize(1)
            .elementAt(0)
            .isEqualTo(Mode('x'))
    }

    @Test
    fun `server changed user1's mode`() {
        val user1 = connectLocalUser1()

        expectThat(user1.modes).isEmpty()

        assumeAsReceived(
            Packet(
                remoteServerId,
                FMODE(user1.id, timestamp, ModeModification(adding = setOf(Mode('x'))))
            )
        )

        expectThat(user1.modes)
            .hasSize(1)
            .elementAt(0)
            .isEqualTo(Mode('x'))
    }

    @Test
    fun `nickname change requested`() {
        val user1 = connectLocalUser1()

        assumeAsReceived(Packet(remoteServerId, SVSNICK(user1.id, "user1-svsnick", timestamp))) {
            expectThat(it)
                .hasSize(1)
                .first()
                .isEqualTo(Packet(user1.id, NICK(user1.nickname, user1.timestamp)))
        }
    }

    @Test
    fun `user2 requested user1's idle time`() {
        val user1 = connectLocalUser1()
        val user2 = connectRemoteUser2()

        assumeAsReceived(Packet(user2.id, IDLE(user1.id))) {
            expectThat(it)
                .hasSize(1)
                .first()
                .isEqualTo(Packet(user1.id, IDLE(user2.id, user1.signonAt, Duration.ZERO)))
        }
    }

    @Test
    fun `user1 quitted`() {
        val user1 = connectLocalUser1()
        expectThat(userStore.exists(user1.id)).isTrue()
        assumeAsReceived(Packet(user1.id, QUIT()))
        expectThat(userStore.exists(user1.id)).isFalse()
    }

    @Test
    fun `server net-splitted`() {
        val user1 = connectLocalUser1()
        val user2 = connectRemoteUser2()
        assumeAsReceived(Packet(remoteServerId, SQUIT(remoteServerId, "")))
        expect {
            that(userStore.exists(user1.id)).isTrue()
            that(userStore.exists(user2.id)).isFalse()
        }
    }
}