package database.servicies.users

import utils.hash.SaltedHash

interface UserService {
    suspend fun createUser(username: String, hash: SaltedHash, discriminator: String): Int?

    suspend fun getUser(username: String, discriminator: String): User?

    suspend fun getUser(id: Int): User?

    suspend fun deleteUser(id: Int): Boolean

    // TODO fun editUser(id: Int, username: String): Boolean
    // TODO fun editUser(username: String, discriminator: String, username: String): Boolean
}