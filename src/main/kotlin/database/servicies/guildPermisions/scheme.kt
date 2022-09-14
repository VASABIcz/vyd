package database.servicies.guildPermisions

import database.servicies.guilds.*
import database.servicies.users.DatabaseUser
import database.servicies.users.DatabaseUsers
import database.servicies.users.User
import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.Instant

fun Boolean?.applyOr(bool: Boolean?): Boolean? {
    return if (this == null) {
        bool
    } else if (bool == null) {
        this
    } else if (this == false) {
        bool
    } else {
        true
    }
}

interface GuildPermissions {
    // guild
    var admin: Boolean
    var viewChannels: Boolean
    var manageChannels: Boolean
    var manageRoles: Boolean
    var manageEmojis: Boolean
    var viewLogs: Boolean
    var manageWebhooks: Boolean
    var manageGuild: Boolean
}

interface MemberRole {
    val member: GuildMember
    val role: Role
    val timestamp: Instant
    val assigner: User?
}

// TODO IMPLEMENT THIS
interface ChannelPermissionsOverride {
    var viewChannel: Boolean?
    var manageChannel: Boolean?
    var managePermissions: Boolean?
    // var manageWebhooks: Boolean?
}

interface TextChannelPermissions {
    // text
    var sendMessages: Boolean
    var sendEmbeds: Boolean
    var sendAttachments: Boolean
    var addReactions: Boolean
    var sendExternalEmojis: Boolean
    var mentionEveryone: Boolean
    var manageMessages: Boolean
    var viewHistory: Boolean
}

interface TextChannelPermissionsOverride : ChannelPermissionsOverride {
    // text
    val id: Int
    var sendMessages: Boolean?
    var sendEmbeds: Boolean?
    var sendAttachments: Boolean?
    var addReactions: Boolean?
    var sendExternalEmojis: Boolean?
    var mentionEveryone: Boolean?
    var manageMessages: Boolean?
    var viewHistory: Boolean?

    fun applyOr(item: TextChannelPermissionsOverride) {
        this.viewChannel = this.viewChannel.applyOr(item.viewChannel)
        this.manageChannel = this.manageChannel.applyOr(item.manageChannel)
        this.managePermissions = this.managePermissions.applyOr(item.managePermissions)
        this.sendMessages = this.sendMessages.applyOr(item.sendMessages)
        this.sendEmbeds = this.sendEmbeds.applyOr(item.sendEmbeds)
        this.sendAttachments = this.sendAttachments.applyOr(item.sendAttachments)
        this.addReactions = this.addReactions.applyOr(item.addReactions)
        this.sendExternalEmojis = this.sendExternalEmojis.applyOr(item.sendExternalEmojis)
        this.mentionEveryone = this.mentionEveryone.applyOr(item.mentionEveryone)
        this.manageMessages = this.manageMessages.applyOr(item.manageMessages)
        this.viewHistory = this.viewHistory.applyOr(item.viewHistory)
    }
}

interface VoiceChannelPermissions {
    // voice
    var connect: Boolean
    var speak: Boolean
    var streamVideo: Boolean

    // TODO find-out what this is
    // var activities: Boolean?
    var prioritySpeaker: Boolean
    var deafen: Boolean
    var move: Boolean
}

interface MemberPermissions {
    // member
    var createInvite: Boolean
    var changeNickName: Boolean
    var manageNickName: Boolean
    var kickMember: Boolean
    var banMember: Boolean
    var moderate: Boolean
}

interface VoiceChannelPermissionsOverride : ChannelPermissionsOverride {
    // text
    val id: Int
    var connect: Boolean?
    var speak: Boolean?
    var streamVideo: Boolean?

    // TODO find-out what this is
    var activities: Boolean?
    var prioritySpeaker: Boolean?
    var deafen: Boolean?
    var move: Boolean?

    fun applyOr(item: VoiceChannelPermissionsOverride) {
        this.viewChannel = this.viewChannel.applyOr(item.viewChannel)
        this.manageChannel = this.manageChannel.applyOr(item.manageChannel)
        this.managePermissions = this.managePermissions.applyOr(item.managePermissions)
        this.connect = this.connect.applyOr(item.connect)
        this.speak = this.speak.applyOr(item.speak)
        this.streamVideo = this.streamVideo.applyOr(item.streamVideo)
        this.activities = this.activities.applyOr(item.activities)
        this.prioritySpeaker = this.prioritySpeaker.applyOr(item.prioritySpeaker)
        this.deafen = this.deafen.applyOr(item.deafen)
        this.move = this.move.applyOr(item.move)
    }
}

