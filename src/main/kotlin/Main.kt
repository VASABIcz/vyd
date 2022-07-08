import api.auth
import api.configuration.configureCallLogging
import api.configuration.configureCors
import api.configuration.configureMetrics
import api.configuration.configureNegotiation
import api.configureSecurity
import api.friends
import api.users
import auth.hash.SHA256HashingService
import auth.token.JwtService
import auth.token.TokenConfig
import database.servicies.friendRequests.DatabaseFriendRequestService
import database.servicies.friends.DatabaseFriendService
import database.servicies.usernames.DatabaseUsernameService
import database.servicies.users.DatabaseUserService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.database.Database


fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("port").toInt(),
        host = System.getenv("host"),
        watchPaths = listOf("classes")
    ) {
        val database = Database.connect(
            System.getenv("database_uri"),
            user = System.getenv("database_username"),
            password = System.getenv("database_password")
        )
        val usernameService = DatabaseUsernameService(database)
        val userService = DatabaseUserService(database, usernameService)
        val hashingService = SHA256HashingService()
        val tokenService = JwtService()
        val friendService = DatabaseFriendService(database)
        val friendRequestService = DatabaseFriendRequestService(database, friendService)
        val config = TokenConfig(
            issuer = "http://${System.getenv("host")}:${System.getenv("port").toInt()}",
            audience = "users",
            expiresIn = 1000L * 60L * 60L * 24L * 365L,
            secret = System.getenv("secret")
        )
        routing {
            get("/") {
                call.respond("Hello world")
            }
        }
        configureSecurity(config)
        configureNegotiation()
        configureCors()
        configureCallLogging()
        configureMetrics()

        auth(userService, hashingService, tokenService, config)
        friends(userService, friendService, friendRequestService)
        users(userService)
    }.start(wait = true)
}