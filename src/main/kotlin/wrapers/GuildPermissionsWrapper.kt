package wrapers

import database.servicies.guildPermisions.*
import database.servicies.guilds.GuildMember
import database.servicies.guilds.GuildMemberService
import org.ktorm.database.Database

@JvmInline
value class MemberId(val id: Int)

@JvmInline
value class UserId(val id: Int)

@JvmInline
value class GuildId(val id: Int)

@JvmInline
value class ChannelId(val id: Int)

@JvmInline
value class MessageId(val id: Int)

data class Channel(val id: Int, val isText: Boolean)

data class UserPermissionState(
    val permissions: Permissions,
    val highestRole: RolePosition,
    val roles: List<RolePosition>,
    val member: GuildMember
)

data class PermissionsData(
    override var admin: Boolean = false,
    override var viewChannels: Boolean = false,
    override var manageChannels: Boolean = false,
    override var manageRoles: Boolean = false,
    override var manageEmojis: Boolean = false,
    override var viewLogs: Boolean = false,
    override var manageWebhooks: Boolean = false,
    override var manageGuild: Boolean = false,
    override var sendMessages: Boolean = false,
    override var sendEmbeds: Boolean = false,
    override var sendAttachments: Boolean = false,
    override var addReactions: Boolean = false,
    override var sendExternalEmojis: Boolean = false,
    override var mentionEveryone: Boolean = false,
    override var manageMessages: Boolean = false,
    override var viewHistory: Boolean = false,
    override var connect: Boolean = false,
    override var speak: Boolean = false,
    override var streamVideo: Boolean = false,
    override var prioritySpeaker: Boolean = false,
    override var deafen: Boolean = false,
    override var move: Boolean = false,
    override var createInvite: Boolean = false,
    override var changeNickName: Boolean = false,
    override var manageNickName: Boolean = false,
    override var kickMember: Boolean = false,
    override var banMember: Boolean = false,
    override var moderate: Boolean = false
) : Permissions

class PermissionManager(
    private val guildMemberPermissionsService: DatabaseGuildMemberPermissionsService,
    private val textChannelMemberPermissionsService: TextChannelMemberPermissionsService,
    private val textChannelRolePermissionsService: TextChannelRolePermissionsService,
    private val voiceChannelMemberPermissionsService: VoiceChannelMemberPermissionsService,
    private val voiceChannelRolePermissionsService: VoiceChannelRolePermissionsService,
    private val guildRolesOrdering: GuildRolesOrdering,
    private val guildMemberService: GuildMemberService,
    private val guildRoleService: DatabaseGuildRolesService
) {
    suspend fun getMember(user: Int, guild: Int): GuildMember? {
        return guildMemberService.getMember(user, guild)
    }

    suspend fun getMember(mem: MemberId): GuildMember? {
        return guildMemberService.getMember(mem.id)
    }

    suspend fun getMajorRole(user: Int, guild: Int): UserPermissionState? {
        val member = getMember(user, guild) ?: return null

        val myRoles = guildMemberPermissionsService.getRoles(member.id)
        val myOrdered = guildRolesOrdering.get(guild, myRoles.map { it.role.id })

        val myPermissions = PermissionsData()

        myOrdered.forEach {
            myPermissions.applyOr(it.role)
        }

        return UserPermissionState(myPermissions, myOrdered[0], myOrdered, member)
    }

    suspend fun getMajorRole(mem: MemberId, guild: Int): UserPermissionState? {
        val member = getMember(mem) ?: return null

        val myRoles = guildMemberPermissionsService.getRoles(member.id)
        val myOrdered = guildRolesOrdering.get(guild, myRoles.map { it.role.id })

        val myPermissions = PermissionsData()

        myOrdered.map {
            myPermissions.applyOr(it.role)
        }

        return UserPermissionState(myPermissions, myOrdered[0], myOrdered, member)
    }

    suspend fun getPermissions(
        user: Int,
        guild: Int,
        channel: Channel? = null,
        role: Int? = null,
        checkOwner: Boolean = true
    ): Permissions? {
        /*
        get all users permission
        not accounting for admin perms

        owner has all permissions

        if role is above users role returns none

        channel overrides are merged to general permissions
         */
        val member = getMember(user, guild) ?: return null // not a member of guild
        if (member.owner && checkOwner) {
            return PermissionsData().apply {
                setAll(true)
            }
        } else {
            val myPerms = getMajorRole(user, guild) ?: return null


            // TODO if user is admin and adminBypass is true ignore role check and return
            // usefull for channel specific perrmission management where admin has all perms
            if (role != null) {
                if (guildRolesOrdering.isHigher(myPerms.highestRole.role.id, role) == false) {
                    return null
                }
            }

            if (channel == null) {
                return myPerms.permissions
            } else {
                if (channel.isText) {
                    val channelPerms = TextPermissionsData()

                    // apply user roles overrides
                    val overrides =
                        textChannelRolePermissionsService.getOverrides(channel.id, myPerms.roles.map { it.role.id })
                    overrides.forEach {
                        channelPerms.applyOr(it.permissions)
                    }
                    // apply member override
                    val memberOverride = textChannelMemberPermissionsService.getOverride(channel.id, member.id)?.also {
                        channelPerms.applyOr(it.permissions)
                    }
                    // apply override
                    return myPerms.permissions.also {
                        it.applyOverride(channelPerms)
                    }
                } else {
                    val channelPerms = VoicePermissionsData()

                    // apply user roles overrides
                    val overrides =
                        voiceChannelRolePermissionsService.getOverrides(channel.id, myPerms.roles.map { it.role.id })
                    overrides.forEach {
                        channelPerms.applyOr(it.permissions)
                    }
                    // apply member override
                    val memberOverride = voiceChannelMemberPermissionsService.getOverride(channel.id, member.id)?.also {
                        channelPerms.applyOr(it.permissions)
                    }
                    // apply override
                    return myPerms.permissions.also {
                        it.applyOverride(channelPerms)
                    }
                }
            }
        }
    }
}

