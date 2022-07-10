package database.servicies.channels

interface ChannelService {
    fun createChannel(type: ChannelType): Int?

    fun deleteChannel(id: Int): Boolean

    fun getChannel(id: Int): Channel?
}