package api

import Converter
import Parameters
import Responses.Companion.badRequest
import Responses.Companion.serverIssue
import Responses.Companion.success
import data.requests.MessagePayload
import database.servicies.friends.FriendService
import database.servicies.users.userId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wrapers.FriendWrapper


fun Application.friends(
    friendService: FriendService,
    friendWrapper: FriendWrapper
) {
    routing {
        route("friends") {
            authenticate {
                get {
                    val me = call.principal<JWTPrincipal>()!!.userId!!

                    val f = friendService.getFriends(me)
                    return@get call.respond(f.map { it.toFriendsFriend(me) })
                }
                route("friend") {
                    route("{friend_id}") {
                        get {
                            val p = Parameters(call.parameters)
                            val friendId by p.parameter("friend_id", Converter.Int)
                            if (!p.isValid) {
                                return@get call.badRequest(p.getIssues)
                            }

                            val me = call.principal<JWTPrincipal>()!!.userId!!

                            val f = friendService.getFriendship(me, friendId!!)
                            return@get call.respond(f!!.toFriendsFriend(me))
                        }
                        delete {
                            val p = Parameters(call.parameters)
                            val friendId by p.parameter("friend", Converter.Int)
                            if (!p.isValid) {
                                return@delete call.badRequest(p.getIssues)
                            }

                            val me = call.principal<JWTPrincipal>()!!.userId!!

                            if (friendService.removeFriend(me, friendId!!)) {
                                return@delete call.success()
                            } else {
                                return@delete call.serverIssue()
                            }
                        }
                        route("messages") {
                            get {
                                val p = Parameters(call.parameters)
                                val friendId by p.parameter("friend_id", Converter.Int)
                                if (!p.isValid) {
                                    return@get call.badRequest(p.getIssues)
                                }

                                val q = Parameters(call.request.queryParameters)
                                val amount by q.parameter("amount", Converter.Int, optional = true)
                                val offset by q.parameter("offset", Converter.Int, optional = true)
                                val id by q.parameter("id", Converter.Int, optional = true)
                                // TODO
                                val asc by q.parameter("asc", Converter.Boolean, optional = true)
                                val start by q.parameter("start", Converter.Int, optional = true)
                                val end by q.parameter("end", Converter.Int, optional = true)
                                val author by q.parameter("author", Converter.Int, optional = true)
                                if (!q.isValid) {
                                    return@get call.badRequest(q.getIssues)
                                }

                                val me = call.principal<JWTPrincipal>()!!.userId!!

                                val messages =
                                    friendWrapper.getMessages(me, friendId!!, amount, offset)
                                        ?: return@get call.serverIssue()

                                call.respond(messages)
                            }
                            post {
                                val message = call.receive<MessagePayload>()

                                val p = Parameters(call.parameters)
                                val friendId by p.parameter("friend_id", Converter.Int)
                                if (!p.isValid) {
                                    return@post call.badRequest(p.getIssues)
                                }

                                val me = call.principal<JWTPrincipal>()!!.userId!!

                                if (friendWrapper.sendMessage(me, friendId!!, message.content)) {
                                    call.success()
                                } else {
                                    call.serverIssue()
                                }
                            }
                            route("message") {
                                route("{message_id}") {
                                    get {
                                        val p = Parameters(call.parameters)
                                        val friendId by p.parameter("friend_id", Converter.Int)
                                        val messageId by p.parameter("message_id", Converter.Int)
                                        if (!p.isValid) {
                                            return@get call.badRequest(p.getIssues)
                                        }

                                        val me = call.principal<JWTPrincipal>()!!.userId!!

                                        val message = friendWrapper.getMessage(me, friendId!!, messageId!!)
                                            ?: return@get call.serverIssue()

                                        call.respond(message)
                                    }
                                    delete {
                                        val p = Parameters(call.parameters)
                                        val friendId by p.parameter("friend_id", Converter.Int)
                                        val messageId by p.parameter("message_id", Converter.Int)
                                        if (!p.isValid) {
                                            return@delete call.badRequest(p.getIssues)
                                        }

                                        val me = call.principal<JWTPrincipal>()!!.userId!!

                                        if (friendWrapper.deleteMessage(me, friendId!!, messageId!!)) {
                                            call.success()
                                        } else {
                                            call.serverIssue()
                                        }
                                    }
                                    patch {
                                        val p = Parameters(call.parameters)
                                        val friendId by p.parameter("friend_id", Converter.Int)
                                        val messageId by p.parameter("message_id", Converter.Int)
                                        if (!p.isValid) {
                                            return@patch call.badRequest(p.getIssues)
                                        }

                                        val me = call.principal<JWTPrincipal>()!!.userId!!
                                        // TODO use this in guild
                                        val message = call.receive<MessagePayload>()
                                        // TODO refactor friends route

                                        if (friendWrapper.editMessage(me, friendId!!, messageId!!, message.content)) {
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
    }
}