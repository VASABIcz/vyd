package database

interface FriendService {
    fun addFriend(user1: DatabaseUser, user2: DatabaseUser): Boolean

    fun removeFriend(user1: DatabaseUser, user2: DatabaseUser): Boolean

    fun getFriendship(user1: DatabaseUser, user2: DatabaseUser): DatabaseFriend?

    fun getFriends(user: DatabaseUser): List<DatabaseFriend>

    fun sendMessage(sender: DatabaseUser, receiver: DatabaseUser, content: String): Boolean

    fun getMessages(
        user1: DatabaseUser,
        user2: DatabaseUser,
        amount: Int = 100,
        offset: Int = 0
    ): List<DatabaseMessage>?
}