interface Override {
    val guild: Int
    val channel: GuildChannel
    val timestamp: Instant
    val author: User?
}

interface TextChannelRoleOverride {
    val role: Role

    // val guild: Guild
    val channel: GuildChannel
    val permissions: TextChannelPermissionsOverride
    val timestamp: Instant
    val author: User?
}

interface TextChannelMemberOverride {
    val member: GuildMember

    // val guild: Guild
    val channel: GuildChannel
    val permissions: TextChannelPermissionsOverride
    val timestamp: Instant
    val author: User?
}

interface VoiceChannelRoleOverride {
    val role: Role

    // val guild: Guild
    val channel: GuildChannel
    val permissions: VoiceChannelPermissionsOverride
    val timestamp: Instant
    val author: User?
}

interface VoiceChannelMemberOverride {
    val member: GuildMember

    // val guild: Guild
    val channel: GuildChannel
    val permissions: VoiceChannelPermissionsOverride
    val timestamp: Instant
    val author: User?
}

interface Permissions : TextChannelPermissions, VoiceChannelPermissions, GuildPermissions, MemberPermissions {
    fun applyOr(permissions: Permissions) {
        this.admin = this.admin || permissions.admin
        this.viewChannels = this.viewChannels || permissions.viewChannels
        this.manageChannels = this.manageChannels || permissions.manageChannels
        this.manageRoles = this.manageRoles || permissions.manageRoles
        this.manageEmojis = this.manageEmojis || permissions.manageEmojis
        this.viewLogs = this.viewLogs || permissions.viewLogs
        this.manageWebhooks = this.manageWebhooks || permissions.manageWebhooks
        this.manageGuild = this.manageGuild || permissions.manageGuild
        this.sendMessages = this.sendMessages || permissions.sendMessages
        this.sendEmbeds = this.sendEmbeds || permissions.sendEmbeds
        this.sendAttachments = this.sendAttachments || permissions.sendAttachments
        this.addReactions = this.addReactions || permissions.addReactions
        this.sendExternalEmojis = this.sendExternalEmojis || permissions.sendExternalEmojis
        this.mentionEveryone = this.mentionEveryone || permissions.mentionEveryone
        this.manageMessages = this.manageMessages || permissions.manageMessages
        this.viewHistory = this.viewHistory || permissions.viewHistory
        this.connect = this.connect || permissions.connect
        this.speak = this.speak || permissions.speak
        this.streamVideo = this.streamVideo || permissions.streamVideo
        this.prioritySpeaker = this.prioritySpeaker || permissions.prioritySpeaker
        this.deafen = this.deafen || permissions.deafen
        this.move = this.move || permissions.move
        this.createInvite = this.createInvite || permissions.createInvite
        this.changeNickName = this.changeNickName || permissions.changeNickName
        this.manageNickName = this.manageNickName || permissions.manageNickName
        this.kickMember = this.kickMember || permissions.kickMember
        this.banMember = this.banMember || permissions.banMember
        this.moderate = this.moderate || permissions.moderate
    }

    fun setAll(bool: Boolean) {
        this.admin = bool
        this.viewChannels = bool
        this.manageChannels = bool
        this.manageRoles = bool
        this.manageEmojis = bool
        this.viewLogs = bool
        this.manageWebhooks = bool
        this.manageGuild = bool
        this.sendMessages = bool
        this.sendEmbeds = bool
        this.sendAttachments = bool
        this.addReactions = bool
        this.sendExternalEmojis = bool
        this.mentionEveryone = bool
        this.manageMessages = bool
        this.viewHistory = bool
        this.connect = bool
        this.speak = bool
        this.streamVideo = bool
        this.prioritySpeaker = bool
        this.deafen = bool
        this.move = bool
        this.createInvite = bool
        this.changeNickName = bool
        this.manageNickName = bool
        this.kickMember = bool
        this.banMember = bool
        this.moderate = bool
    }

