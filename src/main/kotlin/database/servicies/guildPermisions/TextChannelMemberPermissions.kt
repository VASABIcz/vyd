package database.servicies.guildPermisions

interface TextChannelMemberPermissions {
    suspend fun getOverrides(channel: Int): List<TextChannelMemberOverride>

    suspend fun getOverrides(channel: Int, member: List<Int>): List<TextChannelMemberOverride>

    suspend fun getOverride(channel: Int, member: Int): TextChannelMemberOverride?

    suspend fun removeOverride(channel: Int, member: Int): Boolean

    suspend fun createOverride(channel: Int, member: Int, author: Int?): Boolean

    suspend fun updateOverride(channel: Int, member: Int, override: TextChannelPermissionsOverride): Boolean
}