package data.responses

import kotlinx.serialization.Serializable


@Serializable
data class GuildsChannels(val categories: List<GuildsCategory>, val noCategory: List<GuildsChannel>)
