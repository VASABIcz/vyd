package database.servicies.dms

interface DmMemberService {
    suspend fun addMember(dm: Int, user: Int, inviter: Int?): Boolean

    suspend fun removeMember(dm: Int, user: Int): Boolean

    suspend fun banMember(dm: Int, member: Int): Boolean

    suspend fun getMember(dm: Int, user: Int): DmMember?

    suspend fun getMembers(dm: Int): List<DmMember>

    suspend fun getDmChannels(user: Int): List<DmMember>

    suspend fun getDmChannels(user: Int, user2: Int): List<Pair<Int, String>>
}