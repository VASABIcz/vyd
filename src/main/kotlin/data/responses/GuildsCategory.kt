package data.responses

import kotlinx.serialization.Serializable


@Serializable
data class GuildsCategory(val channel: GuildsChannel, val channels: List<GuildsChannel>)
