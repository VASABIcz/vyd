package api

import Converter
import Parameters
import Responses.Companion.badRequest
import Responses.Companion.serverIssue
import Responses.Companion.success
import data.requests.MessagePayload
import database.FriendService
import database.UserService
import database.fetchUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.friends(userService: UserService, friendService: FriendService) {
    routing {
        authenticate {
            route("/friends") {
                get {
                    val principal = call.principal<JWTPrincipal>()
                    val me = principal!!.fetchUser(userService)!!

                    val f = friendService.getFriends(me)
                    return@get call.respond(f.map { it.toFriendsFriend(me.id) })
                }
                route("{friend}") {
                    get {
                        val p = Parameters(call.parameters)
                        val friendId by p.parameter("friend", Converter.Int)
                        if (!p.isValid) {
                            return@get call.badRequest(p.getIssues)
                        }

                        val principal = call.principal<JWTPrincipal>()
                        val me = principal!!.fetchUser(userService)!!

                        val friend = userService.getUser(friendId!!)
                        val f = friendService.getFriendship(me, friend!!)
                        return@get call.respond(f!!.toFriendsFriend(me.id))
                    }
                    delete {
                        val p = Parameters(call.parameters)
                        val friendId by p.parameter("friend", Converter.Int)
                        if (!p.isValid) {
                            return@delete call.badRequest(p.getIssues)
                        }

                        val principal = call.principal<JWTPrincipal>()
                        val me = principal!!.fetchUser(userService)!!

                        val friend = userService.getUser(friendId!!)

                        if (friendService.removeFriend(me, friend!!)) {
                            return@delete call.success()
                        } else {
                            return@delete call.serverIssue()
                        }
                    }
                    put {
                        val p = Parameters(call.parameters)
                        val friendId by p.parameter("friend", Converter.Int)
                        if (!p.isValid) {
                            return@put call.badRequest(p.getIssues)
                        }

                        val principal = call.principal<JWTPrincipal>()
                        val me = principal!!.fetchUser(userService)!!

                        val friend = userService.getUser(friendId!!)

                        if (friendService.addFriend(me, friend!!)) {
                            return@put call.success()
                        } else {
                            return@put call.serverIssue()
                        }
                    }
                    post("send") {
                        val payload = call.receive<MessagePayload>()
                        val p = Parameters(call.parameters)
                        val friendId by p.parameter("friend", Converter.Int)
                        if (!p.isValid) {
                            return@post call.badRequest(p.getIssues)
                        }

                        val principal = call.principal<JWTPrincipal>()
                        val me = principal!!.fetchUser(userService)!!

                        val friend = userService.getUser(friendId!!)

                        if (friendService.sendMessage(me, friend!!, payload.content)) {
                            return@post call.success()
                        } else {
                            return@post call.serverIssue()
                        }
                    }
                    get("messages") {
                        val p = Parameters(call.parameters)
                        val friendId by p.parameter("friend", Converter.Int)
                        if (!p.isValid) {
                            return@get call.badRequest(p.getIssues)
                        }

                        val principal = call.principal<JWTPrincipal>()
                        val me = principal!!.fetchUser(userService)!!

                        val friend = userService.getUser(friendId!!)

                        val messages =
                            friendService.getMessages(me, friend!!) ?: return@get call.respond(HttpStatusCode.NotFound)

                        return@get call.respond(messages.map {
                            it.toMessagesMessage()
                        })
                    }
                }
            }
        }
    }
}