    fun applyOverride(permissions: TextChannelPermissionsOverride) {
        permissions.sendMessages?.also { this.sendMessages = it }
        permissions.sendEmbeds?.also { this.sendEmbeds = it }
        permissions.sendAttachments?.also { this.sendAttachments = it }
        permissions.addReactions?.also { this.addReactions = it }
        permissions.sendExternalEmojis?.also { this.sendExternalEmojis = it }
        permissions.mentionEveryone?.also { this.mentionEveryone = it }
        permissions.manageMessages?.also { this.manageMessages = it }
        permissions.viewHistory?.also { this.viewHistory = it }
    }

    fun applyOverride(permissions: VoiceChannelPermissionsOverride) {
        permissions.connect?.also { this.connect = it }
        permissions.speak?.also { this.speak = it }
        permissions.streamVideo?.also { this.streamVideo = it }
        permissions.prioritySpeaker?.also { this.prioritySpeaker = it }
        permissions.deafen?.also { this.deafen = it }
        permissions.move?.also { this.move = it }

        permissions.viewChannel
        var viewChannel: Boolean?
        var manageChannel: Boolean?
        var managePermissions: Boolean?
    }

    fun applyChannelOverride(permissions: ChannelPermissionsOverride) {
        permissions.viewChannel?.also { this.viewChannels = it }
        permissions.manageChannel?.also { this.manageChannels = it }
        // FIXME propably equivalent
        permissions.managePermissions?.also { this.manageRoles = it }
    }
}

interface Role : Permissions {
    val id: Int
    val guild: Guild
    var name: String
    val author: User
    val timestamp: Instant
}

interface UserPermissions : Permissions {
    val position: Int
    /*
    maybe add permission value
    0 -> guild owner
    -1 / null -> everyone / or make everyone editable idk
     */
}

interface DatabaseGuildRole : Role, Entity<DatabaseGuildRole> {
    companion object : Entity.Factory<DatabaseGuildRole>()

    /*
    override val id: Int
    override val guild: DatabaseGuild
    override var name: String
    override var sendMessages: Boolean
    override var sendEmbeds: Boolean
    override var sendAttachments: Boolean
    override var addReactions: Boolean
    override var sendExternalEmojis: Boolean
    override var mentionEveryone: Boolean
    override var manageMessages: Boolean
    override var viewHistory: Boolean
    override var connect: Boolean
    override var speak: Boolean
    override var streamVideo: Boolean
    override var activities: Boolean?
    override var prioritySpeaker: Boolean
    override var deafen: Boolean
    override var move: Boolean
    override var admin: Boolean
    override var viewChannels: Boolean
    override var manageChannels: Boolean
    override var manageRoles: Boolean
    override var manageEmojis: Boolean
    override var viewLogs: Boolean
    override var manageWebhooks: Boolean
    override var manageGuild: Boolean
    override var createInvite: Boolean
    override var changeNickName: Boolean
    override var manageNickName: Boolean
    override var kickMember: Boolean
    override var banMember: Boolean
    override var moderate: Boolean
    override val author: DatabaseUser

     */
    override val guild: DatabaseGuild
    override val author: DatabaseUser
}

object DatabaseGuildRoles : Table<DatabaseGuildRole>("guild_roles") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val guild = int("guild_id").references(DatabaseGuilds) { it.guild }
    val author = int("author").references(DatabaseUsers) { it.author }
    val timestamp = timestamp("timestamp").bindTo { it.timestamp }

    val admin = boolean("admin").bindTo { it.admin }
    val view_channels = boolean("view_channels").bindTo { it.viewChannels }
    val manage_channels = boolean("manage_channels").bindTo { it.manageChannels }
    val manage_roles = boolean("manage_roles").bindTo { it.manageRoles }
    val manage_emojis = boolean("manage_emojis").bindTo { it.manageEmojis }
    val view_logs = boolean("view_logs").bindTo { it.viewLogs }
    val mange_webhooks = boolean("mange_webhooks").bindTo { it.manageWebhooks }
    val manage_guild = boolean("manage_guild").bindTo { it.manageGuild }

    // voice
    val connect = boolean("connect").bindTo { it.connect }
    val speak = boolean("speak").bindTo { it.speak }
    val stream_video = boolean("stream_video").bindTo { it.streamVideo }
    val priority_speaker = boolean("priority_speaker").bindTo { it.prioritySpeaker }
    val deafen = boolean("deafen").bindTo { it.deafen }
    val move = boolean("move").bindTo { it.move }

    // member
    val create_invites = boolean("create_invites").bindTo { it.createInvite }
    val change_nickname = boolean("change_nickname").bindTo { it.changeNickName }
    val manage_nickname = boolean("manage_nickname").bindTo { it.manageNickName }
    val kick_members = boolean("kick_members").bindTo { it.kickMember }
    val ban_members = boolean("ban_members").bindTo { it.banMember }
    val moderate = boolean("moderate").bindTo { it.moderate }

    // text
    val send_messages = boolean("send_messages").bindTo { it.sendMessages }
    val send_embeds = boolean("send_embeds").bindTo { it.sendEmbeds }
    val send_attachments = boolean("send_attachments").bindTo { it.sendAttachments }
    val add_reactions = boolean("add_reactions").bindTo { it.addReactions }
    val send_external_emojis = boolean("send_external_emojis").bindTo { it.sendExternalEmojis }
    val mention_everyone = boolean("mention_everyone").bindTo { it.mentionEveryone }
    val manage_messages = boolean("manage_messages").bindTo { it.manageMessages }
    val view_history = boolean("view_history").bindTo { it.viewHistory }
}

