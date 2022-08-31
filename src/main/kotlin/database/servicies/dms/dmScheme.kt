package database.servicies.dms

import data.responses.DmMembersMember
import data.responses.DmsDm
import database.servicies.channels.Channel
import database.servicies.channels.DatabaseChannel
import database.servicies.channels.DatabaseChannels
import database.servicies.users.DatabaseUser
import database.servicies.users.DatabaseUsers
import database.servicies.users.User
import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.Instant

enum class DmType {
    group,
    friends,
    dm
}

interface DmChannel {
    val channel: Channel
    val type: DmType
    val timestamp: Instant

    // val avatar: ByteArray?
    val author: User?
    val name: String?

    fun toDmsDm(): DmsDm {
        return DmsDm(
            channel.toChannelsChannel(),
            type,
            timestamp.toEpochMilli(),
            author?.toUsersUser(),
            name
        )
    }
}

interface DmMember {
    val id: Int
    val user: User
    val dm: DmChannel
    val inviter: User?
    val timestamp: Instant

    fun toDmMembersMember(): DmMembersMember {
        return DmMembersMember(
            id,
            user.toUsersUser(),
            dm.toDmsDm(),
            inviter?.toUsersUser(),
            timestamp.toEpochMilli()
        )
    }
}

interface GroupInvite {
    val group: DmChannel
    val url: String
    val timestamp: Instant
    val expire: Instant
    val author: DmMember

    val isExpired: Boolean
        get() = Instant.now() > expire
}

interface DmAvatar {
    val group: DmChannel
    val avatar: ByteArray
}

interface DatabaseDmChannel : DmChannel, Entity<DatabaseDmChannel> {
    override val author: DatabaseUser?

    // override val avatar: ByteArray?
    override val channel: DatabaseChannel
    override var name: String?
    override val timestamp: Instant
    override var type: DmType
}

interface DatabaseDmMember : DmMember, Entity<DatabaseDmMember> {
    override val id: Int
    override val dm: DatabaseDmChannel
    override val inviter: DatabaseUser
    override val user: DatabaseUser
    override val timestamp: Instant
}

interface DatabaseGroupInvite : GroupInvite, Entity<DatabaseGroupInvite> {
    override val author: DatabaseDmMember
    override val expire: Instant
    override val group: DatabaseDmChannel
    override val url: String
    override val timestamp: Instant
}

interface DatabaseDmAvatar : DmAvatar, Entity<DatabaseDmAvatar> {
    override val group: DatabaseDmChannel
    override val avatar: ByteArray
}

object DatabaseDmChannels : Table<DatabaseDmChannel>("dm_channels") {
    val id = int("channel_id").primaryKey().references(DatabaseChannels) { it.channel }

    // val avatar = blob("avatar").bindTo { it.avatar }
    val creator = int("creator").references(DatabaseUsers) { it.author }
    val name = varchar("name").bindTo { it.name }
    val timestamp = timestamp("timestamo").bindTo { it.timestamp }
    val type = enum<DmType>("type").bindTo { it.type }
}

object DatabaseDmMembers : Table<DatabaseDmMember>("dm_members") {
    val id = int("id").primaryKey().bindTo { it.id }
    val user = int("user_id").references(DatabaseUsers) { it.user }
    val channel = int("channel_id").references(DatabaseDmChannels) { it.dm }
    val inviter = int("inviter").references(DatabaseUsers) { it.inviter }
}

object DatabaseGroupInvites : Table<DatabaseGroupInvite>("dm_invites") {
    val id = int("channel_id").references(DatabaseDmChannels) { it.group }
    val url = varchar("url").primaryKey().bindTo { it.url }
    val author = int("author").references(DatabaseDmMembers) { it.author }
    val timestamp = timestamp("timestamp").bindTo { it.timestamp }
    val expire = timestamp("expire_timestamp").bindTo { it.expire }
}

object DatabaseDmAvatars : Table<DatabaseDmAvatar>("dm_avatars") {
    val id = int("dm_id").references(DatabaseDmChannels) { it.group }
    val avatar = blob("avatar").bindTo { it.avatar }
}