package database

import database.DatabaseMessages.id
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.neq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

class DatabaseFriendRequestService(private val database: Database) : FriendRequestService {
    private val requests get() = database.sequenceOf(DatabaseFriendRequests)

    override fun createRequest(requester: DatabaseUser, receiver: DatabaseUser): Boolean {
        val request = getRequestState(requester, receiver)
        return if (request?.state == FriendRequestState.accpeted || request?.state == FriendRequestState.canceled || request == null) {
            database.insert(DatabaseFriendRequests) {
                set(it.requester, requester.id)
                set(it.receiver, receiver.id)
                set(it.state, FriendRequestState.pending)
            } != 0
        } else {
            false
        }
    }

    override fun getRequestState(requester: DatabaseUser, receiver: DatabaseUser): DatabaseFriendRequest? {
        return requests.find {
            (it.state eq FriendRequestState.pending) and (it.requester eq requester.id) and (it.receiver eq receiver.id)
        }
            ?: requests.find {
                (it.state neq FriendRequestState.pending) and (it.requester eq requester.id) and (it.receiver eq receiver.id)
            }
    }

    override fun getRequestState(id: Int): DatabaseFriendRequest? {
        return requests.find {
            it.id eq id
        }
    }

    override fun deletePendingRequest(requester: DatabaseUser, receiver: DatabaseUser): Boolean {
        return (requests.find { it.id eq id } ?: return false).delete() != 0
    }

    override fun changePendingRequestState(
        requester: DatabaseUser,
        receiver: DatabaseUser,
        state: FriendRequestState
    ): Boolean {
        val request = getRequestState(requester, receiver) ?: return false

        return if (request.state == FriendRequestState.pending) {
            request.state = state
            request.flushChanges() != 0
        } else {
            false
        }
    }

    override fun changePendingRequestState(id: Int, state: FriendRequestState, receiver: DatabaseUser): Boolean {
        val request = getRequestState(id) ?: return false

        return if (request.state == FriendRequestState.pending && request.receiver == receiver) {
            request.state = state
            request.flushChanges() != 0
        } else {
            false
        }
    }

    override fun getPendingRequests(receiver: DatabaseUser): List<DatabaseFriendRequest> {
        return requests.filter {
            (it.receiver eq receiver.id) and (it.state eq FriendRequestState.pending)
        }.toList()
    }
}