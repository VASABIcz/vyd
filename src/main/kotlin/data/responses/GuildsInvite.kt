package data.responses

import kotlinx.serialization.Serializable


@Serializable
data class GuildsInvite(
    val url: String,
    val author: UsersUser,
    val guild: GuildsGuild,
    val uses: Int,
    val timestamp: Long,
    val maxUses: Int? = null,
    val expireTimestamp: Long? = null
)
