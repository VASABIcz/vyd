package database.servicies.guilds

import ifLaunch
import websockets.DispatcherService

class EventGuildService(private val base: GuildService, private val dispatcher: DispatcherService) : GuildService {
    override suspend fun createGuild(owner: Int, name: String): Int? = base.createGuild(owner, name)

    override suspend fun deleteGuild(id: Int): Boolean = base.deleteGuild(id).ifLaunch {
        dispatcher.deleteGuild(id)
    }

    override suspend fun editGuild(id: Int, name: String): Boolean = base.editGuild(id, name)

    override suspend fun getGuild(id: Int): Guild? = base.getGuild(id)

    override suspend fun renameGuild(id: Int, name: String): Boolean = base.renameGuild(id, name).ifLaunch {
        dispatcher.renameGuild(id, name)
    }
}