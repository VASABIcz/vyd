package database.servicies.guildPermisions

interface GuildRolesService {
    suspend fun removeRole(id: Int): Boolean

    suspend fun createRole(name: String, author: Int, guild: Int, permissions: Permissions): Int?

    suspend fun getRole(name: String, guild: Int): Role?

    suspend fun getRole(id: Int): Role?

    suspend fun updateRole(id: Int, permissions: Permissions): Boolean

    suspend fun getRoles(roles: Iterable<Int>): List<Role>
}