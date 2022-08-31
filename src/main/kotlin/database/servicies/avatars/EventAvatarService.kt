package database.servicies.avatars

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import websockets.DispatcherService

class EventAvatarService(
    private val base: AvatarService,
    private val dispatcher: DispatcherService,
    private val prefix: String
) : AvatarService by base {
    private val scope = CoroutineScope(Dispatchers.IO)
    override suspend fun setAvatar(id: Int, avatar: ByteArray): Boolean {
        return base.setAvatar(id, avatar).also {
            scope.launch {
                dispatcher.avatarChange("$prefix:$id")
            }
        }
    }

    override suspend fun removeAvatar(id: Int): Boolean {
        return base.removeAvatar(id).also {
            scope.launch {
                dispatcher.avatarChange("$prefix:$id")
            }
        }
    }
}