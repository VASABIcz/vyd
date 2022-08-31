package database.servicies.dms

import database.servicies.channels.ChannelService
import database.servicies.channels.ChannelType
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.inList
import org.ktorm.dsl.insert
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList

class DatabaseDmService(private val database: Database, private val channelService: ChannelService) : DmService {
    private val dms = database.sequenceOf(DatabaseDmChannels)

    override suspend fun createGroup(owner: Int, name: String): Int {
        val i = database.useTransaction {
            val id = channelService.createChannel(ChannelType.category) ?: throw Exception("failed to create channel")
            val i = database.insert(DatabaseDmChannels) {
                set(it.id, id)
                set(it.name, name)
                set(it.creator, owner)
                set(it.type, DmType.group)
            }
            if (i == 0) throw Exception("failed to create category")
            id
        }
        return i
    }

    override suspend fun createFriend(): Int {
        val i = database.useTransaction {
            val id = channelService.createChannel(ChannelType.friends) ?: throw Exception("failed to create channel")
            val i = database.insert(DatabaseDmChannels) {
                set(it.id, id)
                set(it.type, DmType.friends)
            }
            if (i == 0) throw Exception("failed to create friend")
            id
        }
        return i
    }

    override suspend fun createDm(): Int {
        val i = database.useTransaction {
            val id = channelService.createChannel(ChannelType.dm) ?: throw Exception("failed to create channel")
            val i = database.insert(DatabaseDmChannels) {
                set(it.id, id)
                set(it.type, DmType.group)
            }
            if (i == 0) throw Exception("failed to create dm")
            id
        }
        return i
    }

    override suspend fun deleteDm(id: Int): Boolean {
        val dm = dms.find {
            it.id eq id
        } ?: return false
        return dm.delete() != 0
    }

    override suspend fun changeName(id: Int, name: String): Boolean {
        val dm = dms.find {
            (it.id eq id) and (it.type eq DmType.group)
        } ?: return false
        dm.name = name
        return dm.flushChanges() != 0
    }

    override suspend fun changeAvatar(id: Int, avatar: ByteArray): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun switchToFriend(dm: Int): Boolean {
        val dm = dms.find {
            (it.id eq dm) and (it.type eq DmType.dm)
        } ?: return false
        dm.type = DmType.friends
        return dm.flushChanges() != 0
    }

    override suspend fun switchToDm(dm: Int): Boolean {
        val dm = dms.find {
            (it.id eq dm) and (it.type eq DmType.friends)
        } ?: return false
        dm.type = DmType.dm
        return dm.flushChanges() != 0
    }

    override suspend fun getChannel(dm: Int): DmChannel? {
        return dms.find {
            it.id eq dm
        }
    }

    override suspend fun getChannels(vararg dm: Int): List<DmChannel> {
        return dms.filter {
            it.id inList dm.toList()
        }.toList()
    }
}