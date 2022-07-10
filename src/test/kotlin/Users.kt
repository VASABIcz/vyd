import api.configuration.configureNegotiation
import api.configureSecurity
import auth.hash.SHA256HashingService
import auth.hash.SaltedHash
import auth.token.JwtService
import auth.token.TokenConfig
import data.requests.SigninUsername
import database.servicies.usernames.UsernameService
import database.servicies.users.User
import database.servicies.users.UserService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import junit.framework.TestCase.assertEquals
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import org.ktorm.database.Database
import wrapers.HashWrapper
import wrapers.UserWrapper
import java.time.Instant
import api.auth as authRoute
import users as usersRoute


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
        val id = ids++
        users.add(
            TestingUser(
                id,
                username,
                hash.hash.encodeToByteArray(),
                discriminator,
                Instant.now(),
                hash.salt.encodeToByteArray()
            )
        )

        return id
    }

    override fun getUser(username: String, discriminator: String): User? {
        return users.find {
            it.name == username && it.discriminator == discriminator
        }
    }

    override fun getUser(id: Int): User? {
        return users.find {
            it.id == id
        }
    }

    override fun deleteUser(id: Int): Boolean {
        users.forEachIndexed { index, user ->
            if (user.id == id) {
                users.removeAt(index)
                return true
            }
        }
        return false
    }

    override fun deleteUser(username: String, discriminator: String): Boolean {
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

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        val database = Database.connect(
            "jdbc:sqlite::memory:"
        )
        val userService = TestUserService(mutableListOf())
        val usernameService = TestUsernameService()
        val hashingService = SHA256HashingService()
        val tokenService = JwtService()
        val config = TokenConfig(
            issuer = "http://127.0.0.1:8080",
            audience = "users",
            expiresIn = 1000L * 60L * 60L * 24L * 365L,
            secret = "very hidden secret",
            realm = "here"
        )

        val userWrapper = UserWrapper(database, userService, usernameService, hashingService)
        val hashWrapper = HashWrapper(hashingService, userService, tokenService, config)

        application {
            configureSecurity(config)
            configureNegotiation()
            usersRoute(userService)
            authRoute(userService, userWrapper, hashWrapper)
        }

        // test
        val test = client.get("auth/test")
        assertEquals("unauthorized baka", test.body())

        val signin1 = client.post("auth/signin") {
            setBody(Json.encodeToString(SigninUsername("vasabi", "0", "123")))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.InternalServerError, signin1.status)
    }
}