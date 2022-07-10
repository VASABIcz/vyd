import auth.hash.SaltedHash
import database.servicies.usernames.UsernameService
import database.servicies.users.User
import database.servicies.users.UserService
import java.time.Instant

data class TestingUser(
    override val id: Int,
    override val name: String,
    override val hash: ByteArray,
    override val discriminator: String,
    override val registerDate: Instant,
    override val salt: ByteArray,
) : User {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestingUser

        if (id != other.id) return false
        if (name != other.name) return false
        if (!hash.contentEquals(other.hash)) return false
        if (discriminator != other.discriminator) return false
        if (registerDate != other.registerDate) return false
        if (!salt.contentEquals(other.salt)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + hash.contentHashCode()
        result = 31 * result + discriminator.hashCode()
        result = 31 * result + registerDate.hashCode()
        result = 31 * result + salt.contentHashCode()
        return result
    }
}

class TestUserService(val users: MutableList<User>) : UserService {
    var ids = 0

    override fun createUser(username: String, hash: SaltedHash, discriminator: String): Int? {
        println(users)
        val id = ids++
        users.add(
            TestingUser(
                id,
                username,
                hash.hash.toByteArray(),
                discriminator,
                Instant.now(),
                hash.salt.toByteArray()
            )
        )

        return id
    }

    override fun getUser(username: String, discriminator: String): User? {
        println(users)
        return users.find {
            it.name == username && it.discriminator == discriminator
        }
    }

    override fun getUser(id: Int): User? {
        println(users)
        return users.find {
            it.id == id
        }
    }

    override fun deleteUser(id: Int): Boolean {
        println(users)
        users.forEachIndexed { index, user ->
            if (user.id == id) {
                users.removeAt(index)
                return true
            }
        }
        return false
    }

    override fun deleteUser(username: String, discriminator: String): Boolean {
        println(users)
        users.forEachIndexed { index, user ->
            if (user.name == username && user.discriminator == discriminator) {
                users.removeAt(index)
                return true
            }
        }
        return false
    }

}

class TestUsernameService : UsernameService {
    var dis = 0

    override fun getDiscriminator(username: String): String? {
        return dis.toString()
    }

    override fun incrementDiscriminator(username: String): Boolean {
        dis++
        return true
    }

}
