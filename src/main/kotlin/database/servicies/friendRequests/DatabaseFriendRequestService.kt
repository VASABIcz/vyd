package database.servicies.friendRequests

import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.neq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

class DatabaseFriendRequestService(private val database: Database) :
    FriendRequestService {
    private val requests get() = database.sequenceOf(DatabaseFriendRequests)

    override fun createRequest(requester: Int, receiver: Int): Boolean {
        if (requester == receiver) {
            return false
        }

        val request = getRequestState(requester, receiver)
        return if (request?.state == FriendRequestState.accepted || request?.state == FriendRequestState.declined || request == null) {
            database.insert(DatabaseFriendRequests) {
                set(DatabaseFriendRequests.requester, requester)
                set(DatabaseFriendRequests.receiver, receiver)
                set(DatabaseFriendRequests.state, FriendRequestState.pending)
            } != 0
        } else {
            false
        }
    }

    override fun getRequestState(requester: Int, receiver: Int): DatabaseFriendRequest? {
        return requests.find {
            (DatabaseFriendRequests.state eq FriendRequestState.pending) and (DatabaseFriendRequests.requester eq requester) and (DatabaseFriendRequests.receiver eq receiver)
        }
            ?: requests.find {
                (DatabaseFriendRequests.state neq FriendRequestState.pending) and (DatabaseFriendRequests.requester eq requester) and (DatabaseFriendRequests.receiver eq receiver)
            }
    }

    override fun getRequestState(id: Int): DatabaseFriendRequest? {
        return requests.find {
            DatabaseFriendRequests.id eq id
        }
    }

    // override fun deletePendingRequest(requester: DatabaseUser, receiver: DatabaseUser): Boolean {
    //     return (requests.find { it.id eq id } ?: return false).delete() != 0
    // }

    // override fun changePendingRequestState(
    //     requester: DatabaseUser,
    //     receiver: DatabaseUser,
    //     state: FriendRequestState
    // ): Boolean {
    //     val request = getRequestState(requester, receiver) ?: return false
    //     return if (request.state == FriendRequestState.pending) {
    //         request.state = state
    //         request.flushChanges() != 0
    //     } else {
    //         false
    //     }
    // }

    override fun changePendingRequestState(id: Int, state: FriendRequestState, receiver: Int): Boolean {
        val request = getRequestState(id) ?: return false

        return if (request.state == FriendRequestState.pending && request.receiver.id == receiver) {
            request.state = state
            return request.flushChanges() != 0
        } else {
            false
        }
    }

    override fun getPendingRequests(receiver: Int): List<DatabaseFriendRequest> {
        return requests.filter {
            (DatabaseFriendRequests.receiver eq receiver) and (DatabaseFriendRequests.state eq FriendRequestState.pending)
        }.toList()
    }
}