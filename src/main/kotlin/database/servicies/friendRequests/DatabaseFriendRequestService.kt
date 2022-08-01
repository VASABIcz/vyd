package database.servicies.friendRequests

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.dsl.neq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

class DatabaseFriendRequestService(private val database: Database) :
    FriendRequestService {
    private val requests get() = database.sequenceOf(DatabaseFriendRequests)

    override suspend fun createRequest(requester: Int, receiver: Int): Int? = withContext(Dispatchers.IO) {
        if (requester == receiver) {
            return@withContext null
        }

        val request = getRequestState(requester, receiver)
        return@withContext if (request?.state == FriendRequestState.accepted || request?.state == FriendRequestState.declined || request == null) {
            return@withContext database.insertAndGenerateKey(DatabaseFriendRequests) {
                set(DatabaseFriendRequests.requester, requester)
                set(DatabaseFriendRequests.receiver, receiver)
                set(DatabaseFriendRequests.state, FriendRequestState.pending)
            } as Int?
        } else {
            null
        }
    }

    override suspend fun getRequestState(requester: Int, receiver: Int): DatabaseFriendRequest? =
        withContext(Dispatchers.IO) {
            return@withContext requests.find {
                (DatabaseFriendRequests.state eq FriendRequestState.pending) and (DatabaseFriendRequests.requester eq requester) and (DatabaseFriendRequests.receiver eq receiver)
            }
                ?: requests.find {
                    (DatabaseFriendRequests.state neq FriendRequestState.pending) and (DatabaseFriendRequests.requester eq requester) and (DatabaseFriendRequests.receiver eq receiver)
                }
        }

    override suspend fun getRequestState(id: Int): DatabaseFriendRequest? = withContext(Dispatchers.IO) {
        return@withContext requests.find {
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

    override suspend fun changePendingRequestState(id: Int, state: FriendRequestState, receiver: Int): Boolean =
        withContext(Dispatchers.IO) {
            val request = getRequestState(id) ?: return@withContext false

            return@withContext if (request.state == FriendRequestState.pending && request.receiver.id == receiver) {
                request.state = state
                return@withContext request.flushChanges() != 0
            } else {
                false
            }
        }

    override suspend fun getPendingRequests(receiver: Int): List<DatabaseFriendRequest> = withContext(Dispatchers.IO) {
        return@withContext requests.filter {
            (DatabaseFriendRequests.receiver eq receiver) and (DatabaseFriendRequests.state eq FriendRequestState.pending)
        }.toList()
    }
}