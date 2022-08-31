package websockets

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RedisEventDispatcher(host: String) : EventDispatcher {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var client: KredsClient

    init {
        client = newClient(Endpoint.from(host))
    }

    override suspend fun dispatch(channel: String, message: String) {
        println("dispatching $message")
        scope.launch {
            client.publish(channel, message)
        }
    }
}