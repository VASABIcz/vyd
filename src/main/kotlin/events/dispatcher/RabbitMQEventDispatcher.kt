package events.dispatcher

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RabbitMQEventDispatcher(uri: String, private val queue: String) : EventDispatcher {
    private var channel: Channel
    private val json = Json
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        val factory = ConnectionFactory()
        channel = factory.newConnection(uri).createChannel(0)
    }

    override suspend fun dispatch(message: Serializable) {
        scope.launch {
            channel.basicPublish("", "", null, json.encodeToString(message).toByteArray())
        }
    }

    override suspend fun dispatch(message: String) {
        scope.launch {
            channel.basicPublish("", "", null, message.toByteArray())
        }
    }

    override suspend fun dispatch(message: ByteArray) {
        scope.launch {
            channel.basicPublish("", "", null, message)
        }
    }
}

fun main() = runBlocking {
    val q = RabbitMQEventDispatcher("", "")
    q.dispatch("baka UwU")
}