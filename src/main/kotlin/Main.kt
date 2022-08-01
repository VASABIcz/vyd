import api.*
import api.configuration.configureCallLogging
import api.configuration.configureCors
import api.configuration.configureMetrics
import api.configuration.configureNegotiation
import database.servicies.avatars.DatabaseAvatarsService
import database.servicies.avatars.DatabaseDefaultAvatarService
import database.servicies.avatars.EventAvatarService
import database.servicies.channels.DatabaseChannelService
import database.servicies.friendRequests.DatabaseFriendRequestService
import database.servicies.friendRequests.EventFriendRequestService
import database.servicies.friends.DatabaseFriendService
import database.servicies.friends.EventFriendService
import database.servicies.guildChannels.DatabaseGuildChannelOrderingService
import database.servicies.guildChannels.EventGuildChannelOrderingService
import database.servicies.guilds.*
import database.servicies.messages.DatabaseMessageService
import database.servicies.usernames.DatabaseUsernameService
import database.servicies.users.DatabaseUserService
import database.servicies.users.EventUserService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import utils.hash.SHA256HashingService
import utils.random.BasicRandomStringService
import utils.token.JwtService
import utils.token.TokenConfig
import websockets.DispatcherService
import websockets.RedisEventDispatcher
import websockets.gateway.configureWebsockets
import websockets.gateway.gateway
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
            password = System.getenv("database_password"),
            dialect = PostgreSqlDialect()
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
        val avatarService = DatabaseAvatarsService(database)
        val defaultAvatarService = DatabaseDefaultAvatarService(database)
        val guildChannelOrderingService = DatabaseGuildChannelOrderingService(database, guildChannelService)
        val guildInviteService = DatabaseGuildInviteService(database)
        val randomStringService = BasicRandomStringService()

        val eventDispatcher = RedisEventDispatcher(System.getenv("redis_host"))
        val dispatcherService = DispatcherService(eventDispatcher)

        val eventUserService = EventUserService(userService, dispatcherService)
        val eventGuildService = EventGuildService(guildService, dispatcherService)
        val eventMemberService = EventGuildMemberService(guildMemberService, dispatcherService)
        val eventFriendService = EventFriendService(friendService, dispatcherService)
        val eventAvatarService = EventAvatarService(avatarService, dispatcherService)
        val eventFriendRequestService = EventFriendRequestService(friendRequestService, dispatcherService)
        val eventGuildOrdering = EventGuildChannelOrderingService(guildChannelOrderingService, dispatcherService)


        val config = TokenConfig(
            issuer = "http://${System.getenv("host")}:${System.getenv("port").toInt()}",
            audience = "users",
            expiresIn = 1000L * 60L * 60L * 24L * 365L,
            secret = System.getenv("secret"),
            realm = System.getenv("realm") // FIXME not sure about this
        )

        val friendRequestWrapper = FriendRequestWrapper(database, eventFriendRequestService, eventFriendService)
        val friendWrapper = FriendWrapper(eventFriendService, messageService, dispatcherService)
        val userWrapper = UserWrapper(database, eventUserService, usernameService, hashingService)
        val hashWrapper = HashWrapper(hashingService, eventUserService, tokenService, config)
        val guildWrapper = GuildWrapper(
            database,
            eventGuildService,
            eventMemberService,
            guildChannelService,
            channelService,
            messageService,
            eventGuildOrdering,
            guildInviteService,
            randomStringService,
            dispatcherService
        )
        val avatarWrapper = AvatarWrapper(eventAvatarService, defaultAvatarService)

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
        users(userService, avatarWrapper)
        guilds(guildMemberService, guildWrapper)
        friendRequests(friendRequestService, friendRequestWrapper)
        configureWebsockets()
        gateway(guildMemberService, friendService)
    }.start(wait = true)
}