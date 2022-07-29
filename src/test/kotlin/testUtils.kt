import auth.hash.SaltedHash
import database.servicies.channels.Channel
import database.servicies.channels.ChannelService
import database.servicies.channels.ChannelType
import database.servicies.guilds.*
import database.servicies.messages.Message
import database.servicies.messages.MessageService
import database.servicies.usernames.UsernameService
import database.servicies.users.User
import database.servicies.users.UserService
import java.time.Instant

data class TestingUser(
    override val id: Int,
    override val name: String,
    override val discriminator: String,
    override val hash: ByteArray = "".toByteArray(),
    override val registerDate: Instant = Instant.now(),
    override val salt: ByteArray = "".toByteArray(),
) : User

data class TestingGuild(
    override val id: Int,
    override var name: String,
    override val owner: User,
    override val timestamp: Instant = Instant.now()
) : Guild

data class TestingGuildMember(
    override val user: User,
    override val guild: Guild,
    override var nick: String? = null,
    override val timestamp: Instant = Instant.now()
) : GuildMember

data class TestingGuildChannel(
    override val guild: Guild,
    override val channel: Channel,
    override var name: String
) : GuildChannel

data class TestingChannel(
    override val id: Int,
    override val type: ChannelType,
    override val timestamp: Instant = Instant.now(),
) : Channel

data class TestingMessage(
    override val id: Int,
    override val content: String,
    override val author: User,
    override val timestamp: Instant,
    override val channel: Channel
) : Message

data class PartialTestingMessage(
    val author: Int,
    val channel: Int,
    var content: String,
    val timestamp: Instant = Instant.now()
)

class TestUserService(val users: MutableList<User>) : UserService {
    var ids = 0

    override fun createUser(username: String, hash: SaltedHash, discriminator: String): Int {
        println(users)
        val id = ids++
        users.add(
            TestingUser(
                id,
                username,
                discriminator,
                hash.hash.toByteArray(),
                Instant.now(),
                hash.salt.toByteArray()
            )
        )

        return id
    }

    override fun getUser(username: String, discriminator: String): User? {
        println(users)
        return users.find {
            it.name == username && it.discriminator == discriminator
        }
    }

    override fun getUser(id: Int): User? {
        println(users)
        return users.find {
            it.id == id
        }
    }

    override fun deleteUser(id: Int): Boolean {
        println(users)
        users.forEachIndexed { index, user ->
            if (user.id == id) {
                users.removeAt(index)
                return true
            }
        }
        return false
    }

    override fun deleteUser(username: String, discriminator: String): Boolean {
        println(users)
        users.forEachIndexed { index, user ->
            if (user.name == username && user.discriminator == discriminator) {
                users.removeAt(index)
                return true
            }
        }
        return false
    }

}

class TestUsernameService : UsernameService {
    var dis = 0

    override fun getDiscriminator(username: String): String {
        return dis.toString()
    }

    override fun incrementDiscriminator(username: String): Boolean {
        dis++
        return true
    }

}

class TestGuildService(val guilds: MutableList<Guild>, val userService: UserService) : GuildService {
    var ids = 0
    override fun createGuild(owner: Int, name: String): Int {
        val id = ids++
        guilds.add(TestingGuild(id, name, userService.getUser(owner)!!, Instant.now()))
        return id
    }

    override fun deleteGuild(id: Int): Boolean {
        guilds.forEachIndexed { index, guild ->
            if (guild.id == id) {
                guilds.removeAt(index)
                return true
            }
        }
        return false
    }

    override fun editGuild(id: Int, name: String): Boolean { // TODO ??????
        guilds.forEachIndexed { index, guild ->
            if (guild.id == id) {
                guilds[index].name = name
                return true
            }
        }
        return false
    }

    override fun getGuild(id: Int): Guild? {
        return guilds.find {
            it.id == id
        }
    }

    override fun renameGuild(id: Int, name: String): Boolean {
        guilds.forEachIndexed { index, guild ->
            if (guild.id == id) {
                guilds[index].name = name
                return true
            }
        }
        return false
    }

}

class TestGuildMemberService(val userService: UserService, val guildService: GuildService) : GuildMemberService {
    val members = HashMap<Guild, MutableList<GuildMember>>()

    override fun joinGuild(user: Int, guild: Int): Boolean {
        val user = userService.getUser(user) ?: return false
        val guild = guildService.getGuild(guild) ?: return false

        members[guild]?.add(TestingGuildMember(user, guild)) ?: return false
        return true
    }

