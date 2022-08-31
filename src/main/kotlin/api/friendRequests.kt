package api

import Converter
import Parameters
import Responses.Companion.badRequest
import Responses.Companion.serverIssue
import Responses.Companion.success
import database.servicies.friendRequests.FriendRequestService
import database.servicies.users.userId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wrapers.FriendRequestWrapper


fun Application.friendRequests(
    friendRequestService: FriendRequestService,
    friendRequestWrapper: FriendRequestWrapper
) {
    routing {
        route("friendrequests") {
            authenticate {
                get {
                    val me = call.principal<JWTPrincipal>()!!.userId!!

                    val requests = friendRequestService.getPendingRequests(me).map {
                        it.toFriendRequestsRequest()
                    }

                    call.respond(requests)
                }

                route("create") {
                    // TODO
                    post("{user_id}") {
                        val p = Parameters(call.parameters)
                        val userId by p.parameter("user_id", Converter.Int)
                        if (!p.isValid) {
                            return@post call.badRequest(p.getIssues)
                        }

                        val me = call.principal<JWTPrincipal>()!!.userId!!

                        if (friendRequestService.createRequest(me, userId!!) != null) {
                            call.success()
                        } else {
                            call.serverIssue()
                        }
                    }
                }

                route("request") {
                    route("{request_id}") {
                        patch("accept") {
                            val p = Parameters(call.parameters)
                            val requestId by p.parameter("request_id", Converter.Int)
                            if (!p.isValid) {
                                return@patch call.badRequest(p.getIssues)
                            }

                            val me = call.principal<JWTPrincipal>()!!.userId!!

                            if (friendRequestWrapper.acceptFriendRequest(requestId!!, me)) {
                                call.success()
                            } else {
                                call.serverIssue()
                            }
                        }
                        patch("reject") {
                            val p = Parameters(call.parameters)
                            val requestId by p.parameter("request_id", Converter.Int)
                            if (!p.isValid) {
                                return@patch call.badRequest(p.getIssues)
                            }

                            val me = call.principal<JWTPrincipal>()!!.userId!!

                            if (friendRequestWrapper.declineFriend(requestId!!, me)) {
                                call.success()
                            } else {
                                call.serverIssue()
                            }
                        }
                        get {
                            val p = Parameters(call.parameters)
                            val requestId by p.parameter("request_id", Converter.Int)
                            if (!p.isValid) {
                                return@get call.badRequest(p.getIssues)
                            }

                            val me = call.principal<JWTPrincipal>()!!.userId!!

                            val request = friendRequestService.getRequestState(requestId!!)?.toFriendRequestsRequest() ?: return@get call.serverIssue()

                            call.respond(request)
                        }
                    }
                }
            }
        }
    }
}