package events.dispatcher

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RedisEventDispatcher(host: String, port: Int, private val channel: String = "messages") : EventDispatcher {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var client: KredsClient
    private val json = Json

    init {
        client = newClient(Endpoint(host, port))
    }

    override suspend fun dispatch(message: Serializable) {
        scope.launch {
            client.lpush(channel, json.encodeToString(message))
        }
    }

    override suspend fun dispatch(message: String) {
        scope.launch {
            client.lpush(channel, message)
        }
    }

    override suspend fun dispatch(message: ByteArray) {
        scope.launch {
            client.lpush(channel, message.decodeToString())
        }
    }
}