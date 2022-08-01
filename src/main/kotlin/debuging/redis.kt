package debuging

import database.servicies.avatars.AvatarOwner
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsSubscriber
import io.github.crackthecodeabhi.kreds.connection.newSubscriberClient
import kotlinx.coroutines.runBlocking

object handl: KredsSubscriber {
    override fun onException(ex: Throwable) {
    }

    override fun onMessage(channel: String, message: String) {
        println("$channel message $message")
    }

    override fun onPMessage(pattern: String, channel: String, message: String) {
    }

    override fun onPSubscribe(pattern: String, subscribedChannels: Long) {
    }

    override fun onPUnsubscribe(pattern: String, subscribedChannels: Long) {
    }

    override fun onSubscribe(channel: String, subscribedChannels: Long) {
    }

    override fun onUnsubscribe(channel: String, subscribedChannels: Long) {
    }

}

fun main() = runBlocking {
    val redisClient = newSubscriberClient(Endpoint.from("192.168.0.101:6379"), handl)
    redisClient.subscribe("guild:6")
}