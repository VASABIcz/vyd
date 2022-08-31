package database.servicies.dms

import org.ktorm.database.Database
import org.ktorm.database.asIterable
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

class DatabaseDmMemberService(private val database: Database) : DmMemberService {
    private val members = database.sequenceOf(DatabaseDmMembers)

    override suspend fun addMember(dm: Int, user: Int, inviter: Int?): Boolean {
        return database.insert(DatabaseDmMembers) {
            set(it.channel, dm)
            set(it.user, user)
            set(it.inviter, inviter)
        } != 0
    }

    override suspend fun removeMember(dm: Int, user: Int): Boolean {
        val member = members.find {
            (it.channel eq dm) and (it.user eq user)
        } ?: return false
        return member.delete() != 0
    }

    override suspend fun banMember(dm: Int, member: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getMember(dm: Int, user: Int): DmMember? {
        return members.find {
            (it.user eq user) and (it.channel eq dm)
        }
    }

    override suspend fun getMembers(dm: Int): List<DmMember> {
        return members.filter {
            it.channel eq dm
        }.toList()
    }

    override suspend fun getDmChannels(user: Int): List<DmMember> {
        return members.filter {
            it.user eq user
        }.toList()
    }

    override suspend fun getDmChannels(user: Int, user2: Int): List<Pair<Int, String>> {
        return database.useConnection { conn ->
            val sql = """
                SELECT dc.channel_id, dc.type 
                    FROM dm_members 
                    JOIN dm_members as e 
                        on dm_members.channel_id = e.channel_id 
                        and dm_members.user_id = ? 
                        and e.user_id = ? 
                    JOIN dm_channels dc
                        on dm_members.channel_id = dc.channel_id
                """
            conn.prepareStatement(sql).use { statement ->
                statement.setInt(0, user)
                statement.setInt(1, user2)
                statement.executeQuery().asIterable().map {
                    it.getInt(1) to it.getString(2)
                }
            }
        }
    }
}