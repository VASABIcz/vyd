package database.servicies.friends

import database.servicies.messages.Message

interface FriendService {
    suspend fun addFriend(user1: Int, user2: Int): Boolean

    suspend fun removeFriend(user1: Int, user2: Int): Boolean

    suspend fun getFriendship(user1: Int, user2: Int): Friend?

    suspend fun getFriends(user: Int): Set<Friend>

    suspend fun sendMessage(sender: Int, receiver: Int, content: String): Int?

    suspend fun getMessages(
        user1: Int,
        user2: Int,
        amount: Int = 100,
        offset: Int = 0
    ): List<Message>?
}