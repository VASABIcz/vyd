package wrapers

import data.responses.GuildsChannel
import data.responses.MembersMember
import data.responses.MessagesMessage
import database.servicies.channels.ChannelService
import database.servicies.guilds.GuildChannelService
import database.servicies.guilds.GuildMemberService
import database.servicies.guilds.GuildService
import database.servicies.messages.MessageService
import org.ktorm.database.Database

class GuildWrapper(
    private val database: Database,
    private val guildService: GuildService,
    private val memberService: GuildMemberService,
    private val guildChannelService: GuildChannelService,
    private val channelService: ChannelService,
    private val messageService: MessageService
) {

    fun isMember(userId: Int, guildId: Int): Boolean {
        return memberService.getMember(userId, guildId) != null
    }

    fun isOwner(userId: Int, guildId: Int): Boolean {
        val guild = guildService.getGuild(guildId) ?: return false
        return guild.owner.id == userId
    }

    fun isGuildChannel(channelId: Int, guildId: Int): Boolean {
        return guildChannelService.getChannel(channelId, guildId) != null
    }

    fun createGuild(owner: Int, name: String): Boolean {
        database.useTransaction {
            val guildId = guildService.createGuild(owner, name) ?: throw Throwable("failed to create guild")
            if (!memberService.joinGuild(owner, guildId)) {
                throw Throwable("failed to join guild")
            }
        }
        return true
    }

    fun deleteGuild(userId: Int, guildId: Int): Boolean {
        // TODO implement guild permisions ex, delete, ban, channels, ...

        return if (isOwner(userId, guildId)) {
            guildService.deleteGuild(guildId)
        } else {
            false
        }
    }

    fun renameGuild(userId: Int, guildId: Int, name: String): Boolean {
        return if (isOwner(userId, guildId)) {
            guildService.renameGuild(guildId, name)
        } else {
            false
        }
    }

    fun getChannels(userId: Int, guildId: Int): List<GuildsChannel>? {
        if (!isMember(userId, guildId)) {
            return null
        }

        return guildChannelService.getChannels(guildId).map {
            it.toGuildsChannel()
        }
    }

    fun getChannel(userId: Int, guildId: Int, channelId: Int): GuildsChannel? {
        if (!isMember(userId, guildId)) {
            return null
        }

        return guildChannelService.getChannel(channelId, guildId)?.toGuildsChannel()
    }

    fun deleteChannel(userId: Int, guildId: Int, channelId: Int): Boolean {
        return if (isOwner(userId, guildId)) {
            channelService.deleteChannel(channelId)
        } else {
            false
        }
    }

    fun getMessages(
        userId: Int,
        guildId: Int,
        channelId: Int,
        amount: Int = 50,
        offset: Int = 0
    ): List<MessagesMessage>? {
        if (!isMember(userId, guildId) || !isGuildChannel(channelId, guildId)) {
            return null
        }

        return messageService.getMessages(channelId, amount, offset).map {
            it.toMessagesMessage()
        }
    }

    fun getMessage(userId: Int, guildId: Int, channelId: Int, messageId: Int): MessagesMessage? {
        if (!isMember(userId, guildId) || !isGuildChannel(channelId, guildId)) {
            return null
        }

        val message = messageService.getMessage(messageId) ?: return null
        return if (message.channel.id == channelId) {
            message.toMessagesMessage()
        } else {
            null
        }
    }

    fun sendMessage(userId: Int, guildId: Int, channelId: Int, content: String): Boolean {
        if (!isMember(userId, guildId) || !isGuildChannel(channelId, guildId)) {
            return false
        }

        return messageService.createMessage(userId, channelId, content) != null
    }

    fun deleteMessage(userId: Int, guildId: Int, channelId: Int, messageId: Int): Boolean {
        // TODO delete message that takes user as parameter
        val message = messageService.getMessage(messageId) ?: return false
        if (!isOwner(userId, guildId) || (message.author.id == userId) || (channelId != message.channel.id)) {
            return false
        }
        return messageService.deleteMessage(messageId)
    }

    fun getMembers(userId: Int, guildId: Int): List<MembersMember>? {
        if (!isMember(userId, guildId)) {
            return null
        }
        val members = memberService.getMembers(guildId)

        return members.map {
            it.toMembersMember()
        }
    }

    fun getMember(userId: Int, guildId: Int, memberId: Int): MembersMember? {
        if (!isMember(userId, guildId)) {
            return null
        }

        return memberService.getMember(memberId, guildId)?.toMembersMember()
    }

    fun leaveMember(userId: Int, guildId: Int, memberId: Int): Boolean {
        if (!isMember(userId, guildId) || !isMember(memberId, guildId)) {
            return false
        }

        return if (userId == memberId || isOwner(userId, guildId)) {
            memberService.leaveGuild(memberId, guildId)
        } else {
            false
        }
    }
}