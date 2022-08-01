package database.servicies.guildChannels

import ifLaunch
import websockets.DispatcherService

class EventGuildChannelOrderingService(
    private val base: GuildChannelOrderingService,
    private val dispatcher: DispatcherService
) : GuildChannelOrderingService {
    override suspend fun getChannels(guild: Int): Chans? = base.getChannels(guild)

    override suspend fun moveCategory(
        channel: Int,
        guild: Int,
        position: Int,
        a: GuildChannelOrderingService
    ): Boolean = base.moveCategory(channel, guild, position, this)

    override suspend fun deleteCategory(channel: Int, guild: Int, a: GuildChannelOrderingService): Boolean =
        base.deleteCategory(channel, guild, this)

    override suspend fun createCategory(channel: Int, guild: Int, a: GuildChannelOrderingService): Boolean =
        base.createCategory(channel, guild, this)

    override suspend fun createChannel(
        channel: Int,
        guild: Int,
        category: Int?,
        a: GuildChannelOrderingService
    ): Boolean = base.createChannel(channel, guild, category, this)

    override suspend fun deleteChannel(channel: Int, guild: Int, a: GuildChannelOrderingService): Boolean =
        base.deleteChannel(channel, guild, this)

    override suspend fun moveChannel(
        channel: Int,
        guild: Int,
        category: Int?,
        position: Int,
        a: GuildChannelOrderingService
    ): Boolean = base.moveChannel(channel, guild, category, position, this)

    override suspend fun update(guild: Int, channels: Chans): Boolean = base.update(guild, channels).ifLaunch {
        dispatcher.updateGuildOrdering(guild, channels.toGuildsChannels())
    }

    override suspend fun createRecord(guild: Int): Boolean = base.createRecord(guild)
}