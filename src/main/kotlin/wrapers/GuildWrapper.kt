package wrapers

import database.servicies.guilds.DatabaseGuildMemberService
import database.servicies.guilds.DatabaseGuildService
import org.ktorm.database.Database

class GuildWrapper(
    private val database: Database,
    private val guildService: DatabaseGuildService,
    private val memberService: DatabaseGuildMemberService
) {
    fun createGuild(owner: Int, name: String): Boolean {
        database.useTransaction {
            val guildId = guildService.createGuild(owner, name) ?: throw Throwable("failed to create guild")
            if (!memberService.joinGuild(owner, guildId)) {
                throw Throwable("failed to join guild")
            }
        }
        return true
    }
}