import api.configuration.configureNegotiation
import api.configureSecurity
import database.servicies.users.UserService
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import junit.framework.TestCase.assertEquals
import org.junit.Test
import utils.token.TokenConfig


class UserTests {
    var userService: UserService =
        TestUserService(mutableListOf(TestingUser(0, "bob", "0")))
    var config: TokenConfig = TokenConfig(
        issuer = "http://127.0.0.1:8080",
        audience = "users",
        expiresIn = 1000L * 60L * 60L * 24L * 365L,
        secret = "very hidden secret",
        realm = "here"
    )
    val token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJ1c2VycyIsImlzcyI6Imh0dHA6Ly8xMjcuMC4wLjE6ODA4MCIsImlkIjoiMCIsImV4cCI6MTY4ODk5NjY5Mn0.l4TaYuTGI6P4SK38dsBLt0_UweBqW-Fd-cs_RiQUzAk"

    @Test
    fun `users test valid name`() = testApplication {
        application {
            configureNegotiation()
            configureSecurity(config)
            users(userService)
        }
        val test = client.get("users/username/bob/0")

        assertEquals(HttpStatusCode.OK, test.status)
    }

    @Test
    fun `users test valid id`() = testApplication {
        application {
            configureNegotiation()
            configureSecurity(config)
            users(userService)
        }
        val test = client.get("users/id/0")

        assertEquals(HttpStatusCode.OK, test.status)
    }

    @Test
    fun `users test invalid id`() = testApplication {
        application {
            configureNegotiation()
            configureSecurity(config)
            users(userService)
        }

        val test = client.get("users/id/991")

        assertEquals(HttpStatusCode.NotFound, test.status)
    }

    @Test
    fun `users test invalid name`() = testApplication {
        application {
            configureNegotiation()
            configureSecurity(config)
            users(userService)
        }

        val test = client.get("users/username/bobo/21518")

        assertEquals(HttpStatusCode.NotFound, test.status)
    }

    @Test
    fun `auth test valid me`() = testApplication {
        application {
            configureNegotiation()
            configureSecurity(config)
            users(userService)
        }

        val test = client.get("users/@me") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, test.status)
    }

    @Test
    fun `auth test invalid me`() = testApplication {
        application {
            configureNegotiation()
            configureSecurity(config)
            users(userService)
        }

        val test = client.get("users/@me")

        assertEquals(HttpStatusCode.Unauthorized, test.status)
    }
}