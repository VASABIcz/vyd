package database.servicies.guildPermisions

import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

class VoiceChannelMemberPermissionsService(private val database: Database) : VoiceChannelMemberPermissions {
    private val permissions = database.sequenceOf(DatabaseVoicePermissionsMembers)

    override suspend fun getOverrides(channel: Int): List<VoiceChannelMemberOverride> {
        return permissions.filter {
            it.channel eq channel
        }.toList()
    }

    override suspend fun getOverrides(channel: Int, member: List<Int>): List<VoiceChannelMemberOverride> {
        return permissions.filter {
            (it.channel eq channel) and (it.member inList member)
        }.toList()
    }

    override suspend fun getOverride(channel: Int, member: Int): VoiceChannelMemberOverride? {
        return permissions.find {
            (it.channel eq channel) and (it.member eq member)
        }
    }

    override suspend fun removeOverride(channel: Int, member: Int): Boolean {
        val ov = permissions.find {
            (it.channel eq channel) and (it.member eq member)
        } ?: return false
        ov.delete()
        return ov.flushChanges() != 0
    }

    override suspend fun createOverride(channel: Int, member: Int, author: Int?): Boolean {
        database.useTransaction {
            val id = database.insertAndGenerateKey(DatabaseVoicePermissions) {
                set(it.connect, null)
                set(it.speak, null)
                set(it.stream_video, null)
                set(it.priority_speaker, null)
                set(it.deafen, null)
                set(it.move, null)

            } as Int? ?: throw Throwable("whops failed to insert text permissions")
            val x = database.insert(DatabaseVoicePermissionsMembers) {
                set(it.member, member)
                set(it.channel, channel)
                set(it.permissions, id)
                set(it.author, author)
            }
            if (x == 0) throw Throwable("whops failed to insert role text permissions")
        }
        return true
    }

    override suspend fun updateOverride(channel: Int, member: Int, override: VoiceChannelPermissionsOverride): Boolean {
        val ov = permissions.find { (it.channel eq channel) and (it.member eq member) } ?: return false
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