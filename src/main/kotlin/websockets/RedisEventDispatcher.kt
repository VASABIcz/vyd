package websockets

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RedisEventDispatcher(host: String) : EventDispatcher {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var client: KredsClient

    init {
        client = newClient(Endpoint.from(host))
    }

    override suspend fun dispatch(channel: String, message: String) {
        println(client.clientInfo())
        scope.launch {
            client.publish(channel, message)
        }
    }
}