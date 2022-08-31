package websockets

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class GoPubEventDispatcher(val host: String, val port: Int) : EventDispatcher {
    val selectorManager = SelectorManager(Dispatchers.IO)
    val socket: Socket = runBlocking {
        aSocket(selectorManager).tcp().connect(host, port)
    }

    override suspend fun dispatch(channel: String, message: String) {
        socket.connection().output.writeByte('e'.code.toByte())
        socket.connection().output.writeInt(channel.length)
        socket.connection().output.writeStringUtf8(channel)

        socket.connection().output.writeInt(message.length)
        socket.connection().output.writeStringUtf8(message)
        socket.connection().output.flush()
    }
}