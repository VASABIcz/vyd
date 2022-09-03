package database.servicies.guildPermisions

import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

class DatabaseGuildRolesService(private val database: Database) : GuildRolesService {
    private val _roles = database.sequenceOf(DatabaseGuildRoles)

    override suspend fun removeRole(id: Int): Boolean {
        val role = _roles.find {
            it.id eq id
        } ?: return false
        role.delete()
        return role.flushChanges() != 0
    }

    override suspend fun createRole(name: String, author: Int, guild: Int, permissions: Permissions): Int? {
        return database.insertAndGenerateKey(DatabaseGuildRoles) {
            set(it.name, name)
            set(it.author, author)
            set(it.guild, guild)

            set(it.admin, permissions.admin)
            set(it.view_channels, permissions.viewChannels)
            set(it.manage_channels, permissions.manageChannels)
            set(it.manage_roles, permissions.manageRoles)
            set(it.manage_emojis, permissions.manageEmojis)
            set(it.view_logs, permissions.viewLogs)
            set(it.mange_webhooks, permissions.manageWebhooks)
            set(it.manage_guild, permissions.manageGuild)

            set(it.connect, permissions.connect)
            set(it.speak, permissions.speak)
            set(it.stream_video, permissions.streamVideo)
            set(it.priority_speaker, permissions.prioritySpeaker)
            set(it.deafen, permissions.deafen)
            set(it.move, permissions.move)

            set(it.create_invites, permissions.createInvite)
            set(it.change_nickname, permissions.changeNickName)
            set(it.manage_nickname, permissions.manageNickName)
            set(it.kick_members, permissions.kickMember)
            set(it.ban_members, permissions.banMember)
            set(it.moderate, permissions.moderate)

            set(it.send_messages, permissions.sendMessages)
            set(it.send_embeds, permissions.sendEmbeds)
            set(it.send_attachments, permissions.sendAttachments)
            set(it.add_reactions, permissions.addReactions)
            set(it.send_external_emojis, permissions.sendExternalEmojis)
            set(it.mention_everyone, permissions.mentionEveryone)
            set(it.manage_messages, permissions.manageMessages)
            set(it.view_history, permissions.viewHistory)
        } as Int?
    }

    override suspend fun getRole(name: String, guild: Int): Role? {
        return _roles.find {
            (it.name eq name) and (it.guild eq guild)
        }
    }

    override suspend fun getRole(id: Int): Role? {
        return _roles.find {
            it.id eq id
        }
    }

    override suspend fun updateRole(id: Int, permissions: Permissions): Boolean {
        return database.update(DatabaseGuildRoles) {
            set(it.admin, permissions.admin)
            set(it.view_channels, permissions.viewChannels)
            set(it.manage_channels, permissions.manageChannels)
            set(it.manage_roles, permissions.manageRoles)
            set(it.manage_emojis, permissions.manageEmojis)
            set(it.view_logs, permissions.viewLogs)
            set(it.mange_webhooks, permissions.manageWebhooks)
            set(it.manage_guild, permissions.manageGuild)

            set(it.connect, permissions.connect)
            set(it.speak, permissions.speak)
            set(it.stream_video, permissions.streamVideo)
            set(it.priority_speaker, permissions.prioritySpeaker)
            set(it.deafen, permissions.deafen)
            set(it.move, permissions.move)

            set(it.create_invites, permissions.createInvite)
            set(it.change_nickname, permissions.changeNickName)
            set(it.manage_nickname, permissions.manageNickName)
            set(it.kick_members, permissions.kickMember)
            set(it.ban_members, permissions.banMember)
            set(it.moderate, permissions.moderate)

            set(it.send_messages, permissions.sendMessages)
            set(it.send_embeds, permissions.sendEmbeds)
            set(it.send_attachments, permissions.sendAttachments)
            set(it.add_reactions, permissions.addReactions)
            set(it.send_external_emojis, permissions.sendExternalEmojis)
            set(it.mention_everyone, permissions.mentionEveryone)
            set(it.manage_messages, permissions.manageMessages)
            set(it.view_history, permissions.viewHistory)
            where {
                it.id eq id
            }
        } != 0
    }

    override suspend fun getRoles(roles: Iterable<Int>): List<Role> {
        return _roles.filter {
            it.id inList roles.toList()
        }.toList()
    }
}