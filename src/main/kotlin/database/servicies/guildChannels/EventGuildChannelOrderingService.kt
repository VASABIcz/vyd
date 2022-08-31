package database.servicies.guildChannels

import ifLaunch
import websockets.DispatcherService

class EventGuildChannelOrderingService(
    private val base: GuildChannelOrderingService,
    private val dispatcher: DispatcherService
) : GuildChannelOrderingService by base {
    override suspend fun update(guild: Int, channels: Chans): Boolean = base.update(guild, channels).ifLaunch {
        dispatcher.updateGuildOrdering(guild, channels.toGuildsChannels())
    }
}