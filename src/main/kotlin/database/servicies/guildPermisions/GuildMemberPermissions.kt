package database.servicies.guildPermisions

interface GuildMemberPermissions {
    suspend fun getHighestPermissions(member: Int): UserPermissions

    suspend fun getRoles(member: Int): List<MemberRole>

    suspend fun addRole(member: Int, role: Int, assigner: Int?): Boolean

    suspend fun removeRole(member: Int, role: Int): Boolean
}