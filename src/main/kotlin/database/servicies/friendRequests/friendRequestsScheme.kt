package database.servicies.friendRequests

import data.responses.FriendRequestsRequest
import database.servicies.users.DatabaseUser
import database.servicies.users.DatabaseUsers
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.enum
import org.ktorm.schema.int
import org.ktorm.schema.timestamp
import java.time.Instant

@kotlinx.serialization.Serializable
enum class FriendRequestState {
    accepted,
    pending,
    declined
}

@kotlinx.serialization.Serializable
enum class FriendRequestResponse {
    accept,
    decline
}

fun FriendRequestResponse.toFriendRequestState(): FriendRequestState {
    return when (this) {
        FriendRequestResponse.accept -> FriendRequestState.accepted
        FriendRequestResponse.decline -> FriendRequestState.declined
    }
}

interface DatabaseFriendRequest : Entity<DatabaseFriendRequest> {
    companion object : Entity.Factory<DatabaseFriendRequest>()

    val id: Int
    val requester: DatabaseUser
    val receiver: DatabaseUser
    var state: FriendRequestState
    val timestamp: Instant

    fun toFriendRequestsRequest(): FriendRequestsRequest {
        return FriendRequestsRequest(id, requester.toUsersUser(), timestamp.toEpochMilli())
    }
}

object DatabaseFriendRequests : Table<DatabaseFriendRequest>("friend_requests") {
    val id = int("id").primaryKey().bindTo { it.id }
    val requester = int("requester").references(DatabaseUsers) { it.requester }
    val receiver = int("receiver").references(DatabaseUsers) { it.receiver }
    val state = enum<FriendRequestState>("state").bindTo { it.state }
    val timestamp = timestamp("[timestamp]").bindTo { it.timestamp }
}