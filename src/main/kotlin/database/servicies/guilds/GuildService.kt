package database.servicies.guilds

interface GuildService {
    fun createGuild(owner: Int, name: String): Int?

    fun deleteGuild(id: Int): Boolean

    fun editGuild(id: Int, name: String): Boolean

    fun getGuild(id: Int): DatabaseGuild?

    fun renameGuild(id: Int, name: String): Boolean
}