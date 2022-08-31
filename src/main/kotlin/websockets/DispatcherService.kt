package websockets

import data.responses.GuildsChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

class DispatcherService(private val eventDispatcher: EventDispatcher) {
    private val json = Json
    val scope = CoroutineScope(Dispatchers.IO)

    private fun user(id: Int): String {
        return "friend:$id"
    }

    private fun guild(id: Int): String {
        return "guild:$id"
    }

    private fun me(id: Int): String {
        return "me:$id"
    }

    suspend fun userRename(author: Int, name: String) {
        eventDispatcher.dispatch(user(author), UserRename(author, name, EventType.UserRename).encode)
    }

    suspend fun guildAvatarChange(guild: Int) {
        eventDispatcher.dispatch(guild(guild), GuildAvatarChange(guild, EventType.GuildAvatarChange).encode)
    }

    suspend fun userAvatarChange(user: Int) {
        eventDispatcher.dispatch(user(user), UserAvatarChange(user, EventType.UserAvatarChange).encode)
    }

    suspend fun friendRequest(sender: Int, receiver: Int, id: Int) {
        eventDispatcher.dispatch(me(receiver), FriendRequest(sender, receiver, id, EventType.FriendRequest).encode)
    }

    suspend fun addFriend(user: Int, friend: Int) {
        eventDispatcher.dispatch(me(user), AddFriend(friend, EventType.AddFriend).encode)
    }

    suspend fun removeFriend(user: Int, friend: Int) {
        eventDispatcher.dispatch(me(user), RemoveFriend(friend, EventType.RemoveFriend).encode)
    }

    suspend fun sendDM(user: Int, friend: Int, message: String) {
        eventDispatcher.dispatch(me(friend), SendDM(user, message, EventType.SendDm).encode)
    }

    suspend fun updateGuildOrdering(guild: Int, channels: GuildsChannels) {
        eventDispatcher.dispatch(guild(guild), GuildChannelsUpdate(channels, EventType.GuildChannelsUpdate).encode)
    }

    suspend fun guildLeave(guild: Int, member: Int) {
        eventDispatcher.dispatch(guild(guild), GuildMemberLeave(member, EventType.GuildMemberLeave).encode)
    }

    suspend fun guildJoin(guild: Int, member: Int) {
        eventDispatcher.dispatch(guild(guild), GuildMemberJoin(member, EventType.GuildMemberJoin).encode)
    }

    suspend fun guildChangeNick(guild: Int, member: Int, nick: String?) {
        eventDispatcher.dispatch(guild(guild), GuildMemberNickChange(member, nick, EventType.MemberChangeNick).encode)
    }

    suspend fun deleteGuild(guild: Int) {
        eventDispatcher.dispatch(guild(guild), GuildDelete(guild, EventType.GuildDelete).encode)
    }

    suspend fun renameGuild(guild: Int, name: String) {
        eventDispatcher.dispatch(guild(guild), GuildRename(name, EventType.GuildRename).encode)
    }

    suspend fun sendMessage(guild: Int, channel: Int, author: Int, content: String) {
        eventDispatcher.dispatch(guild(guild), MessageEvent(author, channel, content, EventType.Message).encode)
    }

    suspend fun deleteUser(user: Int) {
        eventDispatcher.dispatch(user(user), UserDelete(user, EventType.UserDelete).encode)
    }

    suspend fun avatarChange(to: String) {
        TODO()
        // eventDispatcher.dispatch(to, GuildAvatarChange(user, EventType.UserDelete).encode)
    }
}