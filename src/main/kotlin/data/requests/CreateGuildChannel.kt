package data.requests

import database.servicies.channels.ChannelType
import kotlinx.serialization.Serializable

@Serializable
data class CreateGuildChannel(val name: String, val type: ChannelType, val category: Int? = null)
