package api

import Responses.Companion.serverIssue
import Responses.Companion.success
import auth.hash.HashingService
import auth.token.TokenClaim
import auth.token.TokenConfig
import auth.token.TokenService
import data.requests.Credentials
import database.UserService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*

fun Application.auth(userService: UserService, hashingService: HashingService, tokenService: TokenService, config: TokenConfig) {
    routing {
        install(ContentNegotiation) {
            json()
        }
        route("/auth/") {
            post("/signup") {
                val creds = call.receive<Credentials>()

                val hash = hashingService.generateSaltedHash(creds.password)
                if (userService.createUser(creds.username, hash)) {
                    return@post call.success()
                }
                else {
                    return@post call.serverIssue()
                }
            }
            post("/signin") {
                val user = call.receive<Credentials>()
                val token = tokenService.generate(config, TokenClaim("username", user.username))
                call.respond(hashMapOf("token" to token))
            }
        }
    }
}
