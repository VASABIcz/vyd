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

    private fun guildState(id: Int): String {
        return "guild:state:$id"
    }

    private fun meState(id: Int): String {
        return "me:state:$id"
    }

    private fun userState(id: Int): String {
        return "user:state:$id"
    }

    suspend fun userRename(author: Int, name: String) {
        eventDispatcher.dispatch(user(author), UserRename(author, name).encode)
    }

    suspend fun guildAvatarChange(guild: Int) {
        eventDispatcher.dispatch(guild(guild), GuildAvatarChange(guild).encode)
    }

    suspend fun userAvatarChange(user: Int) {
        eventDispatcher.dispatch(guild(user), UserAvatarChange(user).encode)
    }

    suspend fun friendRequest(sender: Int, receiver: Int, id: Int) {
        eventDispatcher.dispatch(me(receiver), FriendRequest(sender, receiver, id).encode)
    }

    suspend fun addFriend(user: Int, friend: Int) {
        eventDispatcher.dispatch(meState(user), AddFriend(friend).encode)
    }

    suspend fun removeFriend(user: Int, friend: Int) {
        eventDispatcher.dispatch(meState(user), RemoveFriend(friend).encode)
    }

    suspend fun sendDM(user: Int, friend: Int, message: String) {
        eventDispatcher.dispatch(me(friend), SendDM(user, message).encode)
    }

    suspend fun updateGuildOrdering(guild: Int, channels: GuildsChannels) {
        eventDispatcher.dispatch(guild(guild), GuildChannelsUpdate(channels).encode)
    }

    suspend fun guildLeave(guild: Int, member: Int) {
        eventDispatcher.dispatch(guildState(guild), GuildMemberLeave(member).encode)
    }

    suspend fun guildJoin(guild: Int, member: Int) {
        eventDispatcher.dispatch(guildState(guild), GuildMemberJoin(member).encode)
    }

    suspend fun guildChangeNick(guild: Int, member: Int, nick: String?) {
        eventDispatcher.dispatch(guild(guild), GuildMemberNickChange(member, nick).encode)
    }

    suspend fun deleteGuild(guild: Int) {
        eventDispatcher.dispatch(guildState(guild), GuildDelete(guild).encode)
    }

    suspend fun renameGuild(guild: Int, name: String) {
        eventDispatcher.dispatch(guild(guild), GuildRename(name).encode)
    }

    suspend fun sendMessage(guild: Int, channel: Int, author: Int, content: String) {
        eventDispatcher.dispatch(guild(guild), MessageEvent(author, channel, content, EventType.Message).encode)
    }

    suspend fun deleteUser(user: Int) {
        eventDispatcher.dispatch(userState(user), UserDelete(user).encode)
    }
}