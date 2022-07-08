package database.servicies.friends

import database.servicies.messages.DatabaseMessage

interface FriendService {
    fun addFriend(user1: Int, user2: Int): Boolean

    fun removeFriend(user1: Int, user2: Int): Boolean

    fun getFriendship(user1: Int, user2: Int): DatabaseFriend?

    fun getFriends(user: Int): Set<DatabaseFriend>

    fun sendMessage(sender: Int, receiver: Int, content: String): Boolean

    fun getMessages(
        user1: Int,
        user2: Int,
        amount: Int = 100,
        offset: Int = 0
    ): List<DatabaseMessage>?
}