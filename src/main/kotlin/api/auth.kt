package api

import Responses.Companion.serverIssue
import auth.hash.HashingService
import auth.hash.SaltedHash
import auth.token.TokenClaim
import auth.token.TokenConfig
import auth.token.TokenService
import data.requests.SigninUserId
import data.requests.SigninUsername
import data.requests.SignupCredentials
import database.UserService
import database.fetchUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.auth(userService: UserService, hashingService: HashingService, tokenService: TokenService, config: TokenConfig) {
    routing {
        route("/auth/") {
            post("/signup") {
                // todo signup will also gen token
                val creds = call.receive<SignupCredentials>()

                val hash = hashingService.generateSaltedHash(creds.password)
                val usr = userService.createUser(creds.username, hash)
                if (usr != null) {
                    return@post call.respond(usr.toUsersUser())
                } else {
                    return@post call.serverIssue()
                }
            }
            post("/signin") {
                var signinId: SigninUserId? = null
                var signinName: SigninUsername? = null
                try {
                    signinId = call.receiveOrNull<SigninUserId>()
                } catch (_: Throwable) {
                }
                try {
                    signinName = call.receiveOrNull<SigninUsername>()
                } catch (_: Throwable) {

                }

                val user = if (signinName != null) {
                    userService.getUser(signinName.username, signinName.discriminator)
                } else if (signinId != null) {
                    userService.getUser(signinId.id)
                } else {
                    return@post call.respond(HttpStatusCode.BadRequest)
                }

                if (user == null) {
                    return@post call.serverIssue()
                }

                val password = signinName?.password
                    ?: (signinId?.password ?: return@post call.respond(HttpStatusCode.BadRequest))

                val hash = SaltedHash(user.hash.decodeToString(), user.salt.decodeToString())
                if (hashingService.verify(password, hash)) {
                    val token = tokenService.generate(config, TokenClaim("id", user.id.toString()))
                    call.respond(hashMapOf("token" to token))
                } else {
                    return@post call.respond(HttpStatusCode.Unauthorized)
                }
            }
            authenticate(optional = true) {
                get("/test") {
                    val principal = call.principal<JWTPrincipal>()
                    if (principal == null) {
                        return@get call.respondText("unauthorized baka")
                    } else {
                        principal.fetchUser(userService)?.name
                        val username = principal.payload.getClaim("username").asString()
                        val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                        call.respondText("Hello, $username! Token is expired at $expiresAt ms.")
                    }
                }
            }
        }
    }
}
