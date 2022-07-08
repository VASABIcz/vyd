package wrapers

import database.servicies.friendRequests.DatabaseFriendRequestService
import database.servicies.friendRequests.FriendRequestState
import database.servicies.friends.FriendService
import org.ktorm.database.Database

class FriendRequestWrapper(
    private val database: Database,
    private val friendRequestService: DatabaseFriendRequestService,
    private val friendService: FriendService
) {

    fun acceptFriendRequest(requestId: Int, user: Int): Boolean {
        val request = friendRequestService.getRequestState(requestId) ?: return false

        database.useTransaction {
            if (!friendRequestService.changePendingRequestState(requestId, FriendRequestState.accepted, user)) {
                throw Throwable("failed to accept friend request")
            }
            if (!friendService.addFriend(request.requester.id, request.receiver.id)) {
                throw Throwable("failed to add friends")
            }
        }
        return true
    }

    fun declineFriend(requestId: Int, user: Int): Boolean {
        if (!friendRequestService.changePendingRequestState(requestId, FriendRequestState.declined, user)) {
            return false
        }
        return true
    }
}