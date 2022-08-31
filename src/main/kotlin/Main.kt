import api.*
import api.configuration.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import database.servicies.avatars.DatabaseDefaultAvatarService
import database.servicies.avatars.DatabaseGuildAvatarService
import database.servicies.channels.DatabaseChannelService
import database.servicies.dms.DatabaseDmInviteService
import database.servicies.dms.DatabaseDmMemberService
import database.servicies.dms.DatabaseDmService
import database.servicies.dms.DmWrapper
import database.servicies.friendRequests.DatabaseFriendRequestService
import database.servicies.friendRequests.EventFriendRequestService
import database.servicies.friends.DatabaseFriendService
import database.servicies.friends.EventFriendService
import database.servicies.guildChannels.DatabaseGuildChannelOrderingService
import database.servicies.guildChannels.EventGuildChannelOrderingService
import database.servicies.guilds.*
import database.servicies.messages.DatabaseMessageService
import database.servicies.settings.DatabaseUserSettingsService
import database.servicies.usernames.DatabaseUsernameService
import database.servicies.users.DatabaseUserService
import database.servicies.users.EventUserService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import org.ktorm.database.Database
import org.ktorm.support.mysql.MySqlDialect
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
        val cong = HikariConfig().apply {
            driverClassName = "org.mariadb.jdbc.Driver"
            jdbcUrl = System.getenv("database_uri")
            maximumPoolSize = 20
            isAutoCommit = true
            username = System.getenv("database_username")
            password = System.getenv("database_password")
            validate()
        }
        val database = Database.connect(HikariDataSource(cong), dialect = MySqlDialect())
        /*
        val database = Database.connect(
            System.getenv("database_uri"),
            user = System.getenv("database_username"),
            password = System.getenv("database_password"),
            dialect = MySqlDialect()
        )
         */

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
        val defaultAvatarService = DatabaseDefaultAvatarService(database)
        val guildChannelOrderingService = DatabaseGuildChannelOrderingService(database, guildChannelService)
        val guildInviteService = DatabaseGuildInviteService(database)
        val randomStringService = BasicRandomStringService()

        val eventDispatcher = RedisEventDispatcher(System.getenv("redis_host"))
        val dispatcherService = DispatcherService(eventDispatcher)

        val guildAvatarService = DatabaseGuildAvatarService(database)
        val dmAvatarService = DatabaseGuildAvatarService(database)
        val userAvatarService = DatabaseGuildAvatarService(database)

        val eventUserService = EventUserService(userService, dispatcherService)
        val eventGuildService = EventGuildService(guildService, dispatcherService)
        val eventMemberService = EventGuildMemberService(guildMemberService, dispatcherService)
        val eventFriendService = EventFriendService(friendService, dispatcherService)
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
        val dmMemberService = DatabaseDmMemberService(database)
        val dmInviteService = DatabaseDmInviteService(database)
        val dmService = DatabaseDmService(database, channelService)
        val userSettingsService = DatabaseUserSettingsService(database)
        val dmWrapper = DmWrapper(
            dmMemberService,
            dmInviteService,
            dmService,
            dmAvatarService,
            messageService,
            friendRequestService,
            database,
            userSettingsService,
            guildMemberService
        )
        val avatarWrapper = AvatarWrapper(userAvatarService, guildAvatarService, dmAvatarService, defaultAvatarService)
        //val worker = Worker("localhost", 9001, guildMemberService, friendService)
        launch {
            //worker.work()
        }
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
        guilds(guildMemberService, guildWrapper, avatarWrapper)
        friendRequests(friendRequestService, friendRequestWrapper)
        dms(dmWrapper, avatarWrapper)
        configureWebsockets()
        gateway(guildMemberService, friendService)
        // gate(guildMemberService, friendService, worker)
        configureAndroidCert()
    }.start(wait = true)
}