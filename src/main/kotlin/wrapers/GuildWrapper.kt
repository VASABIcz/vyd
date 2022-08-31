package wrapers

import Config
import data.responses.GuildsChannel
import data.responses.GuildsInvite
import data.responses.MembersMember
import data.responses.MessagesMessage
import database.servicies.channels.ChannelService
import database.servicies.channels.ChannelType
import database.servicies.guildChannels.Chans
import database.servicies.guildChannels.GuildChannelOrderingService
import database.servicies.guilds.*
import database.servicies.messages.MessageService
import kotlinx.coroutines.*
import org.ktorm.database.Database
import utils.random.RandomStringService
import websockets.DispatcherService
import java.time.Instant
import kotlin.system.measureTimeMillis

class GuildWrapper(
    private val database: Database,
    private val guildService: GuildService,
    private val memberService: GuildMemberService,
    private val guildChannelService: GuildChannelService,
    private val channelService: ChannelService,
    private val messageService: MessageService,
    private val guildChannelOrderingService: GuildChannelOrderingService,
    private val guildInviteService: GuildInviteService,
    private val randomStringService: RandomStringService,
    private val dispatcherService: DispatcherService
) {
    // TODO add to cfg
    private val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    val scope = CoroutineScope(Dispatchers.Default)

    private fun isValid(text: String): Boolean {
        for (c in text) {
            if (chars.contains(c)) {
                continue
            } else {
                return false
            }
        }
        return true
    }

    fun isMember(userId: Int, guildId: Int): Deferred<Boolean> = scope.async {
        return@async memberService.getMember(userId, guildId) != null
    }

    suspend fun isOwner(userId: Int, guildId: Int): Deferred<Boolean> = scope.async {
        val guild = guildService.getGuild(guildId) ?: return@async false
        return@async guild.owner.id == userId
    }

    suspend fun isGuildChannel(channelId: Int, guildId: Int): Deferred<Boolean> = scope.async {
        return@async guildChannelService.getChannel(channelId, guildId) != null
    }

    suspend fun createGuild(owner: Int, name: String): Guild? {
        database.useTransaction {
            val guildId = guildService.createGuild(owner, name) ?: throw Throwable("failed to create guild")
            println("guild id is $guildId $owner")
            if (!guildChannelOrderingService.createRecord(guildId)) {
                throw Throwable("failed to create channel ordering")
            }
            if (!memberService.joinGuild(owner, guildId)) {
                throw Throwable("failed to join guild")
            }
            return guildService.getGuild(guildId)
        }
    }

    suspend fun deleteGuild(userId: Int, guildId: Int): Boolean {
        // TODO implement guild permisions ex, delete, ban, channels, ...

        return if (isOwner(userId, guildId).await()) {
            guildService.deleteGuild(guildId)
        } else {
            false
        }
    }

    suspend fun renameChannel(userId: Int, guildId: Int, channelId: Int, name: String): Boolean {
        return if (isOwner(userId, guildId).await()) {
            guildChannelService.editChannel(channelId, guildId, name)
        } else {
            false
        }
    }

    suspend fun renameGuild(userId: Int, guildId: Int, name: String): Boolean {
        return if (isOwner(userId, guildId).await()) {
            guildService.renameGuild(guildId, name)
        } else {
            false
        }
    }

    suspend fun createChannel(guildId: Int, userId: Int, name: String, type: ChannelType, category: Int?): Boolean {
        if (!isOwner(userId, guildId).await()) {
            return false
        }
        if (category != null && type == ChannelType.category) {
            return false
        }

        database.useTransaction {
            val ch = channelService.createChannel(type) ?: throw Exception()

            guildChannelService.createChannel(guildId, ch, name) ?: throw Exception()

            if (!guildChannelOrderingService.createChannel(ch, guildId, category, guildChannelOrderingService)) {
                throw Exception()
            }
        }

        return true
    }

    suspend fun moveChannel(channelId: Int, userId: Int, guildId: Int, position: Int, category: Int?): Boolean {
        if (!isOwner(userId, guildId).await()) {
            return false
        }
        val ch = guildChannelService.getChannel(channelId, guildId) ?: return false

        if (ch.channel.type == ChannelType.category) {
            guildChannelOrderingService.moveCategory(channelId, guildId, position, guildChannelOrderingService)
        } else {
            guildChannelOrderingService.moveChannel(channelId, guildId, category, position, guildChannelOrderingService)
        }

        return true
    }

    suspend fun getChannels(userId: Int, guildId: Int): List<GuildsChannel>? {
        if (!isMember(userId, guildId).await()) {
            return null
        }

        return guildChannelService.getChannels(guildId).map {
            it.toGuildsChannel()
        }
    }

    suspend fun getChannelsOrdered(userId: Int, guildId: Int): Chans? {
        if (!isMember(userId, guildId).await()) {
            return null
        }

        return guildChannelOrderingService.getChannels(guildId) ?: Chans(mutableListOf(), mutableListOf())
    }

    suspend fun getChannel(userId: Int, guildId: Int, channelId: Int): GuildsChannel? {
        if (!isMember(userId, guildId).await()) {
            return null
        }

        return guildChannelService.getChannel(channelId, guildId)?.toGuildsChannel()
    }

    suspend fun deleteChannel(userId: Int, guildId: Int, channelId: Int): Boolean {
        return if (isOwner(userId, guildId).await()) {
            database.useTransaction {
                if (!channelService.deleteChannel(channelId)) {
                    throw Exception()
                }
                if (!guildChannelOrderingService.deleteChannel(channelId, guildId, guildChannelOrderingService)) {
                    throw Exception()
                }
            }
            true
        } else {
            false
        }
    }

    suspend fun getMessages(
        userId: Int,
        guildId: Int,
        channelId: Int,
        amount: Int? = Config.messageAmountDefault,
        offset: Int? = 0,
        id: Int?
    ): List<MessagesMessage>? {
        val isMember = isMember(userId, guildId)
        val isGuildChannel = isGuildChannel(channelId, guildId)
        if (!isMember.await() || !isGuildChannel.await()) {
            return null
        }

        // TODO amount validation f???
        var amount = amount ?: Config.messageAmountDefault
        if (amount > Config.messageAmountLimit) {
            amount = Config.messageAmountDefault
        } else if (amount < 0) {
            return null
        }
        val offset = offset ?: 0

        return messageService.getMessages(channelId, amount, offset, id).map {
            it.toMessagesMessage()
        }
    }

    suspend fun getMessage(userId: Int, guildId: Int, channelId: Int, messageId: Int): MessagesMessage? {
        val isMember = isMember(userId, guildId)
        val isGuildChannel = isGuildChannel(channelId, guildId)

        if (!isMember.await() || !isGuildChannel.await()) {
            return null
        }

        val message = messageService.getMessage(messageId) ?: return null
        return if (message.channel.id == channelId) {
            message.toMessagesMessage()
        } else {
            null
        }
    }

    suspend fun sendMessage(userId: Int, guildId: Int, channelId: Int, content: String): Boolean {
        val t = measureTimeMillis {
            val isGuildChannel = isGuildChannel(channelId, guildId)
            val isMember = isMember(userId, guildId)
            if (!isMember.await() || !isGuildChannel.await()) {
                return false
            }
        }
        println("check took $t ms")

        return messageService.createMessage(userId, channelId, content)?.also {
            withContext(Dispatchers.IO) {
                launch {
                    dispatcherService.sendMessage(guildId, channelId, userId, content)
                }
            }
        } != null
    }

    suspend fun deleteMessage(userId: Int, guildId: Int, channelId: Int, messageId: Int): Boolean {
        // TODO delete message that takes user as parameter
        val message = messageService.getMessage(messageId) ?: return false
        if (!isOwner(userId, guildId).await() || (message.author.id == userId) || (channelId != message.channel.id)) {
            return false
        }
        return messageService.deleteMessage(messageId)
    }

    suspend fun getMembers(userId: Int, guildId: Int): List<MembersMember>? {
        if (!isMember(userId, guildId).await()) {
            return null
        }
        val members = memberService.getMembers(guildId)

        return members.map {
            it.toMembersMember()
        }
    }

    suspend fun getMember(userId: Int, guildId: Int, memberId: Int): MembersMember? {
        if (!isMember(userId, guildId).await()) {
            return null
        }

        return memberService.getMember(memberId, guildId)?.toMembersMember()
    }

    suspend fun changeNick(userId: Int, guildId: Int, nick: String): Boolean {
        if (!isMember(userId, guildId).await()) {
            return false
        }

        return memberService.changeNick(userId, guildId, nick)
    }

    suspend fun leaveMember(userId: Int, guildId: Int, memberId: Int): Boolean {
        val isMember1 = isMember(userId, guildId)
        val isMember2 = isMember(memberId, guildId)
        val isOwner = isOwner(userId, guildId)
        if (!isMember1.await() || !isMember2.await()) {
            return false
        }

        return if (userId == memberId || isOwner.await()) {
            memberService.leaveGuild(memberId, guildId)
        } else {
            false
        }
    }

    suspend fun createInvite(userId: Int, guildId: Int, url: String?, expire: Long?, maxUses: Int?): String? {
        if (!isOwner(userId, guildId).await()) {
            return null
        }
        val expireTimestamp: Instant? = expire?.let { Instant.ofEpochMilli(it) }
        if (expireTimestamp != null) {
            if (expireTimestamp < Instant.now()) {
                return null
            }
        }
        if (maxUses != null) {
            if (maxUses < 1) {
                return null
            }
        }

        return if (url == null) {
            // TODO create cfg for this
            repeat(4) {
                val genUrl = randomStringService.generateString(8)
                val res = guildInviteService.createInvite(guildId, userId, genUrl, expireTimestamp, maxUses)
                if (res == null) {
                    delay(50)
                    return@repeat
                } else {
                    return res
                }
            }
            null
        } else {
            if (url.length < 4 || url.length > 11 || !isValid(url)) {
                return null
            }
            val inv = guildInviteService.createInvite(guildId, userId, url, expireTimestamp, maxUses)
            println(inv)
            inv
        }
    }

    suspend fun joinInvite(userId: Int, url: String): Boolean {
        val inv = guildInviteService.getInvite(url) ?: return false
        if (!inv.isUsable()) {
            return false
        }
        database.useTransaction {
            if (!guildInviteService.incrementInvites(url)) {
                throw Exception()
            }
            if (!memberService.joinGuild(userId, inv.guild.id)) {
                throw Exception()
            }
        }
        return true
    }

    suspend fun getInvites(userId: Int, guildId: Int): List<GuildsInvite>? {
        if (!isOwner(userId, guildId).await()) {
            return null
        }
        return guildInviteService.getInvites(guildId).map {
            it.toGuildsInvite()
        }
    }

    suspend fun getInvite(userId: Int, guildId: Int, url: String): GuildsInvite? {
        if (!isOwner(userId, guildId).await()) {
            return null
        }
        return guildInviteService.getInvite(url)?.toGuildsInvite()
    }

    suspend fun deleteInvite(userId: Int, guildId: Int, url: String): Boolean {
        if (!isOwner(userId, guildId).await()) {
            return false
        }
        return guildInviteService.removeInvite(url)
    }
}