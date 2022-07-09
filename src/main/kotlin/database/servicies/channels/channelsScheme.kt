package database.servicies.channels

import data.responses.ChannelsChannel
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.enum
import org.ktorm.schema.int
import org.ktorm.schema.timestamp
import java.time.Instant

@kotlinx.serialization.Serializable
enum class ChannelType {
    friends, text, voice, group, category
}

interface DatabaseChannel : Entity<DatabaseChannel> {
    companion object : Entity.Factory<DatabaseChannel>()

    val id: Int
    val timestamp: Instant
    val type: ChannelType

    fun toChannelsChannel(): ChannelsChannel {
        return ChannelsChannel(id, timestamp.toEpochMilli(), type)
    }
}

object DatabaseChannels : Table<DatabaseChannel>("channels") {
    val id = int("id").primaryKey().bindTo { it.id }
    val timestamp = timestamp("[timestamp]").bindTo { it.timestamp }
    val type = enum<ChannelType>("type").bindTo { it.type }
}