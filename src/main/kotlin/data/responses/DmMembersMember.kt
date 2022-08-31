package data.responses

import database.servicies.dms.DmType
import kotlinx.serialization.Serializable

@Serializable
data class DmsDm(
    val channel: ChannelsChannel,
    val type: DmType,
    val timestamp: Long,
    val author: UsersUser?,
    val name: String?
)

@Serializable
data class DmMembersMember(
    val id: Int,
    val user: UsersUser,
    val dm: DmsDm,
    val inviter: UsersUser?,
    val timestamp: Long
)
