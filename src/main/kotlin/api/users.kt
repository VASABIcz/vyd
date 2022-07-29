import Responses.Companion.badRequest
import Responses.Companion.notFound
import Responses.Companion.serverIssue
import Responses.Companion.success
import database.servicies.avatars.AvatarOwner
import database.servicies.users.UserService
import database.servicies.users.userId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wrapers.AvatarWrapper
import java.io.InputStream

fun Application.users(
    userService: UserService,
    avatarWrapper: AvatarWrapper
) {
    routing {
        route("users") {
            route("id") {
                route("{user_id}") {
                    get {
                        val p = Parameters(call.parameters)
                        val id by p.parameter("user_id", Converter.Int)
                        if (!p.isValid) {
                            return@get call.badRequest(p.getIssues)
                        }
                        val u = userService.getUser(id!!) ?: return@get call.notFound()
                        call.respond(u.toUsersUser())
                    }
                    route("avatar") {
                        get {
                            val p = Parameters(call.parameters)
                            val id by p.parameter("user_id", Converter.Int)
                            if (!p.isValid) {
                                return@get call.badRequest(p.getIssues)
                            }

                            val avatar = avatarWrapper.getUserAvatar(id!!)

                            call.respondBytes(avatar, ContentType.Image.PNG, HttpStatusCode.OK)
                        }
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
            // TODO refactor
            authenticate {
                route("@me") {
                    get {
                        val me = call.principal<JWTPrincipal>()?.userId!!
                        val user = userService.getUser(me) ?: return@get call.serverIssue()
                        call.respond(user.toUsersUser())
                    }
                    route("avatar") {
                        patch {
                            val me = call.principal<JWTPrincipal>()?.userId!!
                            val user = userService.getUser(me) ?: return@patch call.serverIssue()
                            call.respond(user.toUsersUser())


                            var image: ByteArray
                            withContext(Dispatchers.IO) {
                                call.receive<InputStream>().use {
                                    image = it.readBytes()
                                }
                            }

                            if (avatarWrapper.createAvatar(user.id, AvatarOwner.user, image)) {
                                call.success()
                            } else {
                                call.serverIssue()
                            }
                        }
                    }
                }
            }
        }
    }
}