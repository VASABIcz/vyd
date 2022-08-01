package websockets

import data.responses.GuildsChannels
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed interface Event {
    val op: EventType
}

@Serializable
data class MessageEvent(
    val author: Int,
    val channel: Int,
    val content: String,
    override val op: EventType = EventType.Message
) : Event {
    val encode: String
        get() = Json.encodeToString(this)
}

@Serializable
data class MessageMetadata(val author: Int, override val op: EventType = EventType.MessageMeta) : Event {
    val encode: String
        get() = Json.encodeToString(this)
}

@Serializable
data class GuildAvatarChange(val author: Int, override val op: EventType = EventType.GuildAvatarChange) : Event {
    val encode: String
        get() = Json.encodeToString(this)
}

@Serializable
data class UserAvatarChange(val author: Int, override val op: EventType = EventType.UserAvatarChange) : Event {
    val encode: String
        get() = Json.encodeToString(this)
}

@Serializable
data class UserRename(val author: Int, val name: String, override val op: EventType = EventType.UserRename) : Event {
    val encode: String
        get() = Json.encodeToString(this)
}

@Serializable
data class FriendRequest(
    val sender: Int,
    val receiver: Int,
    val id: Int,
    override val op: EventType = EventType.FriendRequest
) : Event {
    val encode: String
        get() = Json.encodeToString(this)
}

@Serializable
data class AddFriend(val friend: Int, override val op: EventType = EventType.AddFriend) : Event {
    val encode: String
        get() = Json.encodeToString(this)
}

@Serializable
data class RemoveFriend(val friend: Int, override val op: EventType = EventType.AddFriend) : Event {
    val encode: String
        get() = Json.encodeToString(this)
}

@Serializable
data class SendDM(val sender: Int, val message: String, override val op: EventType = EventType.SendDm) : Event {
    val encode: String
        get() = Json.encodeToString(this)
}

@Serializable
data class GuildChannelsUpdate(
    val channels: GuildsChannels,
    override val op: EventType = EventType.GuildChannelsUpdate
) : Event {
    val encode: String
        get() = Json.encodeToString(this)
}

@Serializable
data class GuildMemberLeave(val member: Int, override val op: EventType = EventType.GuildMemberLeave) : Event {
    val encode: String
        get() = Json.encodeToString(this)
}

@Serializable
data class GuildMemberJoin(val member: Int, override val op: EventType = EventType.GuildMemberJoin) : Event {
    val encode: String
        get() = Json.encodeToString(this)
}

@Serializable
data class GuildMemberNickChange(
    val member: Int,
    val nick: String?,
    override val op: EventType = EventType.GuildMemberJoin
) : Event {
    val encode: String
        get() = Json.encodeToString(this)
}

@Serializable
data class GuildDelete(override val op: EventType = EventType.GuildDelete) : Event {
    val encode: String
        get() = Json.encodeToString(this)
}

@Serializable
data class GuildRename(val name: String, override val op: EventType = EventType.GuildRename) : Event {
    val encode: String
        get() = Json.encodeToString(this)
}

@Serializable
data class UserDelete(override val op: EventType = EventType.UserDelete) : Event {
    val encode: String
        get() = Json.encodeToString(this)
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
    UserDelete
}