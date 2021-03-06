package org.ozinger.ika.handler.core

import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import org.ozinger.ika.AbstractPacketTest
import org.ozinger.ika.command.*
import org.ozinger.ika.definition.ModeDefinition
import org.ozinger.ika.enumeration.CapabilityType
import org.ozinger.ika.networking.Packet
import org.ozinger.ika.serialization.ModeDefs
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.withElementAt

class HandshakeHandlerTests : AbstractPacketTest() {
    private val modeDefs: ModeDefs by inject()

    @Test
    fun handshake() {
        assumeAsReceived(
            Packet(
                null, SERVER(
                    name = configuration.link.name,
                    password = configuration.link.password,
                    distance = 0,
                    sid = remoteServerId,
                    description = ""
                )
            )
        ) {
            expectThat(it)
                .hasSize(3)
                .withElementAt(0) { get(Packet::command).isA<BURST>() }
                .withElementAt(1) { get(Packet::command).isA<VERSION>() }
                .withElementAt(2) { get(Packet::command).isA<ENDBURST>() }
        }
    }

    @Test
    fun `handle capability`() {
        assumeAsReceivedSequencially(
            Packet(null, CAPAB(CapabilityType.START, "1202")),
            Packet(
                null,
                CAPAB(
                    CapabilityType.MODULES,
                    "m_banexception.so,m_banredirect.so,m_blockcolor.so,m_chanprotect.so,m_chghost.so,m_chgident.so,m_cloaking.so,m_commonchans.so,m_globalload.so,m_hidechans.so,m_inviteexception.so,m_joinflood.so,m_kicknorejoin.so,m_knock.so,m_messageflood.so,m_nationalchars.so,m_noctcp.so,m_nokicks.so,m_nonicks.so,m_nonotice.so,m_operchans.so,m_operinvex.so,m_rdownload.so,m_redirect.so,m_services_account.so,m_servprotect.so,m_sslmodes.so,m_stripcolor.so,m_svshold.so,m_timedbans.so,m_uninvite.so"
                )
            ),
            Packet(
                null,
                CAPAB(
                    CapabilityType.MODULES_SUPPORT,
                    "m_check.so,m_chghost.so,m_knock.so,m_services_account.so,m_uninvite.so"
                )
            ),
            Packet(
                null,
                CAPAB(
                    CapabilityType.CHANNEL_MODES,
                    "admin=&a ban=b banexception=e blockcolor=c c_registered=r flood=f founder=~q halfop=%h history=H invex=I inviteonly=i joinflood=j key=k kicknorejoin=J limit=l moderated=m noctcp=C noextmsg=n nokick=Q noknock=K nonick=N nonotice=T op=@o operonly=O private=p redirect=L reginvite=R regmoderated=M secret=s sslonly=z stripcolor=S stripcolorex=Z topiclock=t voice=+v"
                )
            ),
            Packet(
                null,
                CAPAB(
                    CapabilityType.USER_MODES,
                    "cloak=x deaf_commonchan=c hidechans=I invisible=i oper=o regdeaf=R servprotect=k snomask=s u_registered=r u_stripcolor=S u_stripcolorex=Z wallops=w"
                )
            ),
            Packet(
                null,
                CAPAB(
                    CapabilityType.CAPABILITIES,
                    "NICKMAX=31 CHANMAX=64 MAXMODES=20 IDENTMAX=11 MAXQUIT=255 MAXTOPIC=307 MAXKICK=255 MAXGECOS=128 MAXAWAY=200 IP6SUPPORT=1 PROTOCOL=1202 CHALLENGE=UNWl[}sRZjFwXTrXKFkF HALFOP=1 PREFIX=(qaohv)~&@%+ CHANMODES=Ibe,k,HJLfjl,CKMNOQRSTZcimnprstz USERMODES=,,s,IRSZcikorwx SVSPART=1"
                )
            ),
            Packet(null, CAPAB(CapabilityType.END)),
        )

        expectThat(modeDefs) {
            get(ModeDefs::channel).isEqualTo(ModeDefinition("Ibe,k,HJLfjl,CKMNOQRSTZcimnprstz"))
            get(ModeDefs::user).isEqualTo(ModeDefinition(",,s,IRSZcikorwx"))
            get(ModeDefs::member).isEqualTo(ModeDefinition(",qaohv,,"))
        }
    }
}