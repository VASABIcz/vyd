package database.servicies.guilds

import ifLaunch
import websockets.DispatcherService

class EventGuildMemberService(private val base: GuildMemberService, private val dispatcher: DispatcherService) :
    GuildMemberService {
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

    override suspend fun getMember(user: Int, guild: Int): GuildMember? = base.getMember(user, guild)

    override suspend fun getMembers(guild: Int, amount: Int, offset: Int): List<GuildMember> =
        base.getMembers(guild, amount, offset)

    override suspend fun getGuilds(user: Int): List<GuildMember> = base.getGuilds(user)
}