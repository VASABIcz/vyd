package database.servicies.guildPermisions

interface TextChannelRolePermissions {
    suspend fun getOverrides(channel: Int): List<TextChannelRoleOverride>

    suspend fun getOverrides(channel: Int, role: List<Int>): List<TextChannelRoleOverride>

    suspend fun getOverride(channel: Int, role: Int): TextChannelRoleOverride?

    suspend fun removeOverride(channel: Int, role: Int): Boolean

    suspend fun createOverride(channel: Int, role: Int, author: Int?): Boolean

    suspend fun updateOverride(channel: Int, role: Int, override: TextChannelPermissionsOverride): Boolean
}