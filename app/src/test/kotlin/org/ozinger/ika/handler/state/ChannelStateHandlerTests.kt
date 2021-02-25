package org.ozinger.ika.handler.state

import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import org.ozinger.ika.AbstractPacketTest
import org.ozinger.ika.command.*
import org.ozinger.ika.definition.*
import org.ozinger.ika.networking.Packet
import org.ozinger.ika.store.ChannelStore
import org.ozinger.ika.store.UserStore
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.*
import java.time.LocalDateTime
import java.time.ZoneOffset

class ChannelStateHandlerTests : AbstractPacketTest() {
    private val userStore: UserStore by inject()
    private val channelStore: ChannelStore by inject()
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

    private fun createChannel1(): Channel {
        val user1 = connectLocalUser1()

        assumeAsReceived(
            Packet(
                configuration.server.id, FJOIN(
                    channelName = channel1Name,
                    timestamp = timestamp,
                    channelModeModification = ModeModification(),
                    memberModeModification = ModeModification(adding = setOf(MemberMode(user1.id))),
                )
            )
        )

        return channelStore.get(channel1Name)
    }

    private fun createChannel2(): Channel {
        val user1 = connectLocalUser1()
        val user2 = connectRemoteUser2()

        assumeAsReceived(
            Packet(
                configuration.server.id, FJOIN(
                    channelName = channel2Name,
                    timestamp = timestamp,
                    channelModeModification = ModeModification(adding = setOf(Mode('n'))),
                    memberModeModification = ModeModification(
                        adding = setOf(
                            MemberMode(user1.id, 'q'),
                            MemberMode(user2.id)
                        )
                    ),
                )
            )
        )

        return channelStore.get(channel2Name)
    }

    @Test
    fun `channel1 created`() {
        val expected = Channel(
            name = channel1Name,
            timestamp = timestamp,
        )
        val actual = createChannel1()

        expectThat(actual) {
            isEqualTo(expected)
            get(Channel::members)
                .hasSize(1)
        }
    }

    @Test
    fun `user2 joined channel1`() {
        val channel1 = createChannel1()
        val user2 = connectRemoteUser2()

        assumeAsReceived(
            Packet(
                remoteServerId, FJOIN(
                    channelName = channel1.name,
                    timestamp = channel1.timestamp,
                    channelModeModification = ModeModification(),
                    memberModeModification = ModeModification(adding = setOf(MemberMode(user2.id))),
                )
            )
        )

        expectThat(channel1.members)
            .hasSize(2)
            .containsKey(user2.id)
    }

    @Test
    fun `user2 joined channel1 with op and voice`() {
        val channel1 = createChannel1()
        val user2 = connectRemoteUser2()

        assumeAsReceived(
            Packet(
                remoteServerId, FJOIN(
                    channelName = channel1.name,
                    timestamp = channel1.timestamp,
                    channelModeModification = ModeModification(),
                    memberModeModification = ModeModification(
                        adding = setOf(
                            MemberMode(user2.id, 'o'),
                            MemberMode(user2.id, 'v')
                        )
                    ),
                )
            )
        )

        expectThat(channel1.members)
            .hasSize(2)
            .getValue(user2.id)
            .get(Member::modes)
            .containsExactlyInAnyOrder(MemberMode(user2.id, 'o'), MemberMode(user2.id, 'v'))
    }

    @Test
    fun `user1 changed channel1's mode`() {
        val channel1 = createChannel1()

        assumeAsReceived(
            Packet(
                localUser1Id, FMODE(
                    target = channel1.name,
                    timestamp = channel1.timestamp,
                    modeModification = ModeModification(adding = setOf(Mode('n'), Mode('k', "1234"))),
                )
            )
        )

        expectThat(channel1.modes)
            .hasSize(2)
            .containsExactlyInAnyOrder(Mode('n'), Mode('k', "1234"))
    }

