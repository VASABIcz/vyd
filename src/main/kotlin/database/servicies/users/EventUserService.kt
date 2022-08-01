package database.servicies.users

import ifLaunch
import utils.hash.SaltedHash
import websockets.DispatcherService

class EventUserService(private val base: UserService, private val dispatcher: DispatcherService) : UserService {
    override suspend fun createUser(username: String, hash: SaltedHash, discriminator: String): Int? =
        base.createUser(username, hash, discriminator)

    override suspend fun getUser(username: String, discriminator: String): User? = base.getUser(username, discriminator)

    override suspend fun getUser(id: Int): User? = base.getUser(id)

    override suspend fun deleteUser(id: Int): Boolean = base.deleteUser(id).ifLaunch {
        dispatcher.deleteUser(id)
    }
}