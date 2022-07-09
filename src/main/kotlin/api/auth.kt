package api

import Responses.Companion.serverIssue
import data.requests.SigninUserId
import data.requests.SigninUsername
import data.requests.SignupCredentials
import database.servicies.users.UserService
import database.servicies.users.fetchUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wrapers.HashWrapper
import wrapers.UserWrapper

fun Application.auth(
    userService: UserService,
    userWrapper: UserWrapper,
    hashWrapper: HashWrapper
) {
    routing {
        route("/auth/") {
            post("/signup") {
                // todo signup will also gen token, maybe returning user is enough c:
                val creds = call.receive<SignupCredentials>()

                val id = userWrapper.createUser(creds.username, creds.password) ?: return@post call.serverIssue()
                val usr = userService.getUser(id) ?: return@post call.serverIssue()

                call.respond(usr.toUsersUser())
            }
            post("/signin") {
                var signinId: SigninUserId? = null
                var signinName: SigninUsername? = null
                try {
                    signinId = call.receiveOrNull()
                } catch (_: Throwable) {
                }
                try {
                    signinName = call.receiveOrNull()
                } catch (_: Throwable) {

                }
                val hash = if (signinName != null) {
                    hashWrapper.createToken(signinName.username, signinName.discriminator, signinName.password)
                } else if (signinId != null) {
                    hashWrapper.createToken(signinId.id, signinId.password)
                } else {
                    return@post call.respond(HttpStatusCode.BadRequest)
                }

                if (hash == null) {
                    return@post call.serverIssue()
                } else {
                    return@post call.respond(mapOf("hash" to hash))
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
