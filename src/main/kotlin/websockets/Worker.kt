package websockets

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.server.websocket.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking


data class Client(val id: Int, val ws: DefaultWebSocketServerSession)

data class Message(val client: String, val msg: String, val channel: String)


fun main() = runBlocking {
    val con = ConnectionManagerImpl("localhost", 9001)
    val goSub = GoSubImpl(con)
    goSub.connect(listOf("vasabi"))
    goSub.subscribe(listOf("vasabi"), listOf("test"))
    goSub.publish("test", "hello")
    val msg = goSub.read()
    println(msg)
}


interface GoSub {
    suspend fun read(): Message
    suspend fun subscribe(clients: List<String>, channels: List<String>)
    suspend fun unSubscribe(clients: List<String>, channels: List<String>)
    suspend fun publish(channel: String, message: String)
    suspend fun connect(clients: List<String>)
    suspend fun disconnect(clients: List<String>)

    companion object {
        val subscribe = 'a'
        val unsubscribe = 'b'
        val connect = 'c'
        val disconnect = 'd'
        val publish = 'e'
        val destroy = 'f'
    }
}

interface ConnectionManager {
    suspend fun getConnection(): Connection

    suspend fun getOutput(): ByteWriteChannel

    suspend fun getInput(): ByteReadChannel

    suspend fun flush()
}

class GoSubImpl(private val con: ConnectionManager) : GoSub {
    private suspend fun writeList(list: List<String>) {
        con.getOutput().writeInt(list.size)
        for (str in list) {
            writeString(str)
        }
        con.getOutput().flush()
    }

    private suspend fun writeString(str: String) {
        con.getOutput().writeInt(str.length)
        con.getOutput().writeAvailable(str.toByteArray())
    }

    private suspend fun writeChar(c: Char) {
        con.getOutput().writeByte(c.code.toByte())
    }

    override suspend fun read(): Message {
        val size1 = con.getInput().readInt()
        val str1 = ByteArray(size1)
        con.getInput().readFully(str1)
        val size2 = con.getInput().readInt()
        val str2 = ByteArray(size2)
        con.getInput().readFully(str2)
        val size3 = con.getInput().readInt()
        val str3 = ByteArray(size3)
        con.getInput().readFully(str3)

        return Message(channel = str1.decodeToString(), client = str2.decodeToString(), msg = str3.decodeToString())
    }

    override suspend fun subscribe(clients: List<String>, channels: List<String>) {
        writeChar(GoSub.subscribe)
        writeList(clients)
        writeList(channels)
        con.flush()
    }

    override suspend fun unSubscribe(clients: List<String>, channels: List<String>) {
        writeChar(GoSub.unsubscribe)
        writeList(clients)
        writeList(channels)
        con.flush()
    }

    override suspend fun publish(channel: String, message: String) {
        writeChar(GoSub.publish)
        writeString(channel)
        writeString(message)
        con.flush()
    }

    override suspend fun connect(clients: List<String>) {
        writeChar(GoSub.connect)
        writeList(clients)
        con.flush()
    }

    override suspend fun disconnect(clients: List<String>) {
        writeChar(GoSub.disconnect)
        writeList(clients)
        con.flush()
    }

}

class ConnectionManagerImpl(val host: String, val port: Int) : ConnectionManager {
    val selectorManager = SelectorManager(Dispatchers.IO)
    val base = aSocket(selectorManager).tcp()
    var socket: Connection? = null
    override suspend fun getConnection(): Connection {
        return if (socket == null) {
            socket = base.connect(host, port).connection()
            socket!!
        } else if (socket!!.socket.isClosed) {
            socket = base.connect(host, port).connection()
            socket!!
        } else {
            socket!!
        }
    }

    override suspend fun getOutput(): ByteWriteChannel {
        return getConnection().output
    }

    override suspend fun getInput(): ByteReadChannel {
        return getConnection().input
    }

    override suspend fun flush() {
        getConnection().output.flush()
    }
}

/*
class Worker(
    host: String,
    port: Int,
    private val guildMemberService: GuildMemberService,
    private val friendService: FriendService,
) {
    private val clients = HashMap<Int, Client>()
    private val con = ConnectionManagerImpl(host, port)

    suspend fun writeList(list: List<String>) {
        con.getOutput().writeInt(list.size)
        for (str in list) {
            writeString(str)
        }
        con.getOutput().flush()
    }
    suspend fun writeString(str: String) {
        con.getOutput().writeInt(str.length)
        con.getOutput().writeAvailable(str.toByteArray())
    }

    suspend fun subscribe(client: Int, channels: List<String>) {
        con.getOutput().writeByte(subscribe.code.toByte())
        writeList(listOf(client.toString()))
        writeList(channels)
    }

    suspend fun connect(client: Int) {
        con.getOutput().writeByte(connect.code.toByte())
        writeList(listOf(client.toString()))
    }

    suspend fun disconnect(client: Int) {
        con.getOutput().writeByte(disconnect.code.toByte())
        writeList(listOf(client.toString()))
    }

    suspend fun receive(): Message? {
        val size1 = con.getInput().readInt()
        val str1 = ByteArray(size1)
        con.getInput().readFully(str1)
        val size2 = con.getInput().readInt()
        val str2 = ByteArray(size2)
        con.getInput().readFully(str2)
        val size3 = con.getInput().readInt()
        val str3 = ByteArray(size3)
        con.getInput().readFully(str3)

        return Message(channel = str1.decodeToString(), client = str2.decodeToString(), msg = str3.decodeToString())
    }

    suspend fun setupClient(client: Client) {
        val topics = mutableListOf<String>()

        topics.addAll(guildMemberService.getGuilds(client.id).map {
            "guild:${it.guild.id}"
        })
        topics.addAll(friendService.getFriends(client.id).map {
            "user:${it.friend(client.id).id}"
        })
        topics.add("me:${client.id}")
        connect(client.id)
        subscribe(client.id, topics)
    }

    suspend fun addClient(client: Client) {
        clients[client.id] = client
        setupClient(client)
    }

    suspend fun removeClient(client: Int) {
        val c = clients.remove(client)
        c?.ws?.close()
        disconnect(client)
    }

    suspend fun work() {
        while (true) {
            val msg = receive() ?: continue
            val id = msg.client.toIntOrNull() ?: continue
            val client = clients[id]
            if (client == null) {
                removeClient(id)
                continue
            }
            try {
                client.ws.send(Frame.Text(msg.msg))
            }
            catch (t: Throwable) {
                removeClient(id)
            }
        }
    }
}

 */