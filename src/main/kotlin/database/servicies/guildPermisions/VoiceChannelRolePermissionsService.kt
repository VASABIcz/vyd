package database.servicies.guildPermisions

import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

class VoiceChannelRolePermissionsService(private val database: Database) : VoiceChannelRolePermissions {
    private val permissions = database.sequenceOf(DatabaseVoicePermissionsRoles)

    override suspend fun getOverrides(channel: Int): List<VoiceChannelRoleOverride> {
        return permissions.filter {
            it.channel eq channel
        }.toList()
    }

    override suspend fun getOverrides(channel: Int, role: List<Int>): List<VoiceChannelRoleOverride> {
        return permissions.filter {
            (it.channel eq channel) and (it.role inList role)
        }.toList()
    }

    override suspend fun getOverride(channel: Int, role: Int): VoiceChannelRoleOverride? {
        return permissions.find {
            (it.channel eq channel) and (it.role eq role)
        }
    }

    override suspend fun removeOverride(channel: Int, role: Int): Boolean {
        val ov = permissions.find {
            (it.channel eq channel) and (it.role eq role)
        } ?: return false
        ov.delete()
        return ov.flushChanges() != 0
    }

    override suspend fun createOverride(channel: Int, role: Int, author: Int?): Boolean {
        database.useTransaction {
            val id = database.insertAndGenerateKey(DatabaseVoicePermissions) {
                set(it.connect, null)
                set(it.speak, null)
                set(it.stream_video, null)
                set(it.priority_speaker, null)
                set(it.deafen, null)
                set(it.move, null)

            } as Int? ?: throw Throwable("whops failed to insert text permissions")
            val x = database.insert(DatabaseVoicePermissionsRoles) {
                set(it.role, role)
                set(it.channel, channel)
                set(it.permissions, id)
                set(it.author, author)
            }
            if (x == 0) throw Throwable("whops failed to insert role text permissions")
        }
        return true
    }

    override suspend fun updateOverride(channel: Int, role: Int, override: VoiceChannelPermissionsOverride): Boolean {
        val ov = permissions.find { (it.channel eq channel) and (it.role eq role) } ?: return false
        ov.permissions.connect = override.connect
        ov.permissions.speak = override.speak
        ov.permissions.streamVideo = override.streamVideo
        ov.permissions.activities = override.activities
        ov.permissions.prioritySpeaker = override.prioritySpeaker
        ov.permissions.deafen = override.deafen
        ov.permissions.move = override.move
        // general
        ov.permissions.viewChannel = override.viewChannel
        ov.permissions.manageChannel = override.manageChannel
        ov.permissions.managePermissions = override.managePermissions

        return ov.permissions.flushChanges() != 0
    }
}