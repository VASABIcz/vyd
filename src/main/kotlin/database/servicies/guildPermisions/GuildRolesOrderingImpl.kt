package database.servicies.guildPermisions

import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*

class GuildRolesOrderingImpl(private val database: Database) : GuildRolesOrdering {
    val roles = database.sequenceOf(DatabaseRoleOrdering)

    override suspend fun add(role: Int, guild: Int): Boolean {
        val x = roles.filter {
            it.guild eq guild
        }.sortedBy { it.position }.lastOrNull()

        return if (x == null) {
            database.insert(DatabaseRoleOrdering) {
                set(it.guild, guild)
                set(it.role, role)
                set(it.position, 0)
            } != 0
        } else {
            database.insert(DatabaseRoleOrdering) {
                set(it.guild, guild)
                set(it.role, role)
                set(it.position, x.position + 1)
            } != 0
        }
    }

    override suspend fun addBeforeLast(role: Int, guild: Int): Boolean {
        // used for adding role that's always before @everyone aka last role
        val lastRole = roles.filter {
            it.guild eq guild
        }.sortedBy { it.position }.lastOrNull()

        return if (lastRole == null) {
            database.insert(DatabaseRoleOrdering) {
                set(it.guild, guild)
                set(it.role, role)
                set(it.position, 0)
            } != 0
        } else {
            database.update(DatabaseRoleOrdering) {
                set(it.position, it.position + 1)
                where {
                    (it.guild eq guild) and (it.position greaterEq lastRole.position)
                }
            }
            database.insert(DatabaseRoleOrdering) {
                set(it.guild, guild)
                set(it.role, role)
                set(it.position, lastRole.position)
            } != 0
        }
    }

    override suspend fun move(role: Int, guild: Int, position: Int): Boolean {
        // checks
        if (position < 0) return false
        roles.find {
            it.role eq role
        } ?: return false

        // find the nth (position) item ordered by position
        // if its null aka we are trying to move the element out of bounds return failure
        val off =
            roles.filter { it.guild eq guild }.sortedBy { it.position }.drop(position).firstOrNull() ?: return false

        // increment every element including it
        database.update(DatabaseRoleOrdering) {
            set(it.position, it.position + 1)
            where {
                (it.guild eq guild) and (it.position greaterEq off.position)
            }
        }
        // set roles desired position
        val r = roles.find {
            it.role eq role
        } ?: return false
        r.position = position
        r.flushChanges()
        return true
    }

    override suspend fun get(guild: Int): List<RolePosition> {
        return roles.filter { it.guild eq guild }.sortedBy { it.position }.toList()
    }

    override suspend fun get(guild: Int, _roles: List<Int>): List<RolePosition> {
        return roles.filter { (it.guild eq guild) and (it.role inList _roles) }.sortedBy { it.position }.toList()
    }

    override suspend fun isHigher(role: Int, role1: Int): Boolean? {
        val rol = roles.find {
            it.role eq role
        } ?: return null
        val rol1 = roles.find {
            it.role eq role1
        } ?: return null
        return rol.position > rol1.position
    }
}