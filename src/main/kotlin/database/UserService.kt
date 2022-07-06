package database

import auth.hash.SaltedHash

interface UserService {
    fun createUser(username: String, hash: SaltedHash): DatabaseUser?

    fun getUser(username: String, discriminator: String): DatabaseUser?

    fun getUser(id: Int): DatabaseUser?

    fun deleteUser(id: Int): Boolean

    fun deleteUser(username: String, discriminator: String): Boolean
}