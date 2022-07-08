package api

import Responses.Companion.serverIssue
import Responses.Companion.success
import data.requests.CreateFriendRequest
import data.requests.RespondFriendRequest
import database.servicies.friendRequests.FriendRequestResponse
import database.servicies.friendRequests.FriendRequestService
import database.servicies.users.UserService
import database.servicies.users.fetchUser
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wrapers.FriendRequestWrapper

fun Application.friendRequests(
    friendRequestService: FriendRequestService,
    userService: UserService,
    friendRequestWrapper: FriendRequestWrapper
) {
    routing {
        authenticate {
            route("friendrequests") {
                put { // accept/decline request
                    val response = call.receive<RespondFriendRequest>()

                    val principal = call.principal<JWTPrincipal>()
                    val me = principal!!.fetchUser(userService)!!

                    if (response.response == FriendRequestResponse.accept) {
                        friendRequestWrapper.acceptFriendRequest(response.id, me.id)
                    } else {
                        friendRequestWrapper.declineFriend(response.id, me.id)
                    }
                }
                post { // create request
                    val response = call.receive<CreateFriendRequest>() // TODO accept username
                    val principal = call.principal<JWTPrincipal>()
                    val me = principal!!.fetchUser(userService)!!

                    if (friendRequestService.createRequest(me.id, response.receiver)) {
                        return@post call.success()
                    } else {
                        return@post call.serverIssue()
                    }
                }
                get {
                    val principal = call.principal<JWTPrincipal>()
                    val me = principal!!.fetchUser(userService)!!

                    call.respond(
                        friendRequestService.getPendingRequests(me.id).map {
                            it.toFriendRequestsRequest()
                        }.toList()
                    )
                }
            }
        }
    }
}