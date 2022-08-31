package database.servicies.dms

interface DmInviteService {
    suspend fun getPendingInvite(member: Int, dm: Int): GroupInvite?

    suspend fun createInvite(member: Int, dm: Int): String?

    suspend fun getPendingInvite(url: String): GroupInvite?
}