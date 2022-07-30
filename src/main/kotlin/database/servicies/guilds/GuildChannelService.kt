package database.servicies.guilds

interface GuildChannelService {
    fun createChannel(guild: Int, channel: Int, name: String): Int?

    // fun deleteChannel(guild: Int, channel: Int)
    // FIXME not sure if make custom implementation or depend on ChannelService + foregin key casacade

    fun editChannel(id: Int, guild: Int, name: String): Boolean

    fun getChannels(guild: Int): List<GuildChannel>

    fun getChannel(id: Int, guild: Int): GuildChannel?
}