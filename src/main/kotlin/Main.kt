import api.auth
import api.configuration.configureNegotiation
import api.configureSecurity
import auth.hash.SHA256HashingService
import auth.token.JwtService
import auth.token.TokenConfig
import database.DatabaseUserService
import database.DatabaseUsernameService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.database.Database

/*
me/auth/register
    token

me/auth/login
    token
me/auth/regenerate
    token

me/guilds/
    id
    avatar

me/friends/
    pepega


user/{id}/
    avatar
    name
    is me
        avatar
        name
        register date
        email
        ...
guild/{id}
    name
    avatar
    joined
        channels
            id
            name
            type
        members

channel/{id}
    type
        group
            send
            messages
        friend
            send
            messages
        text
            send
            messages
        voice
            join
            disconnect
            mute
            defen
        ...
    name

 */


fun main() {
    embeddedServer(Netty, port = System.getenv("port").toInt(), host = System.getenv("host")) {
        val database = Database.connect(System.getenv("database_uri"), user = System.getenv("database_username"), password = System.getenv("database_password"))
        val usernameService = DatabaseUsernameService(database)
        val userService = DatabaseUserService(database, usernameService)
        val hashingService = SHA256HashingService()
        val tokenService = JwtService()
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
        auth(userService, hashingService, tokenService, config)
        configureSecurity(config)
        configureNegotiation()
    }.start(wait = true)
}