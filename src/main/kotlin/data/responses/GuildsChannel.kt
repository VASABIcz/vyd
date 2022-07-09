package data.responses

@kotlinx.serialization.Serializable
data class GuildsChannel(val guild: GuildsGuild, val name: String, val channel: ChannelsChannel)
