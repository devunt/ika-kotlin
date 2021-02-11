@file:UseSerializers(DurationSerializer::class, LocalDateTimeSerializer::class)

package org.ozinger.ika.command

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ozinger.ika.definition.*
import org.ozinger.ika.serialization.serializer.DurationSerializer
import org.ozinger.ika.serialization.serializer.LocalDateTimeSerializer
import org.ozinger.ika.serialization.serializer.ModeModificationSerializer
import org.ozinger.ika.state.ModeDefinitions
import java.time.Duration
import java.time.LocalDateTime

object ChannelModeModificationSerializer : ModeModificationSerializer(ModeDefinitions::channel)
object UserModeModificationSerializer : ModeModificationSerializer(ModeDefinitions::user)

@Serializable
sealed class Command {
    override fun toString() = this::class.simpleName!!
}

@Serializable
data class UnknownCommand(val command: String, val param: String) : Command()


///////////////////////// Direct Commands /////////////////////////
@Serializable
@SerialName("CAPAB")
data class CAPAB(val type: CapabilityType, val value: String? = null) : Command()


///////////////////////// Direct & Server Commands //////////////////////////
@Serializable
@SerialName("SERVER")
data class SERVER(
    val name: String,
    val password: String,
    val distance: Int,
    val sid: ServerId,
    val description: String
) : Command()

@Serializable
@SerialName("ERROR")
data class ERROR(val reason: String) : Command()


///////////////////////// Server Commands /////////////////////////
@Serializable
@SerialName("BURST")
data class BURST(val timestamp: LocalDateTime) : Command()

@Serializable
@SerialName("ENDBURST")
object ENDBURST : Command()

@Serializable
@SerialName("SQUIT")
data class SQUIT(val quittingServerId: ServerId, val reason: String) : Command()

@Serializable
@SerialName("VERSION")
data class VERSION(val version: String) : Command()

@Serializable
@SerialName("PING")
data class PING(val p1: String, val p2: String) : Command()

@Serializable
@SerialName("PONG")
data class PONG(val p2: String, val p1: String) : Command()

@Serializable
@SerialName("ADDLINE")
data class ADDLINE(
    val type: XLineType, val mask: String, val setter: String,
    val addedAt: LocalDateTime, val duration: Duration, val reason: String
) : Command()

@Serializable
@SerialName("DELLINE")
data class DELLINE(val type: XLineType, val mask: String) : Command()

@Serializable
@SerialName("ENCAP")
data class ENCAP(val targetServerId: ServerId, val textCommand: String) : Command()

@Serializable
@SerialName("PUSH")
data class PUSH(val targetUserId: UniversalUserId, val textCommand: String) : Command()

@Serializable
@SerialName("UID")
data class UID(
    val userId: UniversalUserId, val timestamp: LocalDateTime, val nickname: String,
    val host: String, val displayedHost: String, val ident: String,
    val ipAddress: String, val signonAt: LocalDateTime,
    @Serializable(with = UserModeModificationSerializer::class) val modeModification: ModeModification,
    val realname: String
) : Command()

@Serializable
@SerialName("SVSNICK")
data class SVSNICK(val targetUserId: UniversalUserId, val nickname: String, val timestamp: LocalDateTime) : Command()

@Serializable
@SerialName("FJOIN")
data class FJOIN(
    val channelName: ChannelName,
    val timestamp: LocalDateTime,
    @Serializable(with = ChannelModeModificationSerializer::class) val modeModification: ModeModification,
    val members: String
) :
    Command()

@Serializable
@SerialName("FTOPIC")
data class FTOPIC(val channelName: ChannelName, val addedAt: LocalDateTime, val setter: String, val content: String) :
    Command()


///////////////////////// Server & User Commands /////////////////////////
@Serializable
@SerialName("METADATA")
data class METADATA(val target: Identifier, val type: String, val value: String? = null) : Command()

@Serializable
@SerialName("FMODE")
data class FMODE(
    val target: Identifier, val timestamp: LocalDateTime,
    @Contextual val modeModification: ModeModification
) : Command()


///////////////////////// User Commands /////////////////////////
@Serializable
@SerialName("AWAY")
data class AWAY(val reason: String? = null) : Command()

@Serializable
@SerialName("OPERTYPE")
data class OPERTYPE(val type: OperType) : Command()

@Serializable
@SerialName("IDLE")
data class IDLE(
    val targetUserId: UniversalUserId,
    val signonAt: LocalDateTime? = null,
    val duration: Duration? = null
) : Command()

@Serializable
@SerialName("FHOST")
data class FHOST(val displayedHost: String) : Command()

@Serializable
@SerialName("FNAME")
data class FNAME(val realname: String) : Command()

@Serializable
@SerialName("NICK")
data class NICK(val nickname: String, val timestamp: LocalDateTime) : Command()

@Serializable
@SerialName("MODE")
data class MODE(
    val targetUserId: UniversalUserId,
    @Serializable(with = UserModeModificationSerializer::class) val modeModification: ModeModification
) : Command()

@Serializable
@SerialName("TOPIC")
data class TOPIC(val channelName: ChannelName, val content: String) : Command()

@Serializable
@SerialName("KICK")
data class KICK(val channelName: ChannelName, val targetUserId: UniversalUserId, val reason: String? = null) : Command()

@Serializable
@SerialName("PART")
data class PART(val channelName: ChannelName, val reason: String? = null) : Command()

@Serializable
@SerialName("QUIT")
data class QUIT(val reason: String? = null) : Command()