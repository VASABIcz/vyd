package database.servicies.guilds

import ifLaunch
import websockets.DispatcherService

class EventGuildMemberService(private val base: GuildMemberService, private val dispatcher: DispatcherService) :
    GuildMemberService by base {
    override suspend fun joinGuild(user: Int, guild: Int): Boolean = base.joinGuild(user, guild).ifLaunch {
        dispatcher.guildJoin(guild, user)
    }

    override suspend fun leaveGuild(user: Int, guild: Int): Boolean = base.leaveGuild(user, guild).ifLaunch {
        dispatcher.guildLeave(guild, user)
    }

    override suspend fun changeNick(user: Int, guild: Int, nick: String): Boolean =
        base.changeNick(user, guild, nick).ifLaunch {
            dispatcher.guildChangeNick(guild, user, nick)
        }
}