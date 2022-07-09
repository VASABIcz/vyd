package data.responses


@kotlinx.serialization.Serializable
data class MembersMember(val user: UsersUser, val nick: String, val guild: GuildsGuild, val timestamp: Long)
