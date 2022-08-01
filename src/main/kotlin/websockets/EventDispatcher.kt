package websockets

interface EventDispatcher {
    suspend fun dispatch(channel: String, message: String)
}