    @Test
    fun `user1 gives user2 op and voice and removes voice on channel2`() {
        val channel2 = createChannel2()

        assumeAsReceived(
            Packet(
                remoteServerId, FMODE(
                    target = channel2.name,
                    timestamp = channel2.timestamp,
                    modeModification = ModeModification(
                        adding = setOf(
                            MemberMode(remoteUser2Id, 'o'),
                            MemberMode(remoteUser2Id, 'v')
                        )
                    ),
                )
            )
        )

        expectThat(channel2.members)
            .getValue(remoteUser2Id)
            .get(Member::modes)
            .containsExactlyInAnyOrder(MemberMode(remoteUser2Id, 'o'), MemberMode(remoteUser2Id, 'v'))

        assumeAsReceived(
            Packet(
                remoteServerId, FMODE(
                    target = channel2.name,
                    timestamp = channel2.timestamp,
                    modeModification = ModeModification(removing = setOf(MemberMode(remoteUser2Id, 'v'))),
                )
            )
        )

        expectThat(channel2.members)
            .getValue(remoteUser2Id)
            .get(Member::modes)
            .containsExactlyInAnyOrder(MemberMode(remoteUser2Id, 'o'))
    }

    @Test
    fun `channel1 metadata changed`() {
        val channel1 = createChannel1()

        assumeAsReceived(Packet(remoteServerId, METADATA(channel1.name, "test-key", "test_value")))
        expectThat(channel1.metadata)
            .hasSize(1)
            .hasEntry("test-key", "test_value")

        assumeAsReceived(Packet(remoteServerId, METADATA(channel1.name, "test-key")))
        expectThat(channel1.metadata).isEmpty()
    }

    @Test
    fun `server sets channel1's topic`() {
        val channel1 = createChannel1()

        val ts = LocalDateTime.now(ZoneOffset.UTC)
        assumeAsReceived(Packet(remoteServerId, FTOPIC(channel1.name, ts, "test!test@1.2.3.4", "test topic")))
        expectThat(channel1.topic).isEqualTo(Channel.Topic("test topic", "test!test@1.2.3.4", ts))
    }

    @Test
    fun `user1 changed channel1's topic`() {
        val channel1 = createChannel1()

        assumeAsReceived(Packet(localUser1Id, TOPIC(channel1.name, "test topic")))
        expectThat(channel1.topic) {
            get(Channel.Topic::content).isEqualTo("test topic")
            get(Channel.Topic::setter).isEqualTo("User1!user1@1.1.1.1")
        }
    }

    @Test
    fun `user1 kicked user2 from channel2`() {
        val channel2 = createChannel2()

        assumeAsReceived(Packet(localUser1Id, KICK(channel2.name, remoteUser2Id)))
        expectThat(channel2.members).hasSize(1).containsKey(localUser1Id).not().containsKey(remoteUser2Id)
    }

    @Test
    fun `user2 parted from channel2`() {
        val channel2 = createChannel2()

        assumeAsReceived(Packet(remoteUser2Id, PART(channel2.name)))
        expectThat(channel2.members).hasSize(1).containsKey(localUser1Id).not().containsKey(remoteUser2Id)
    }

    @Test
    fun `user1 and user2 parted from channel2`() {
        val channel2 = createChannel2()

        assumeAsReceived(Packet(localUser1Id, PART(channel2.name)))
        assumeAsReceived(Packet(remoteUser2Id, PART(channel2.name)))
        expect {
            that(channel2.members).isEmpty()
            that(channelStore.exists(channel2.name)).isFalse()
        }
    }

    @Test
    fun `user1 parted from channel2 and user2 quitted`() {
        val channel2 = createChannel2()

        assumeAsReceived(Packet(localUser1Id, PART(channel2.name)))
        assumeAsReceived(Packet(remoteUser2Id, QUIT()))

        expect {
            that(channel2.members).isEmpty()
            that(channelStore.exists(channel2.name)).isFalse()
        }
    }

    @Test
    fun `user1 parted from channel2 and server net-splitted`() {
        val channel2 = createChannel2()

        assumeAsReceived(Packet(localUser1Id, PART(channel2.name)))
        assumeAsReceived(Packet(remoteServerId, SQUIT(remoteServerId, "")))

        expect {
            that(channel2.members).isEmpty()
            that(channelStore.exists(channel2.name)).isFalse()
        }
    }
}