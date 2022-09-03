package database.servicies.guildPermisions

interface GuildRolesOrdering {
    suspend fun add(role: Int, guild: Int): Boolean

    suspend fun move(role: Int, guild: Int, position: Int): Boolean

    suspend fun get(guild: Int): List<RolePosition>

    suspend fun isHigher(role: Int, role1: Int): Boolean?
}