interface DatabaseTextPermission : Entity<DatabaseTextPermission>, TextChannelPermissionsOverride {
    companion object : Entity.Factory<DatabaseTextPermission>()
}

object DatabaseTextPermissions : Table<DatabaseTextPermission>("text_channel_permissions") {
    val id = int("id").primaryKey().bindTo { it.id }
    val send_messages = boolean("send_messages").bindTo { it.sendMessages }
    val send_embeds = boolean("send_embeds").bindTo { it.sendEmbeds }
    val send_attachments = boolean("send_attachments").bindTo { it.sendAttachments }
    val add_reactions = boolean("add_reactions").bindTo { it.addReactions }
    val send_external_emojis = boolean("send_external_emojis").bindTo { it.sendExternalEmojis }
    val mention_everyone = boolean("mention_everyone").bindTo { it.mentionEveryone }
    val manage_messages = boolean("manage_messages").bindTo { it.manageMessages }
    val view_history = boolean("view_history").bindTo { it.viewHistory }

    val view_channel = boolean("view_channel").bindTo { it.viewChannel }
    val manage_channel = boolean("manage_channel").bindTo { it.manageChannel }
    val manage_permissions = boolean("manage_permissions").bindTo { it.managePermissions }
}

interface DatabaseVoicePermission : Entity<DatabaseVoicePermission>, VoiceChannelPermissionsOverride {
    companion object : Entity.Factory<DatabaseVoicePermission>()
}

object DatabaseVoicePermissions : Table<DatabaseVoicePermission>("voice_channel_permissions") {
    val id = int("id").primaryKey().bindTo { it.id }
    val connect = boolean("connect").bindTo { it.connect }
    val speak = boolean("speak").bindTo { it.speak }
    val stream_video = boolean("stream_video").bindTo { it.streamVideo }
    val priority_speaker = boolean("priority_speaker").bindTo { it.prioritySpeaker }
    val deafen = boolean("deafen").bindTo { it.deafen }
    val move = boolean("move").bindTo { it.move }

    val view_channel = boolean("view_channel").bindTo { it.viewChannel }
    val manage_channel = boolean("manage_channel").bindTo { it.manageChannel }
    val manage_permissions = boolean("manage_permissions").bindTo { it.managePermissions }
}

interface DatabaseVoicePermissionsMember : Entity<DatabaseVoicePermissionsMember>, VoiceChannelMemberOverride {
    companion object : Entity.Factory<DatabaseVoicePermissionsMember>()

    override val author: DatabaseUser?
    override val member: DatabaseMember

    // override val guild: DatabaseGuild
    override val channel: DatabaseGuildChannel
    override val permissions: DatabaseVoicePermission
}

object DatabaseVoicePermissionsMembers : Table<DatabaseVoicePermissionsMember>("voice_permissions_member") {
    val member = int("member").references(DatabaseMembers) { it.member }
    val author = int("author").references(DatabaseUsers) { it.author }

