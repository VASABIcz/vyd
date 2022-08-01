package database.servicies.avatars

import ifLaunch
import websockets.DispatcherService

class EventAvatarService(private val base: AvatarsService, private val dispatcher: DispatcherService) : AvatarsService {
    override suspend fun createGuildAvatar(id: Int, image: ByteArray): Boolean =
        base.createGuildAvatar(id, image).ifLaunch {
            dispatcher.guildAvatarChange(id)
        }

    override suspend fun createUserAvatar(id: Int, image: ByteArray): Boolean =
        base.createUserAvatar(id, image).ifLaunch {
            dispatcher.userAvatarChange(id)
        }

    override suspend fun getGuildAvatar(id: Int) = base.getGuildAvatar(id)

    override suspend fun getUserAvatar(id: Int): ByteArray? = base.getUserAvatar(id)
}