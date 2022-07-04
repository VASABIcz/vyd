package events.dispatcher

interface EventDispatcher {
    suspend fun dispatch(message: kotlinx.serialization.Serializable)

    suspend fun dispatch(message: String)

    suspend fun dispatch(message: ByteArray)
}