enum class UserGuildState {
    owner,
    member,
    outsider
}

// FIXME we dont check if channel override disables the permission
// TODO extract functionality to function
class GuildPermissionsWrapper(
    private val guildMemberPermissionsService: DatabaseGuildMemberPermissionsService,
    private val textChannelMemberPermissionsService: TextChannelMemberPermissionsService,
    private val textChannelRolePermissionsService: TextChannelRolePermissionsService,
    private val voiceChannelMemberPermissionsService: VoiceChannelMemberPermissionsService,
    private val voiceChannelRolePermissionsService: VoiceChannelRolePermissionsService,
    private val guildRolesOrdering: GuildRolesOrdering,
    private val guildMemberService: GuildMemberService,
    private val guildRoleService: DatabaseGuildRolesService,
    private val database: Database,
    private val permissionManager: PermissionManager
) {
    companion object {
        // TODO
        // FIXME runtime exception
        val defaultPermissions = TODO() as Permissions//PermissionsData()
    }

    suspend fun createRole(user: Int, guild: Int, name: String): Boolean {
        database.useTransaction {
            val id = guildRoleService.createRole(name, user, guild, defaultPermissions) ?: throw Throwable("whops")
            if (!guildRolesOrdering.addBeforeLast(id, guild)) throw Throwable("whops")
        }
        return true
    }

    suspend fun moveGuildRole(user: Int, guild: Int, role: Int, position: Int): Boolean {
        if (position < 0) return false

        val perms = permissionManager.getPermissions(user, guild, role = role) ?: return false

        return if (perms.admin || perms.manageRoles) {
            guildRolesOrdering.move(role, guild, position)
        } else {
            false
        }

        /*
        val member = getMember(user, guild) ?: return false
        return if (member.owner) {
            guildRolesOrdering.move(role, guild, position)
        }
        else {
            val myPerms = getMajorRole(user, guild) ?: return false
            if (myPerms.permissions.admin || myPerms.permissions.manageRoles) {
                val can = guildRolesOrdering.isHigher(myPerms.highestRole.role.id, role) ?: return false
                if (can) {
                    guildRolesOrdering.move(role, guild, position)
                } else {
                    false
                }
            }
            else {
                false
            }
        }

         */

        // bound check
        // user has manage permissions role / admin / owner
        // move is under his role
        TODO()
    }

    suspend fun createGuildRole(user: Int, guild: Int, name: String): Boolean {
        val perms = permissionManager.getPermissions(user, guild) ?: return false

        return if (perms.admin || perms.manageRoles) {
            createRole(user, guild, name)
        } else {
            false
        }

        /*
        return if (member.owner) {
            return createRole(user, guild, name)
        }
        else {
            val myPerms = getMajorRole(user, guild) ?: return false
            if (myPerms.permissions.admin || myPerms.permissions.manageRoles) {
                return createRole(user, guild, name)
            }
            else {
                false
            }
        }

         */
        // user has manage permissions role / admin / owner
        TODO()
    }

    suspend fun editRole(user: Int, guild: Int, role: Int, perms: Permissions): Boolean {
        val _perms = permissionManager.getPermissions(user, guild, role = role) ?: return false
        return if (_perms.manageRoles || _perms.admin) {
            guildRoleService.updateRole(role, perms)
        } else {
            false
        }

        /*
        return if (member.owner) {
            guildRoleService.updateRole(role, perms)
        }
        else {
            val myPerms = getMajorRole(user, guild) ?: return false
            if (myPerms.permissions.admin || myPerms.permissions.manageRoles) {
                val can = guildRolesOrdering.isHigher(myPerms.highestRole.role.id, role) ?: return false
                if (can) {
                    guildRoleService.updateRole(role, perms)
                } else {
                    false
                }
            }
            else {
                false
            }
        }

         */

        // user has manage permissions role / admin / owner
        // move is under his role
        TODO()
    }

    suspend fun addTextChannelRoleOverride(user: Int, channel: Int, guild: Int, role: Int): Boolean {

        // not sure if to set role
        // user cant create channel override for higher roles
        val perms =
            permissionManager.getPermissions(user, guild, role = role, channel = Channel(channel, true)) ?: return false
        return if (perms.manageRoles || perms.admin) {
            textChannelRolePermissionsService.createOverride(channel, role, user)
        } else {
            false
        }

        // mange channels permission
        /*
        val member = getMember(user, guild) ?: return false

        if (member.owner) {
            return textChannelRolePermissionsService.createOverride(channel, role, user)
        }
        else {
            val myPerms = getMajorRole(user, guild) ?: return false
            return if (myPerms.permissions.admin || myPerms.permissions.manageChannels) {
                textChannelRolePermissionsService.createOverride(channel, role, user)
            } else {
                val highestPerm = TextPermissionsData()
                val overrides = textChannelRolePermissionsService.getOverrides(channel, myPerms.roles.map { it.role.id })
                overrides.map {
                    highestPerm.applyOr(it.permissions)
                }
                if (highestPerm.managePermissions == true) {
                    textChannelRolePermissionsService.createOverride(channel, role, user)
                } else {
                    false
                }
            }
        }

         */

        TODO()
    }

    suspend fun addVoiceChannelRoleOverride(user: Int, channel: Int, guild: Int, role: Int): Boolean {
        // mange channels permission

        val perms = permissionManager.getPermissions(user, guild, role = role, channel = Channel(channel, false))
            ?: return false
        return if (perms.manageRoles || perms.admin) {
            voiceChannelRolePermissionsService.createOverride(channel, role, user)
        } else {
            false
        }

        /*
        val member = getMember(user, guild) ?: return false

        if (member.owner) {
            return voiceChannelRolePermissionsService.createOverride(channel, role, user)
        }
        else {
            val myPerms = getMajorRole(user, guild) ?: return false
            return if (myPerms.permissions.admin || myPerms.permissions.manageChannels) {
                voiceChannelRolePermissionsService.createOverride(channel, role, user)
            } else {
                val highestPerm = VoicePermissionsData()
                val overrides = voiceChannelRolePermissionsService.getOverrides(channel, myPerms.roles.map { it.role.id })
                overrides.map {
                    highestPerm.applyOr(it.permissions)
                }
                if (highestPerm.managePermissions == true) {
                    voiceChannelRolePermissionsService.createOverride(channel, role, user)
                } else {
                    false
                }
            }
        }

         */

        TODO()
    }

    suspend fun addTextChannelMemberOverride(user: Int, channel: Int, guild: Int, member: Int): Boolean {
        val high = permissionManager.getMajorRole(member, guild) ?: return false
        val perms = permissionManager.getPermissions(
            user,
            guild,
            role = high.highestRole.role.id,
            channel = Channel(channel, true)
        ) ?: return false
        return if (perms.manageRoles || perms.admin) {
            textChannelMemberPermissionsService.createOverride(channel, high.member.id, user)
        } else {
            false
        }

        /*
        val _member = getMember(user, guild) ?: return false

        if (_member.owner) {
            return textChannelMemberPermissionsService.createOverride(channel, member, user)
        }
        else {
            val myPerms = getMajorRole(user, guild) ?: return false
            return if (myPerms.permissions.admin || myPerms.permissions.manageChannels) {
                textChannelMemberPermissionsService.createOverride(channel, member, user)
            } else {
                val highestPerm = TextPermissionsData()
                val overrides = textChannelMemberPermissionsService.getOverrides(channel, myPerms.roles.map { it.role.id })
                overrides.map {
                    highestPerm.applyOr(it.permissions)
                }
                if (highestPerm.managePermissions == true) {
                    textChannelMemberPermissionsService.createOverride(channel, member, user)
                } else {
                    false
                }
            }
        }

         */

        // mange channels permission
        TODO()
    }

    suspend fun addVoiceChannelMemberOverride(user: Int, channel: Int, guild: Int, member: Int): Boolean {
        val high = permissionManager.getMajorRole(member, guild) ?: return false

        val perms = permissionManager.getPermissions(
            user,
            guild,
            role = high.highestRole.role.id,
            channel = Channel(channel, false)
        ) ?: return false
        return if (perms.admin || perms.manageRoles) {
            voiceChannelMemberPermissionsService.createOverride(channel, high.member.id, user)
        } else {
            false
        }

        /*
        val _member = permissionManager.getMember(user, guild) ?: return false

        if (_member.owner) {
            return voiceChannelMemberPermissionsService.createOverride(channel, member, user)
        }
        else {
            val myPerms = getMajorRole(user, guild) ?: return false
            return if (myPerms.permissions.admin || myPerms.permissions.manageChannels) {
                voiceChannelMemberPermissionsService.createOverride(channel, member, user)
            } else {
                val highestPerm = VoicePermissionsData()
                val overrides = voiceChannelMemberPermissionsService.getOverrides(channel, myPerms.roles.map { it.role.id })
                overrides.map {
                    highestPerm.applyOr(it.permissions)
                }
                if (highestPerm.managePermissions == true) {
                    voiceChannelMemberPermissionsService.createOverride(channel, member, user)
                } else {
                    false
                }
            }
        }

         */

        // mange channels permission
        TODO()
    }

    suspend fun removeTextChannelRoleOverride(user: Int, channel: Int, guild: Int, role: Int): Boolean {

        val perms =
            permissionManager.getPermissions(user, guild, role = role, channel = Channel(channel, true)) ?: return false
        return if (perms.admin || perms.manageRoles) {
            textChannelRolePermissionsService.removeOverride(channel, role)
        } else {
            false
        }

        /*
        val member = getMember(user, guild) ?: return false

        if (member.owner) {
            return textChannelRolePermissionsService.removeOverride(channel, role)
        }
        else {
            val myPerms = getMajorRole(user, guild) ?: return false
            return if (myPerms.permissions.admin || myPerms.permissions.manageChannels) {
                textChannelRolePermissionsService.removeOverride(channel, role)
            } else {
                val highestPerm = TextPermissionsData()
                val overrides = textChannelRolePermissionsService.getOverrides(channel, myPerms.roles.map { it.role.id })
                overrides.map {
                    highestPerm.applyOr(it.permissions)
                }
                if (highestPerm.managePermissions == true) {
                    textChannelRolePermissionsService.removeOverride(channel, role)
                } else {
                    false
                }
            }
        }

         */

        // mange channels permission
        TODO()
    }

    suspend fun removeVoiceChannelRoleOverride(user: Int, channel: Int, guild: Int, role: Int): Boolean {
        val perms = permissionManager.getPermissions(user, guild, role = role, channel = Channel(channel, false))
            ?: return false
        return if (perms.admin || perms.manageRoles) {
            voiceChannelRolePermissionsService.removeOverride(channel, role)
        } else {
            false
        }

        /*
        val member = getMember(user, guild) ?: return false

        if (member.owner) {
            return voiceChannelRolePermissionsService.removeOverride(channel, role)
        }
        else {
            val myPerms = getMajorRole(user, guild) ?: return false
            return if (myPerms.permissions.admin || myPerms.permissions.manageChannels) {
                voiceChannelRolePermissionsService.removeOverride(channel, role)
            } else {
                val highestPerm = VoicePermissionsData()
                val overrides = voiceChannelRolePermissionsService.getOverrides(channel, myPerms.roles.map { it.role.id })
                overrides.map {
                    highestPerm.applyOr(it.permissions)
                }
                if (highestPerm.managePermissions == true) {
                    voiceChannelRolePermissionsService.removeOverride(channel, role)
                } else {
                    false
                }
            }
        }

         */

        // mange channels permission
        TODO()
    }

    suspend fun removeTextChannelMemberOverride(user: Int, channel: Int, guild: Int, member: Int): Boolean {
        val high = permissionManager.getMajorRole(member, guild) ?: return false

        val perms = permissionManager.getPermissions(
            user,
            guild,
            role = high.highestRole.role.id,
            channel = Channel(channel, true)
        ) ?: return false
        return if (perms.admin || perms.manageRoles) {
            textChannelMemberPermissionsService.removeOverride(channel, high.member.id)
        } else {
            false
        }


        /*
        val _member = getMember(user, guild) ?: return false

        if (_member.owner) {
            return textChannelMemberPermissionsService.removeOverride(channel, member)
        }
        else {
            val myPerms = getMajorRole(user, guild) ?: return false
            return if (myPerms.permissions.admin || myPerms.permissions.manageChannels) {
                textChannelMemberPermissionsService.removeOverride(channel, member)
            } else {
                val highestPerm = TextPermissionsData()
                val overrides = textChannelMemberPermissionsService.getOverrides(channel, myPerms.roles.map { it.role.id })
                overrides.map {
                    highestPerm.applyOr(it.permissions)
                }
                if (highestPerm.managePermissions == true) {
                    textChannelMemberPermissionsService.removeOverride(channel, member)
                } else {
                    false
                }
            }
        }

         */

        // mange channels permission
        TODO()
    }

    suspend fun removeVoiceChannelMemberOverride(user: Int, channel: Int, guild: Int, member: Int): Boolean {
        val high = permissionManager.getMajorRole(member, guild) ?: return false

        val perms = permissionManager.getPermissions(
            user,
            guild,
            role = high.highestRole.role.id,
            channel = Channel(channel, false)
        ) ?: return false
        return if (perms.admin || perms.manageRoles) {
            voiceChannelMemberPermissionsService.removeOverride(channel, member)
        } else {
            false
        }
        /*
        val _member = getMember(user, guild) ?: return false

        if (_member.owner) {
            return voiceChannelMemberPermissionsService.removeOverride(channel, member)
        }
        else {
            val myPerms = getMajorRole(user, guild) ?: return false
            return if (myPerms.permissions.admin || myPerms.permissions.manageChannels) {
                voiceChannelMemberPermissionsService.removeOverride(channel, member)
            } else {
                val highestPerm = VoicePermissionsData()
                val overrides = voiceChannelMemberPermissionsService.getOverrides(channel, myPerms.roles.map { it.role.id })
                overrides.map {
                    highestPerm.applyOr(it.permissions)
                }
                if (highestPerm.managePermissions == true) {
                    voiceChannelMemberPermissionsService.removeOverride(channel, member)
                } else {
                    false
                }
            }
        }

         */

        // mange channels permission
        TODO()
    }

    suspend fun editTextChannelRoleOverride(
        user: Int,
        channel: Int,
        guild: Int,
        role: Int,
        override: TextChannelPermissionsOverride
    ): Boolean {
        val perms =
            permissionManager.getPermissions(user, guild, role = role, channel = Channel(channel, true)) ?: return false

        return if (perms.admin || perms.manageRoles) {
            textChannelRolePermissionsService.updateOverride(channel, role, override)
        } else {
            false
        }

        /*
        val member = getMember(user, guild) ?: return false

        if (member.owner) {
            return textChannelRolePermissionsService.updateOverride(channel, role, override)
        }
        else {
            val myPerms = getMajorRole(user, guild) ?: return false
            return if (myPerms.permissions.admin || myPerms.permissions.manageChannels) {
                textChannelRolePermissionsService.updateOverride(channel, role, override)
            }
            else {
                val highestPerm = TextPermissionsData()
                val overrides = textChannelRolePermissionsService.getOverrides(channel, myPerms.roles.map { it.role.id })
                overrides.map {
                    highestPerm.applyOr(it.permissions)
                }
                if (highestPerm.managePermissions == true) {
                    textChannelRolePermissionsService.updateOverride(channel, role, override)
                } else {
                    false
                }
            }
        }

         */

        // mange channels permission
        TODO()
    }

    suspend fun editVoiceChannelRoleOverride(
        user: Int,
        channel: Int,
        guild: Int,
        role: Int,
        override: VoiceChannelPermissionsOverride
    ): Boolean {
        val perms = permissionManager.getPermissions(user, guild, channel = Channel(channel, false), role = role)
            ?: return false

        return if (perms.admin || perms.manageRoles) {
            voiceChannelRolePermissionsService.updateOverride(channel, role, override)
        } else {
            false
        }
        /*
        val member = getMember(user, guild) ?: return false

        if (member.owner) {
            return voiceChannelRolePermissionsService.updateOverride(channel, role, override)
        }
        else {
            val myPerms = getMajorRole(user, guild) ?: return false
            return if (myPerms.permissions.admin || myPerms.permissions.manageChannels) {
                voiceChannelRolePermissionsService.updateOverride(channel, role, override)
            }
            else {
                val highestPerm = VoicePermissionsData()
                val overrides = voiceChannelRolePermissionsService.getOverrides(channel, myPerms.roles.map { it.role.id })
                overrides.map {
                    highestPerm.applyOr(it.permissions)
                }
                if (highestPerm.managePermissions == true) {
                    voiceChannelRolePermissionsService.updateOverride(channel, role, override)
                } else {
                    false
                }
            }
        }

         */


        // mange channels permission
        TODO()
    }

    suspend fun editTextChannelMemberOverride(
        user: Int,
        channel: Int,
        guild: Int,
        member: Int,
        override: TextChannelPermissionsOverride
    ): Boolean {
        val high = permissionManager.getMajorRole(member, guild) ?: return false

        val perms = permissionManager.getPermissions(
            user,
            guild,
            channel = Channel(channel, true),
            role = high.highestRole.role.id
        ) ?: return false

        return if (perms.admin || perms.manageRoles) {
            textChannelMemberPermissionsService.updateOverride(channel, high.member.id, override)
        } else {
            false
        }


        /*
        val _member = getMember(user, guild) ?: return false

        if (_member.owner) {
            return textChannelMemberPermissionsService.updateOverride(channel, member, override)
        }
        else {
            val myPerms = getMajorRole(user, guild) ?: return false
            return if (myPerms.permissions.admin || myPerms.permissions.manageChannels) {
                textChannelMemberPermissionsService.updateOverride(channel, member, override)
            }
            else {
                val highestPerm = TextPermissionsData()
                val overrides = textChannelMemberPermissionsService.getOverrides(channel, myPerms.roles.map { it.role.id })
                overrides.map {
                    highestPerm.applyOr(it.permissions)
                }
                if (highestPerm.managePermissions == true) {
                    textChannelMemberPermissionsService.updateOverride(channel, member, override)
                } else {
                    false
                }
            }
        }

         */

        // mange channels permission
        TODO()
    }

    suspend fun editVoiceChannelMemberOverride(
        user: Int,
        channel: Int,
        guild: Int,
        member: Int,
        override: VoiceChannelPermissionsOverride
    ): Boolean {
        val high = permissionManager.getMajorRole(member, guild) ?: return false

        val perms = permissionManager.getPermissions(
            user,
            guild,
            channel = Channel(channel, false),
            role = high.highestRole.role.id
        ) ?: return false

        return if (perms.admin || perms.manageRoles) {
            voiceChannelMemberPermissionsService.updateOverride(channel, member, override)
        } else {
            false
        }

        /*
        val _member = getMember(user, guild) ?: return false

        if (_member.owner) {
            return voiceChannelMemberPermissionsService.updateOverride(channel, member, override)
        }
        else {
            val myPerms = getMajorRole(user, guild) ?: return false
            return if (myPerms.permissions.admin || myPerms.permissions.manageChannels) {
                voiceChannelMemberPermissionsService.updateOverride(channel, member, override)
            }
            else {
                val highestPerm = VoicePermissionsData()
                val overrides = voiceChannelMemberPermissionsService.getOverrides(channel, myPerms.roles.map { it.role.id })
                overrides.map {
                    highestPerm.applyOr(it.permissions)
                }
                if (highestPerm.managePermissions == true) {
                    voiceChannelMemberPermissionsService.updateOverride(channel, member, override)
                } else {
                    false
                }
            }
        }

         */

        // mange channels permission
        TODO()
    }

    suspend fun getTextChannelPermissions(user: Int, channel: Int, guild: Int): List<TextChannelMemberOverride>? {
        val perms = permissionManager.getPermissions(user, guild, channel = Channel(channel, true)) ?: return null

        return if (perms.admin || perms.manageRoles) {
            textChannelMemberPermissionsService.getOverrides(channel)
        } else {
            null
        }

        TODO()
    }

    suspend fun getVoiceChannelPermissions(user: Int, channel: Int, guild: Int): List<VoiceChannelMemberOverride>? {
        val perms = permissionManager.getPermissions(user, guild, channel = Channel(channel, false)) ?: return null

        return if (perms.admin || perms.manageRoles) {
            voiceChannelMemberPermissionsService.getOverrides(channel)
        } else {
            null
        }

        TODO()
    }

    suspend fun getGuildPermissions(user: Int, guild: Int): List<Role>? {
        val perms = permissionManager.getPermissions(user, guild) ?: return null

        return if (perms.admin || perms.manageRoles) {
            guildRoleService.getRoles(guild)
        } else {
            null
        }

        TODO()
    }
}

