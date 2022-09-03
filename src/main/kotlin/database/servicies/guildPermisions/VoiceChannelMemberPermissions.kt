package database.servicies.guildPermisions

interface VoiceChannelMemberPermissions {
    suspend fun getOverrides(channel: Int): List<VoiceChannelMemberOverride>

    suspend fun getOverride(channel: Int, member: Int): VoiceChannelMemberOverride?

    suspend fun removeOverride(channel: Int, member: Int): Boolean

    suspend fun createOverride(channel: Int, member: Int, author: Int?): Boolean

    suspend fun updateOverride(channel: Int, member: Int, override: VoiceChannelPermissionsOverride): Boolean
}