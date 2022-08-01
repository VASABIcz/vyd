package database.servicies.guilds

interface GuildService {
    suspend fun createGuild(owner: Int, name: String): Int?

    suspend fun deleteGuild(id: Int): Boolean

    suspend fun editGuild(id: Int, name: String): Boolean

    suspend fun getGuild(id: Int): Guild?

    suspend fun renameGuild(id: Int, name: String): Boolean
}