class ChannelOverridesService

data class TextPermissionsData(
    override val id: Int = 0,
    override var viewChannel: Boolean? = null,
    override var manageChannel: Boolean? = null,
    override var managePermissions: Boolean? = null,
    override var sendMessages: Boolean? = null,
    override var sendEmbeds: Boolean? = null,
    override var sendAttachments: Boolean? = null,
    override var addReactions: Boolean? = null,
    override var sendExternalEmojis: Boolean? = null,
    override var mentionEveryone: Boolean? = null,
    override var manageMessages: Boolean? = null,
    override var viewHistory: Boolean? = null,
) : TextChannelPermissionsOverride

data class VoicePermissionsData(
    override val id: Int = 0,
    override var viewChannel: Boolean? = null,
    override var manageChannel: Boolean? = null,
    override var managePermissions: Boolean? = null,
    override var connect: Boolean? = null,
    override var speak: Boolean? = null,
    override var streamVideo: Boolean? = null,
    override var activities: Boolean? = null,
    override var prioritySpeaker: Boolean? = null,
    override var deafen: Boolean? = null,
    override var move: Boolean? = null

) : VoiceChannelPermissionsOverride


/*
owner {
    is {
        do_it
    }
    else {
        admin {
            do_it
        }
        channels(id) {
            manageRoles {
                do_it
            }
        }
    }
}


 */