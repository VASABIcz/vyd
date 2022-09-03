package database.servicies.guildPermisions

interface VoiceChannelRolePermissions {
    suspend fun getOverrides(channel: Int): List<VoiceChannelRoleOverride>

    suspend fun getOverride(channel: Int, role: Int): VoiceChannelRoleOverride?

    suspend fun removeOverride(channel: Int, role: Int): Boolean

    suspend fun createOverride(channel: Int, role: Int, author: Int?): Boolean

    suspend fun updateOverride(channel: Int, role: Int, override: VoiceChannelPermissionsOverride): Boolean
}