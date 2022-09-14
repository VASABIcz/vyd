package database.servicies.guilds

interface GuildMemberService {
    suspend fun joinGuild(user: Int, guild: Int): Boolean

    suspend fun leaveGuild(user: Int, guild: Int): Boolean

    suspend fun changeNick(user: Int, guild: Int, nick: String): Boolean

    suspend fun getMember(user: Int, guild: Int): GuildMember?

    suspend fun getMember(member: Int): GuildMember?
    suspend fun getMembers(guild: Int, amount: Int = 50, offset: Int = 0): List<GuildMember>

    suspend fun getGuilds(user: Int): List<GuildMember>

    suspend fun getGuilds(user: Int, user1: Int): List<Int>
}