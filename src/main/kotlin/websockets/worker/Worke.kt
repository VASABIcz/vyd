package websockets.worker

import database.servicies.friends.FriendService
import database.servicies.guilds.GuildMemberService
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsSubscriber
import io.github.crackthecodeabhi.kreds.connection.KredsSubscriberClient
import io.github.crackthecodeabhi.kreds.connection.newSubscriberClient
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import websockets.*

class RedisConnection(
    private val endpoint: Endpoint,
    private val user: Int,
    private val ws: DefaultWebSocketServerSession,
    private val guildMemberService: GuildMemberService,
    private val friendService: FriendService,
    ): KredsSubscriber {
    lateinit var redisClient: KredsSubscriberClient
    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun connect() = scope.async {
        redisClient = newSubscriberClient(endpoint, this@RedisConnection)
        redisClient.subscribe("me:$user")
        redisClient.subscribe("user:$user")
        redisClient.subscribe("me:state:$user")
        redisClient.subscribe("user:state:$user")
        for (g in guildMemberService.getGuilds(user)) {
            redisClient.subscribe("guild:${g.guild.id}")
            redisClient.subscribe("guild:state:${g.guild.id}")
        }
        for (f in friendService.getFriends(user)) {
            redisClient.subscribe("user:${f.friend(user)}")
            redisClient.subscribe("user:state:${f.friend(user)}")
        }
    }

    suspend fun close() {
        scope.cancel()
        ws.close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "something fucked up :)"))
        redisClient.close()
    }

    suspend fun send(text: String) {
        ws.send(io.ktor.websocket.Frame.Text(text))
    }

    private fun launch(s: suspend () -> Any) {
        scope.launch {
            s()
        }
    }

    override fun onException(ex: Throwable) {
        ex.printStackTrace()
    }

    override fun onMessage(channel: String, message: String) = launch {
        if (channel.contains("state")) {
            val event: JustEvent = json.decodeFromString(message)
            when (event.op) {
                EventType.RemoveFriend -> {
                    val r: RemoveFriend = json.decodeFromString(message)
                    redisClient.unsubscribe("user:${r.friend}")
                    redisClient.unsubscribe("user:state:${r.friend}")
                }

                EventType.AddFriend -> {
                    // FIXME
                    /*
                    val r: AddFriend = json.decodeFromString(message)
                    redisClient.subscribe("user:${r.friend}")
                    redisClient.subscribe("user:state:${r.friend}")

                     */
                }

                EventType.GuildMemberLeave -> {
                    val r: GuildMemberLeave = json.decodeFromString(message)
                    if (r.member == user) {
                        redisClient.unsubscribe("guild:${r.member}")
                        redisClient.unsubscribe("guild:state:${r.member}")
                    }
                }

                EventType.GuildMemberJoin -> {
                    // FIXME
                    /*
                    val r: GuildMemberLeave = json.decodeFromString(message)
                    if (r.member == user) {
                        redisClient.unsubscribe("guild:${r.member}")
                        redisClient.unsubscribe("guild:state:${r.member}")
                    }
                     */
                }

                EventType.GuildDelete -> {
                    val r: GuildDelete = json.decodeFromString(message)
                    redisClient.unsubscribe("guild:${r.guild}")
                    redisClient.unsubscribe("guild:state:${r.guild}")
                }

                EventType.UserDelete -> {
                    val r: UserDelete = json.decodeFromString(message)
                    redisClient.unsubscribe("guild:${r.user}")
                    redisClient.unsubscribe("guild:state:${r.user}")
                }

                else -> {}
            }
        }
        // TODO
        // find a way to identify event like (add friend, or guild join so it can subscribe for those events)
        try {
            send(message)
        } catch (_: Throwable) {
            close()
        }
    }

    override fun onSubscribe(channel: String, subscribedChannels: Long) {
        // TODO("Not yet implemented")
    }

    override fun onUnsubscribe(channel: String, subscribedChannels: Long) {
        // TODO("Not yet implemented")
    }

    override fun onPMessage(pattern: String, channel: String, message: String) {}

    override fun onPSubscribe(pattern: String, subscribedChannels: Long) {}

    override fun onPUnsubscribe(pattern: String, subscribedChannels: Long) {}
}