package data.responses

import database.servicies.channels.ChannelType


@kotlinx.serialization.Serializable
data class ChannelsChannel(val id: Int, val timestamp: Long, val type: ChannelType)
