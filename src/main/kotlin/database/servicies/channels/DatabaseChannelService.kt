package database.servicies.channels

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

class DatabaseChannelService(private val database: Database) : ChannelService {
    val channels get() = database.sequenceOf(DatabaseChannels)

    override fun createChannel(type: ChannelType): Int? {
        return database.insertAndGenerateKey(DatabaseChannels) {
            set(it.type, type)
        } as Int?
    }

    override fun deleteChannel(id: Int): Boolean {
        val channel = getChannel(id) ?: return false
        return channel.delete() > 0
    }

    override fun getChannel(id: Int): DatabaseChannel? {
        return channels.find {
            it.id eq id
        }
    }
}