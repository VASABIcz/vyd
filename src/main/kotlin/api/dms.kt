package api

import Converter
import Parameters
import Responses.Companion.badRequest
import Responses.Companion.serverIssue
import Responses.Companion.success
import data.requests.MessagePayload
import database.servicies.dms.DmWrapper
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

suspend fun Boolean.respond(call: ApplicationCall) {
    if (this) {
        call.success()
    } else {
        call.serverIssue()
    }
}

fun Application.dms(
    dmWrapper: DmWrapper,
    avatarWrapper: AvatarWrapper
) {
    routing {
        authenticate {
            route("dms") {
                get {
                    val me = call.principal<JWTPrincipal>()!!.userId!!
                    call.respond(dmWrapper.getDms(me).map { it.toDmMembersMember() })
                }
                route("create") {
                    route("dm") {
                        post("{user_id}") {
                            val me = call.principal<JWTPrincipal>()!!.userId!!
                            val p = Parameters(call.parameters)
                            val user by p.parameter("user_id", Converter.Int)
                            if (!p.isValid) {
                                return@post call.badRequest(p.getIssues)
                            }
                            if (dmWrapper.createDm(me, user!!)) {
                                call.success()
                            } else {
                                call.serverIssue()
                            }
                        }
                    }
                    route("group") {
                        post {
                            val me = call.principal<JWTPrincipal>()!!.userId!!
                            dmWrapper.createGroup(me)
                            call.success()
                        }
                    }
                }
                route("invite") {
                    get("{invite}") {
                        val me = call.principal<JWTPrincipal>()!!.userId!!
                        val p = Parameters(call.parameters)
                        val invite by p.parameter("invite", Converter.String)
                        if (!p.isValid) {
                            return@get call.badRequest(p.getIssues)
                        }
                        if (dmWrapper.inviteJoin(me, invite!!)) {
                            call.success()
                        } else {
                            call.serverIssue()
                        }
                    }
                }
                route("dm") {
                    route("{dm_id}") {
                        get {
                            val me = call.principal<JWTPrincipal>()!!.userId!!
                            val p = Parameters(call.parameters)
                            val dm by p.parameter("dm_id", Converter.Int)
                            if (!p.isValid) {
                                return@get call.badRequest(p.getIssues)
                            }
                            val res = dmWrapper.getDm(me, dm!!) ?: return@get call.serverIssue()
                            call.respond(res)
                        }
                        delete {
                            TODO()
                        }
                        route("messages") {
                            get {
                                val me = call.principal<JWTPrincipal>()!!.userId!!
                                val p = Parameters(call.parameters)
                                val dm by p.parameter("dm_id", Converter.Int)
                                if (!p.isValid) {
                                    return@get call.badRequest(p.getIssues)
                                }
                                val args = Parameters(call.request.queryParameters)
                                val offset by args.parameter("offset", Converter.Int, optional = true)
                                val newer by args.parameter("newer", Converter.Boolean, defaultValue = true)
                                if (!p.isValid) {
                                    return@get call.badRequest(p.getIssues)
                                }

                                val messages =
                                    dmWrapper.getMessages(me, dm!!, offset, newer!!) ?: return@get call.serverIssue()
                                call.respond(messages.map { it.toMessagesMessage() })
                            }
                            route("message") {
                                route("{message_id}") {
                                    patch {
                                        val me = call.principal<JWTPrincipal>()!!.userId!!
                                        val p = Parameters(call.parameters)
                                        val dm by p.parameter("dm_id", Converter.Int)
                                        val message by p.parameter("message_id", Converter.Int)
                                        if (!p.isValid) {
                                            return@patch call.badRequest(p.getIssues)
                                        }
                                        val payload = call.receive<MessagePayload>()
                                        dmWrapper.editMessage(me, dm!!, message!!, payload.content).respond(call)
                                    }
                                    delete {
                                        val me = call.principal<JWTPrincipal>()!!.userId!!
                                        val p = Parameters(call.parameters)
                                        val dm by p.parameter("dm_id", Converter.Int)
                                        val message by p.parameter("message_id", Converter.Int)
                                        if (!p.isValid) {
                                            return@delete call.badRequest(p.getIssues)
                                        }
                                        // FIXME possible bug
                                        dmWrapper.deleteMessage(me, dm!!, message!!).respond(call)
                                    }
                                    get {
                                        val me = call.principal<JWTPrincipal>()!!.userId!!
                                        val p = Parameters(call.parameters)
                                        val dm by p.parameter("dm_id", Converter.Int)
                                        val message by p.parameter("message_id", Converter.Int)
                                        if (!p.isValid) {
                                            return@get call.badRequest(p.getIssues)
                                        }
                                        // FIXME possible bug
                                        val msg =
                                            dmWrapper.getMessage(me, dm!!, message!!) ?: return@get call.serverIssue()

                                        call.respond(msg.toMessagesMessage())
                                    }
                                }
                            }
                        }
                        route("members") {
                            get {
                                val me = call.principal<JWTPrincipal>()!!.userId!!
                                val p = Parameters(call.parameters)
                                val dm by p.parameter("dm_id", Converter.Int)
                                if (!p.isValid) {
                                    return@get call.badRequest(p.getIssues)
                                }
                                val members = dmWrapper.getMembers(me, dm!!)
                                if (members == null) {
                                    call.serverIssue()
                                } else {
                                    call.respond(members.map { it.toDmMembersMember() })
                                }
                            }
                            route("member") {
                                route("{member_id}") {
                                    get {
                                        val me = call.principal<JWTPrincipal>()!!.userId!!
                                        val p = Parameters(call.parameters)
                                        val dm by p.parameter("dm_id", Converter.Int)
                                        val member by p.parameter("member_id", Converter.Int)
                                        if (!p.isValid) {
                                            return@get call.badRequest(p.getIssues)
                                        }
                                        val mem =
                                            dmWrapper.getMember(me, dm!!, member!!) ?: return@get call.serverIssue()
                                        call.respond(mem.toDmMembersMember())
                                    }
                                    delete {
                                        val me = call.principal<JWTPrincipal>()!!.userId!!
                                        val p = Parameters(call.parameters)
                                        val dm by p.parameter("dm_id", Converter.Int)
                                        val member by p.parameter("member_id", Converter.Int)
                                        if (!p.isValid) {
                                            return@delete call.badRequest(p.getIssues)
                                        }
                                        if (dmWrapper.kickMember(me, dm!!, member!!)) {
                                            call.success()
                                        } else {
                                            call.serverIssue()
                                        }
                                    }
                                }
                            }
                        }
                        route("invite") {
                            get {
                                val me = call.principal<JWTPrincipal>()!!.userId!!
                                val p = Parameters(call.parameters)
                                val dm by p.parameter("dm_id", Converter.Int)
                                if (!p.isValid) {
                                    return@get call.badRequest(p.getIssues)
                                }
                                val invite = dmWrapper.getInvite(dm!!, me)
                                if (invite == null) {
                                    call.serverIssue()
                                } else {
                                    call.respond("url" to invite)
                                }
                            }
                        }
                        route("avatar") {
                            get {
                                val me = call.principal<JWTPrincipal>()!!.userId!!
                                val p = Parameters(call.parameters)
                                val dm by p.parameter("dm_id", Converter.Int)
                                if (!p.isValid) {
                                    return@get call.badRequest(p.getIssues)
                                }
                                val avatar = avatarWrapper.getDmAvatar(dm!!)
                                call.respondBytes(avatar, ContentType.Image.PNG, HttpStatusCode.OK)
                            }
                            patch {
                                val me = call.principal<JWTPrincipal>()?.userId!!

                                val p = Parameters(call.parameters)
                                val dm by p.parameter("dm_id", Converter.Int)
                                if (!p.isValid) {
                                    return@patch call.badRequest(p.getIssues)
                                }

                                var image: ByteArray
                                withContext(Dispatchers.IO) {
                                    call.receive<InputStream>().use {
                                        image = it.readBytes()
                                    }
                                }

                                if (dmWrapper.setAvatar(dm!!, image, me)) {
                                    call.success()
                                } else {
                                    call.serverIssue()
                                }
                            }
                            delete {
                                val me = call.principal<JWTPrincipal>()?.userId!!

                                val p = Parameters(call.parameters)
                                val dm by p.parameter("dm_id", Converter.Int)
                                if (!p.isValid) {
                                    return@delete call.badRequest(p.getIssues)
                                }

                                if (dmWrapper.deleteAvatar(dm!!, me)) {
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
}