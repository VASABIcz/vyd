import api.configuration.configureNegotiation
import api.configureSecurity
import auth.hash.HashingService
import auth.hash.SHA256HashingService
import auth.token.JwtService
import auth.token.TokenConfig
import auth.token.TokenService
import data.requests.SigninUserId
import data.requests.SigninUsername
import data.requests.SignupCredentials
import database.servicies.usernames.UsernameService
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
import kotlin.test.assertNotEquals
import api.auth as authRoute


class AuthTests {
    var database: Database = Database.connect(
        "jdbc:sqlite::memory:"
    )
    var config: TokenConfig = TokenConfig(
        issuer = "http://127.0.0.1:8080",
        audience = "users",
        expiresIn = 1000L * 60L * 60L * 24L * 365L,
        secret = "very hidden secret",
        realm = "here"
    )
    var userService: UserService = TestUserService(mutableListOf())
    var usernameService: UsernameService = TestUsernameService()
    var hashingService: HashingService = SHA256HashingService()
    var tokenService: TokenService = JwtService()
    var userWrapper: UserWrapper = UserWrapper(database, userService, usernameService, hashingService)
    var hashWrapper: HashWrapper = HashWrapper(hashingService, userService, tokenService, config)
    val token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJ1c2VycyIsImlzcyI6Imh0dHA6Ly8xMjcuMC4wLjE6ODA4MCIsImlkIjoiMCIsImV4cCI6MTY4ODk5NjY5Mn0.l4TaYuTGI6P4SK38dsBLt0_UweBqW-Fd-cs_RiQUzAk"

    @Test
    fun `auth test invalid token`() = testApplication {
        application {
            configureSecurity(config)
            configureNegotiation()
            authRoute(userService, userWrapper, hashWrapper)
        }

        val test1 = client.get("auth/test")
        assertEquals(HttpStatusCode.OK, test1.status)
        assertEquals("unauthorized baka", test1.body())


        val test2 = client.get("auth/test") {
            bearerAuth("efowiengoeiwngowengwonegowgnowngowng")
        }
        assertEquals(HttpStatusCode.Unauthorized, test2.status)
        assertEquals("", test2.body())
    }

    @Test
    fun `auth test valid token`() = testApplication {
        application {
            configureSecurity(config)
            configureNegotiation()
            authRoute(userService, userWrapper, hashWrapper)
        }
        // test
        val test = client.get("auth/test") {
            bearerAuth(token)
        }
        println(test.body<String>())
        assertEquals(HttpStatusCode.OK, test.status)
    }

    @Test
    fun `auth valid signup`() = testApplication {
        val userService = TestUserService(mutableListOf())
        val usernameService = TestUsernameService()
        val userWrapper = UserWrapper(database, userService, usernameService, hashingService)

        application {
            configureSecurity(config)
            configureNegotiation()
            authRoute(userService, userWrapper, hashWrapper)
        }
        val signup1 = client.post("auth/signup") {
            setBody(Json.encodeToString(SignupCredentials("vasabi", "1234")))
            contentType(ContentType.Application.Json)
        }
        assertEquals(
            "{\n" +
                    "    \"id\": 0,\n" +
                    "    \"name\": \"vasabi\",\n" +
                    "    \"discriminator\": \"0\"\n" +
                    "}", signup1.body()
        )
    }

    @Test
    fun `auth invalid signup`() = testApplication {
        application {
            configureSecurity(config)
            configureNegotiation()
            authRoute(userService, userWrapper, hashWrapper)
        }
        val signup1 = client.post("auth/signup") {
            setBody(Json.encodeToString(SignupCredentials("", "123")))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.InternalServerError, signup1.status)

        val signup2 = client.post("auth/signup") {
            setBody(Json.encodeToString(SignupCredentials("ewgwegwe", "")))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.InternalServerError, signup2.status)

        val signup3 = client.post("auth/signup") {
            setBody(Json.encodeToString(SignupCredentials("", "")))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.InternalServerError, signup3.status)
    }

    @Test
    fun `auth invalid signin`() = testApplication {
        val userService = TestUserService(
            mutableListOf(
                TestingUser(
                    0,
                    "bob",
                    "".encodeToByteArray(),
                    "0",
                    Instant.MIN,
                    "".encodeToByteArray()
                )
            )
        )
        val usernameService = TestUsernameService()
        val userWrapper = UserWrapper(database, userService, usernameService, hashingService)

        application {
            configureSecurity(config)
            configureNegotiation()
            authRoute(userService, userWrapper, hashWrapper)
        }

        val test1 = client.post("auth/signin") {
            setBody(Json.encodeToString(SigninUsername("eherh", "123", "htrhe")))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.InternalServerError, test1.status)

        val test2 = client.post("auth/signin") {
            setBody(Json.encodeToString(SigninUserId(0, "123")))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.InternalServerError, test2.status)

        val test3 = client.post("auth/signin")
        assertEquals(HttpStatusCode.BadRequest, test3.status)
    }

    @Test
    fun `auth valid signin`() = testApplication {
        val userService = TestUserService(mutableListOf())
        val usernameService = TestUsernameService()
        val userWrapper = UserWrapper(database, userService, usernameService, hashingService)
        val hashWrapper = HashWrapper(hashingService, userService, tokenService, config)
        val user = userWrapper.createUser("bobo", "1234")
        assertNotEquals(null, user)

        application {
            configureSecurity(config)
            configureNegotiation()
            authRoute(userService, userWrapper, hashWrapper)
        }

        val test1 = client.post("auth/signin") {
            setBody(Json.encodeToString(SigninUsername("bobo", "0", "1234")))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, test1.status)
        println(test1.body<String>())

        val test2 = client.post("auth/signin") {
            setBody(Json.encodeToString(SigninUserId(0, "1234")))
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, test2.status)
    }
}