package database.servicies.guildPermisions

import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

class TextChannelRolePermissionsService(private val database: Database) : TextChannelRolePermissions {
    private val permissions = database.sequenceOf(DatabaseTextPermissionsRoles)

    override suspend fun getOverrides(channel: Int): List<TextChannelRoleOverride> {
        return permissions.filter {
            it.channel eq channel
        }.toList()
    }

    override suspend fun getOverride(channel: Int, role: Int): TextChannelRoleOverride? {
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
            val id = database.insertAndGenerateKey(DatabaseTextPermissions) {
                set(it.send_messages, null)
                set(it.send_embeds, null)
                set(it.send_attachments, null)
                set(it.add_reactions, null)
                set(it.send_external_emojis, null)
                set(it.mention_everyone, null)
                set(it.manage_messages, null)
                set(it.view_history, null)

            } as Int? ?: throw Throwable("whops failed to insert text permissions")
            val x = database.insert(DatabaseTextPermissionsRoles) {
                set(it.role, role)
                set(it.channel, channel)
                set(it.permissions, id)
                set(it.author, author)
            }
            if (x == 0) throw Throwable("whops failed to insert role text permissions")
        }
        return true
    }

    override suspend fun updateOverride(channel: Int, role: Int, override: TextChannelPermissionsOverride): Boolean {
        val ov = permissions.find { (it.channel eq channel) and (it.role eq role) } ?: return false
        ov.permissions.sendMessages = override.sendMessages
        ov.permissions.sendEmbeds = override.sendEmbeds
        ov.permissions.sendAttachments = override.sendAttachments
        ov.permissions.addReactions = override.addReactions
        ov.permissions.sendExternalEmojis = override.sendExternalEmojis
        ov.permissions.mentionEveryone = override.mentionEveryone
        ov.permissions.manageMessages = override.manageMessages
        ov.permissions.viewHistory = override.viewHistory
        return ov.permissions.flushChanges() != 0
    }
}