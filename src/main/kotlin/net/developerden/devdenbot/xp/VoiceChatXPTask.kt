package net.developerden.devdenbot.xp

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.developerden.devdenbot.data.StatsUsers
import net.developerden.devdenbot.util.log
import net.developerden.devdenbot.util.scope
import net.dv8tion.jda.api.JDA
import java.util.*

/**
 * @author AlexL
 */
class VoiceChatXPTask(val jda: JDA) : TimerTask() {

    private val log by log()


    @OptIn(FlowPreview::class)
    override fun run() {
        scope.launch {
            jda.guilds.asFlow()
                .flatMapConcat { guild ->
                    guild.voiceChannels.asFlow().filterNot {
                        it == guild.afkChannel
                    }
                }
                .filter { it.members.size > 1 }
                .flatMapConcat { it.members.asFlow() }
                .filterNot { it.user.isBot }
                .filterNot { it.voiceState?.isMuted ?: true }
                .filterNot { it.voiceState?.isDeafened ?: true }
                .collect {
                    val user = StatsUsers.get(it.idLong)
                    val gained = (1..3).random()
                    user.addXP(gained.toLong())
                    log.debug {
                        "Gave ${it.user.name} $gained XP for being in voice channel ${it.voiceState?.channel?.name} (${it.voiceState?.channel?.id})"
                    }
                }
        }
    }
}
