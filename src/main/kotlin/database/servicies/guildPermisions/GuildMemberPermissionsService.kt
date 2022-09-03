package database.servicies.guildPermisions

import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

class GuildMemberPermissionsService(private val database: Database) : GuildMemberPermissions {
    private val roles = database.sequenceOf(DatabaseGuildMemberRoles)
    override suspend fun getHighestPermissions(member: Int): UserPermissions {
        // TODO custom sql query?
        // + not sure if this is the proper behavior
        TODO("Not yet implemented")
    }

    override suspend fun getRoles(member: Int): List<MemberRole> {
        return roles.filter {
            (it.member eq member)
        }.toList()
    }

    override suspend fun addRole(member: Int, role: Int, assigner: Int?): Boolean {
        return database.insert(DatabaseGuildMemberRoles) {
            set(it.member, member)
            set(it.role, role)
            set(it.assigner, assigner)
        } != 0
    }

    override suspend fun removeRole(member: Int, role: Int): Boolean {
        val r = roles.find {
            (it.member eq member) and (it.role eq role)
        } ?: return false
        r.delete()
        return r.flushChanges() != 0
    }
}