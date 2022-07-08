package database.servicies.guilds

interface GuildMemberService {
    fun joinGuild(user: Int, guild: Int): Boolean

    fun leaveGuild(user: Int, guild: Int): Boolean

    fun changeNick(user: Int, guild: Int, nick: String): Boolean

    fun getMember(user: Int, guild: Int): DatabaseMember?

    fun getMembers(guild: Int, amount: Int = 50, offset: Int = 0): List<DatabaseMember>

    fun getGuilds(user: Int): List<DatabaseMember>
}