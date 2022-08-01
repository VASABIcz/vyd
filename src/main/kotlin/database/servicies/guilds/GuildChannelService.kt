package database.servicies.guilds

interface GuildChannelService {
    suspend fun createChannel(guild: Int, channel: Int, name: String): Int?

    // fun deleteChannel(guild: Int, channel: Int)
    // FIXME not sure if make custom implementation or depend on ChannelService + foregin key casacade

    suspend fun editChannel(id: Int, guild: Int, name: String): Boolean

    suspend fun getChannels(guild: Int): List<GuildChannel>

    suspend fun getChannel(id: Int, guild: Int): GuildChannel?

    suspend fun getChannels(guild: Int, vararg chans: Int): List<GuildChannel>
}