import api.guilds
import auth.hash.HashingService
import auth.hash.SHA256HashingService
import auth.token.JwtService
import auth.token.TokenConfig
import auth.token.TokenService
import database.servicies.guilds.Guild
import database.servicies.usernames.UsernameService
import database.servicies.users.UserService
import io.ktor.server.testing.*
import org.junit.Test
import org.ktorm.database.Database
import wrapers.GuildWrapper
import wrapers.HashWrapper
import wrapers.UserWrapper


class GuildTests {
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
    var channelService = TestingChannelService()
    var guildService = TestGuildService(emptyList<Guild>().toMutableList(), userService)
    var guildMemberService = TestGuildMemberService(userService, guildService)
    var guildChannelService = TestingGuildChannelService(guildService, channelService)
    var messageService = TestingMessageService(userService, channelService)


    var userWrapper: UserWrapper = UserWrapper(database, userService, usernameService, hashingService)
    var hashWrapper: HashWrapper = HashWrapper(hashingService, userService, tokenService, config)
    val token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJ1c2VycyIsImlzcyI6Imh0dHA6Ly8xMjcuMC4wLjE6ODA4MCIsImlkIjoiMCIsImV4cCI6MTY4ODk5NjY5Mn0.l4TaYuTGI6P4SK38dsBLt0_UweBqW-Fd-cs_RiQUzAk"
    val guildWrapper =
        GuildWrapper(database, guildService, guildMemberService, guildChannelService, channelService, messageService)

    @Test
    fun test() = testApplication {
        application {
            guilds(guildMemberService, guildWrapper)
        }
    }
}