package api

import Converter
import Parameters
import Responses.Companion.badRequest
import Responses.Companion.notFound
import database.UserService
import database.fetchUser
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.users(
    userService: UserService,
) {
    routing {
        route("/users/") {
            get("/name/{name}/{descriptor}") {
                val p = Parameters(call.parameters)
                val name by p.parameter("name", Converter.String)
                val descriptor by p.parameter("descriptor", Converter.String)
                if (!p.isValid) {
                    return@get call.badRequest(p.getIssues)
                }
                val u = userService.getUser(name!!, descriptor!!) ?: return@get call.notFound()
                call.respond(u.toUsersUser())
            }
            get("/id/{id}") {
                val p = Parameters(call.parameters)
                val id by p.parameter("id", Converter.Int)
                if (!p.isValid) {
                    return@get call.badRequest(p.getIssues)
                }
                val u = userService.getUser(id!!) ?: return@get call.notFound()
                call.respond(u.toUsersUser())
            }
            authenticate {
                get("/@me") {
                    val principal = call.principal<JWTPrincipal>()
                    val me = principal!!.fetchUser(userService)!!

                    call.respond(me.toUsersUser())
                }
            }
        }
    }
}