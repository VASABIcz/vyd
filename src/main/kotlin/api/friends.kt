package api

import Converter
import Parameters
import Responses.Companion.badRequest
import Responses.Companion.serverIssue
import Responses.Companion.success
import database.servicies.friendRequests.FriendRequestService
import database.servicies.friends.FriendService
import database.servicies.users.UserService
import database.servicies.users.fetchUser
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.friends(
    userService: UserService,
    friendService: FriendService,
    friendRequestService: FriendRequestService
) {
    routing {
        authenticate {
            route("/friends") {
                get {
                    val principal = call.principal<JWTPrincipal>()
                    val me = principal!!.fetchUser(userService)!!

                    val f = friendService.getFriends(me.id)
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

                        val f = friendService.getFriendship(me.id, friendId!!)
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

                        if (friendService.removeFriend(me.id, friendId!!)) {
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

                        if (friendService.addFriend(me.id, friendId!!)) {
                            return@put call.success()
                        } else {
                            return@put call.serverIssue()
                        }
                    }
                }
            }
        }
    }
}