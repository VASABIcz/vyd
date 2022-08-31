import Responses.Companion.badRequest
import Responses.Companion.serverIssue
import Responses.Companion.success
import database.servicies.avatars.AvatarOwner
import database.servicies.settings.UserSettingsService
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

fun Application.me(
    userService: UserService,
    avatarWrapper: AvatarWrapper,
    userSettingsService: UserSettingsService
) {
    routing {
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

                        var image: ByteArray
                        withContext(Dispatchers.IO) {
                            call.receive<InputStream>().use {
                                image = it.readBytes()
                            }
                        }

                        if (avatarWrapper.createAvatar(me, AvatarOwner.user, image)) {
                            call.success()
                        } else {
                            call.serverIssue()
                        }
                    }
                    get {
                        val me = call.principal<JWTPrincipal>()?.userId!!

                        val avatar = avatarWrapper.getUserAvatar(me)

                        call.respondBytes(avatar, ContentType.Image.PNG, HttpStatusCode.OK)
                    }
                }
                route("settings") {
                    // TODO
                    get("{key}") {
                        val me = call.principal<JWTPrincipal>()?.userId!!
                        val p = Parameters(call.parameters)
                        val key by p.parameter("key", Converter.String)
                        if (!p.isValid) {
                            return@get call.badRequest(p.getIssues)
                        }
                        userSettingsService.getValue(me, key!!)
                    }
                    patch {

                    }
                    delete {

                    }
                }
            }
        }
    }
}