    override fun leaveGuild(user: Int, guild: Int): Boolean {
        val user = userService.getUser(user) ?: return false
        val guild = guildService.getGuild(guild) ?: return false

        val mems = members[guild] ?: return false
        mems.forEachIndexed { index, guildMember ->
            if (guildMember.user.id == user.id) {
                mems.removeAt(index)
                members[guild] = mems
                return true
            }
        }
        return false
    }

    override fun changeNick(user: Int, guild: Int, nick: String): Boolean {
        val user = userService.getUser(user) ?: return false
        val guild = guildService.getGuild(guild) ?: return false

        val mems = members[guild] ?: return false
        mems.forEachIndexed { index, guildMember ->
            if (guildMember.user.id == user.id) {
                mems[index].nick = nick
                members[guild] = mems
                return true
            }
        }
        return false
    }

    override fun getMember(user: Int, guild: Int): GuildMember? {
        val user = userService.getUser(user) ?: return null
        val guild = guildService.getGuild(guild) ?: return null

        val mems = members[guild] ?: return null
        return mems.find {
            it.user.id == user.id
        }
    }

    override fun getMembers(guild: Int, amount: Int, offset: Int): List<GuildMember> {
        val guild = guildService.getGuild(guild) ?: return emptyList()

        return members[guild] ?: return emptyList()
    }

    override fun getGuilds(user: Int): List<GuildMember> {
        val guilds = emptyList<GuildMember>().toMutableList()

        for ((_, mems) in members) {
            mems.find {
                it.user.id == user
            }?.also {
                guilds.add(it)
            }
        }
        return guilds
    }
}

class TestingGuildChannelService(val guildService: GuildService, val channelService: ChannelService) :
    GuildChannelService {
    val channels = HashMap<Int, Pair<String, Int>>()

    override fun moveChannel(channel: Int, guild: Int, position: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun editChannel(id: Int, guild: Int, name: String): Boolean {
        channelService.getChannel(id) ?: channels.remove(id).also { return false }

        val pair = channels[id] ?: return false
        guildService.getGuild(pair.second) ?: channels.remove(id).also { return false }
        channels[id] = Pair(name, pair.second)
        return true
    }

    override fun getChannels(guild: Int): List<GuildChannel> {
        val chs = emptyList<GuildChannel>().toMutableList()

        for ((channel, pair) in channels) {
            if (pair.second == guild) {
                val g = guildService.getGuild(pair.second) ?: continue
                val ch = channelService.getChannel(channel) ?: continue
                chs.add(TestingGuildChannel(g, ch, pair.first))
            }
        }
        return chs
    }

    override fun getChannel(id: Int, guild: Int): GuildChannel? {
        for ((channel, pair) in channels) {
            if (pair.second == guild) {
                val g = guildService.getGuild(pair.second) ?: continue
                val ch = channelService.getChannel(channel) ?: continue
                return TestingGuildChannel(g, ch, pair.first)
            }
        }
        return null
    }

}

class TestingChannelService : ChannelService {
    val channels = HashMap<Int, Channel>()
    var ids = 0
    override fun createChannel(type: ChannelType): Int {
        val id = ids++
        channels[id] = TestingChannel(id, type)
        return id
    }

    override fun deleteChannel(id: Int): Boolean {
        channels.remove(id) ?: return false
        return true
    }

    override fun getChannel(id: Int): Channel? {
        return channels[id]
    }

}

class TestingMessageService(val userService: UserService, val channelService: ChannelService) : MessageService {
    var ids = 0
    val messages = HashMap<Int, PartialTestingMessage>()
    override fun createMessage(user: Int, channel: Int, content: String): Int {
        val id = ids++
        messages[id] = PartialTestingMessage(user, channel, content)
        return id
    }

    override fun getMessage(id: Int): Message? {
        return messages[id]?.let { toMessage(id, it) }
    }

    override fun editMessage(id: Int, content: String): Boolean {
        messages[id]?.also { it.content = content } ?: return false
        return true
    }

    override fun deleteMessage(id: Int): Boolean {
        messages.remove(id) ?: return false
        return true
    }

    override fun getMessages(channel: Int, amount: Int, offset: Int, id: Int?): List<Message> {
        val ms = emptyList<Message>().toMutableList()

        for ((id, m) in messages) {
            if (m.channel == channel) {
                toMessage(id, m)?.let { ms.add(it) }
            }
        }
        return ms
    }


    fun toMessage(id: Int, m: PartialTestingMessage): Message? {
        val u = userService.getUser(m.author) ?: return null
        val ch = channelService.getChannel(m.channel) ?: return null

        return TestingMessage(id, m.content, u, m.timestamp, ch)
    }
}

