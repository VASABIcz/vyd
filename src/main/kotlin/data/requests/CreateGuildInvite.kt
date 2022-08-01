package data.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateGuildInvite(val url: String? = null, val maxUses: Int? = null, val expireTimestamp: Long? = null)
