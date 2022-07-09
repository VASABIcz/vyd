import api.*
import api.configuration.configureCallLogging
import api.configuration.configureCors
import api.configuration.configureMetrics
import api.configuration.configureNegotiation
import auth.hash.SHA256HashingService
import auth.token.JwtService
import auth.token.TokenConfig
import database.servicies.channels.DatabaseChannelService
import database.servicies.friendRequests.DatabaseFriendRequestService
import database.servicies.friends.DatabaseFriendService
import database.servicies.guilds.DatabaseGuildChannelService
import database.servicies.guilds.DatabaseGuildMemberService
import database.servicies.guilds.DatabaseGuildService
import database.servicies.messages.DatabaseMessageService
import database.servicies.usernames.DatabaseUsernameService
import database.servicies.users.DatabaseUserService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.database.Database
import wrapers.*


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
        val userService = DatabaseUserService(database)
        val hashingService = SHA256HashingService()
        val tokenService = JwtService()
        val friendService = DatabaseFriendService(database)
        val friendRequestService = DatabaseFriendRequestService(database)
        val guildService = DatabaseGuildService(database)
        val guildMemberService = DatabaseGuildMemberService(database)
        val guildChannelService = DatabaseGuildChannelService(database)
        val channelService = DatabaseChannelService(database)
        val messageService = DatabaseMessageService(database)

        val config = TokenConfig(
            issuer = "http://${System.getenv("host")}:${System.getenv("port").toInt()}",
            audience = "users",
            expiresIn = 1000L * 60L * 60L * 24L * 365L,
            secret = System.getenv("secret")
        )

        val friendRequestWrapper = FriendRequestWrapper(database, friendRequestService, friendService)
        val friendWrapper = FriendWrapper(friendService, messageService)
        val userWrapper = UserWrapper(database, userService, usernameService, hashingService)
        val hashWrapper = HashWrapper(hashingService, userService, tokenService, config)
        val guildWrapper = GuildWrapper(
            database,
            guildService,
            guildMemberService,
            guildChannelService,
            channelService,
            messageService
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

        auth(userService, userWrapper, hashWrapper)
        friends(friendService, friendWrapper)
        users(userService)
        guilds(guildMemberService, guildWrapper)
        friendRequests(friendRequestService, friendRequestWrapper)
    }.start(wait = true)
}