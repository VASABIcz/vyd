package websockets

import data.responses.GuildsChannels
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
sealed interface Event

@Serializable
data class JustEvent(val op: EventType) : Event

private val json = Json {
    coerceInputValues = true
    ignoreUnknownKeys = true
}

@Serializable
data class MessageEvent(
    val author: Int,
    val channel: Int,
    val content: String,
    val op: EventType
) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
data class MessageMetadata(val author: Int, val op: EventType) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
data class GuildAvatarChange(val author: Int, val op: EventType) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
data class UserAvatarChange(val author: Int, val op: EventType) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
data class UserRename(val author: Int, val name: String, val op: EventType) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
data class FriendRequest(
    val sender: Int,
    val receiver: Int,
    val id: Int,
    val op: EventType
) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
data class AddFriend(val friend: Int, val op: EventType) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
data class RemoveFriend(val friend: Int, val op: EventType) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
data class SendDM(val sender: Int, val message: String, val op: EventType) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
data class GuildChannelsUpdate(
    val channels: GuildsChannels,
    val op: EventType
) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
data class GuildMemberLeave(val member: Int, val op: EventType) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
data class GuildMemberJoin(val member: Int, val op: EventType) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
data class GuildMemberNickChange(
    val member: Int,
    val nick: String?,
    val op: EventType
) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
data class GuildDelete(val guild: Int, val op: EventType) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
data class GuildRename(val name: String, val op: EventType) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
data class UserDelete(val user: Int, val op: EventType) : Event {
    val encode: String
        get() = json.encodeToString(this)
}

@Serializable
enum class EventType {
    Message,
    MessageMeta,
    GuildAvatarChange,
    UserAvatarChange,
    UserRename,
    FriendRequest,
    AddFriend,
    SendDm,
    GuildChannelsUpdate,
    GuildMemberLeave,
    GuildMemberJoin,
    GuildDelete,
    GuildRename,
    UserDelete,
    RemoveFriend,
    MemberChangeNick
}