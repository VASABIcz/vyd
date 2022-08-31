package database.servicies.guilds

import ifLaunch
import websockets.DispatcherService

class EventGuildService(private val base: GuildService, private val dispatcher: DispatcherService) :
    GuildService by base {
    override suspend fun deleteGuild(id: Int): Boolean = base.deleteGuild(id).ifLaunch {
        dispatcher.deleteGuild(id)
    }

    override suspend fun renameGuild(id: Int, name: String): Boolean = base.renameGuild(id, name).ifLaunch {
        dispatcher.renameGuild(id, name)
    }
}