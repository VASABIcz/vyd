package websockets.worker

import database.servicies.friends.FriendService
import database.servicies.guilds.GuildMember
import database.servicies.guilds.GuildMemberService
import database.servicies.guilds.GuildService
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsSubscriber
import io.github.crackthecodeabhi.kreds.connection.KredsSubscriberClient
import io.github.crackthecodeabhi.kreds.connection.newSubscriberClient
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import java.awt.Frame
import kotlin.coroutines.coroutineContext

class RedisConnection(
    private val endpoint: Endpoint,
    private val user: Int,
    private val ws: DefaultWebSocketServerSession,
    private val guildMemberService: GuildMemberService,
    private val friendService: FriendService,
    ): KredsSubscriber {
    lateinit var redisClient: KredsSubscriberClient
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun connect() = scope.async {
        redisClient = newSubscriberClient(endpoint, this@RedisConnection)
        redisClient.subscribe("user:$user")
        for (g in guildMemberService.getGuilds(user)) {
            redisClient.subscribe("guild:${g.guild.id}")
        }
        for (f in friendService.getFriends(user)) {
            redisClient.subscribe("user:${f.friend(user)}")
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
        // TODO
        // find a way to identify event like (add friend, or guild join so it can subscribe for those events)
        try {
            send(message)
        }
        catch (_: Throwable) {
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