    // val guild = int("guild").references(DatabaseGuilds) { it.guild }
    val channel = int("channel").references(DatabaseGuildChannels) { it.channel }
    val permissions = int("permissions").references(DatabaseVoicePermissions) { it.permissions }
    val timestamp = timestamp("timestamp").bindTo { it.timestamp }
}

interface DatabaseVoicePermissionsRole : Entity<DatabaseVoicePermissionsRole>, VoiceChannelRoleOverride {
    companion object : Entity.Factory<DatabaseVoicePermissionsRole>()

    override val author: DatabaseUser?
    override val role: DatabaseGuildRole

    // override val guild: DatabaseGuild
    override val channel: DatabaseGuildChannel
    override val permissions: DatabaseVoicePermission
}

object DatabaseVoicePermissionsRoles : Table<DatabaseVoicePermissionsRole>("voice_permissions_role") {
    val role = int("role").references(DatabaseGuildRoles) { it.role }
    val author = int("author").references(DatabaseUsers) { it.author }

    // val guild = int("guild").references(DatabaseGuilds) { it.guild }
    val channel = int("channel").references(DatabaseGuildChannels) { it.channel }
    val permissions = int("permissions").references(DatabaseVoicePermissions) { it.permissions }
    val timestamp = timestamp("timestamp").bindTo { it.timestamp }
}

interface DatabaseTextPermissionsMember : Entity<DatabaseTextPermissionsMember>, TextChannelMemberOverride {
    companion object : Entity.Factory<DatabaseTextPermissionsMember>()

    override val author: DatabaseUser?
    override val member: DatabaseMember

    // override val guild: DatabaseGuild
    override val channel: DatabaseGuildChannel
    override val permissions: DatabaseTextPermission
}

object DatabaseTextPermissionsMembers : Table<DatabaseTextPermissionsMember>("text_permissions_members") {
    val member = int("member").references(DatabaseMembers) { it.member }
    val author = int("author").references(DatabaseUsers) { it.author }
    val channel = int("channel").references(DatabaseGuildChannels) { it.channel }
    val permissions = int("permissions").references(DatabaseTextPermissions) { it.permissions }
    val timestamp = timestamp("timestamp").bindTo { it.timestamp }
}

interface DatabaseTextPermissionsRole : Entity<DatabaseTextPermissionsRole>, TextChannelRoleOverride {
    companion object : Entity.Factory<DatabaseTextPermissionsRole>()

    override val author: DatabaseUser?
    override val role: DatabaseGuildRole

    // override val guild: DatabaseGuild
    override val channel: DatabaseGuildChannel
    override val permissions: DatabaseTextPermission
}

object DatabaseTextPermissionsRoles : Table<DatabaseTextPermissionsRole>("text_permissions_role") {
    val role = int("role").references(DatabaseGuildRoles) { it.role }
    val author = int("author").references(DatabaseUsers) { it.author }

    // val guild = int("guild").references(DatabaseGuilds) { it.guild }
    val channel = int("channel").references(DatabaseGuildChannels) { it.channel }
    val permissions = int("permissions").references(DatabaseTextPermissions) { it.permissions }
    val timestamp = timestamp("timestamp").bindTo { it.timestamp }
}

interface DatabaseMemberRole : Entity<DatabaseMemberRole>, MemberRole {
    companion object : Entity.Factory<DatabaseMemberRole>()

    override val member: DatabaseMember
    override val role: DatabaseGuildRole
    override val assigner: DatabaseUser?
}

object DatabaseGuildMemberRoles : Table<DatabaseMemberRole>("guild_member_roles") {
    val member = int("member").references(DatabaseMembers) { it.member }
    val role = int("role").references(DatabaseGuildRoles) { it.role }
    val assigner = int("assigner").references(DatabaseUsers) { it.assigner }
    val timestamp = timestamp("timestamp").bindTo { it.timestamp }
}

interface RolePosition {
    var guild: DatabaseGuild
    var role: DatabaseGuildRole
    var position: Int
}

interface DatabaseRolePosition : Entity<DatabaseRolePosition>, RolePosition {
    companion object : Entity.Factory<DatabaseRolePosition>()
}

object DatabaseRoleOrdering : Table<DatabaseRolePosition>("test_role_ordering") {
    val guild = int("guild").references(DatabaseGuilds) { it.guild }
    val role = int("role").references(DatabaseGuildRoles) { it.role }
    val position = int("position").bindTo { it.position }
}