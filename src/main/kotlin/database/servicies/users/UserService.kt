package database.servicies.users

import auth.hash.SaltedHash

interface UserService {
    fun createUser(username: String, hash: SaltedHash, discriminator: String): Int?

    fun getUser(username: String, discriminator: String): User?

    fun getUser(id: Int): User?

    fun deleteUser(id: Int): Boolean

    fun deleteUser(username: String, discriminator: String): Boolean

    // TODO fun editUser(id: Int, username: String): Boolean
    // TODO fun editUser(username: String, discriminator: String, username: String): Boolean
}