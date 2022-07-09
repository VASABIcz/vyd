package api

import Converter
import Parameters
import Responses.Companion.badRequest
import Responses.Companion.notFound
import Responses.Companion.serverIssue
import database.servicies.users.UserService
import database.servicies.users.userId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.users(
    userService: UserService
) {
    routing {
        route("users") {
            authenticate {
                route("id") {
                    route("{user_id}") {
                        get {
                            val p = Parameters(call.parameters)
                            val id by p.parameter("id", Converter.Int)
                            if (!p.isValid) {
                                return@get call.badRequest(p.getIssues)
                            }
                            val u = userService.getUser(id!!) ?: return@get call.notFound()
                            call.respond(u.toUsersUser())
                        }
                    }
                }
                route("username") {
                    route("{username}") {
                        route("{discriminator}") {
                            get {
                                val p = Parameters(call.parameters)
                                val name by p.parameter("username", Converter.String)
                                val descriptor by p.parameter("discriminator", Converter.String)
                                if (!p.isValid) {
                                    return@get call.badRequest(p.getIssues)
                                }
                                val u = userService.getUser(name!!, descriptor!!) ?: return@get call.notFound()
                                call.respond(u.toUsersUser())
                            }
                        }
                    }
                }
                authenticate {
                    route("@me") {
                        get {
                            val me = call.principal<JWTPrincipal>()?.userId!!
                            val user = userService.getUser(me) ?: return@get call.serverIssue()
                            call.respond(user.toUsersUser())
                        }
                    }
                }
            }
        }
    }
}