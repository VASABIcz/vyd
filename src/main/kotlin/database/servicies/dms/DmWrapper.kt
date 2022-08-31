package database.servicies.dms

import database.servicies.avatars.AvatarService
import database.servicies.friendRequests.FriendRequestService
import database.servicies.friendRequests.FriendRequestState
import database.servicies.guilds.GuildMemberService
import database.servicies.messages.Message
import database.servicies.messages.MessageService
import database.servicies.settings.UserSettingsService
import org.ktorm.database.Database

class DmWrapper(
    private val dmMemberService: DmMemberService,
    private val dmInviteService: DmInviteService,
    private val dmService: DmService,
    private val dmAvatarService: AvatarService,
    private val messageService: MessageService,
    private val friendRequestService: FriendRequestService,
    private val database: Database,
    private val userSettingsService: UserSettingsService,
    private val guildMemberService: GuildMemberService,
) {

    private suspend fun areFriends(user: Int, user1: Int): Boolean {
        dmMemberService.getDmChannels(user, user1).forEach {
            if (it.second == DmType.friends.name) {
                return true
            }
        }
        return false
    }

    private suspend fun isMember(user: Int, dm: Int): Boolean {
        return dmMemberService.getMember(dm, user) != null
    }

    private suspend fun isOwner(user: Int, dm: Int): Boolean {
        return dmMemberService.getMember(dm, user)?.dm?.author?.id == user
    }

    suspend fun getInvite(dm: Int, user: Int): String? {
        return dmInviteService.getPendingInvite(user, dm)?.url ?: dmInviteService.createInvite(user, dm)
        TODO()
    }

    suspend fun inviteJoin(user: Int, url: String): Boolean {
        val invite = dmInviteService.getPendingInvite(url) ?: return false
        if (invite.isExpired) return false
        assert(invite.group.type == DmType.group)
        return dmMemberService.addMember(invite.group.channel.id, user, invite.author.user.id)
        // find dm by url
        TODO()
    }

    suspend fun setAvatar(dm: Int, avatar: ByteArray, user: Int): Boolean {
        val member = dmMemberService.getMember(dm, user) ?: return false
        if (member.dm.author?.id != user || member.dm.type != DmType.group) return false
        return dmAvatarService.setAvatar(dm, avatar)

        // check if user is owner and dm is group
        TODO()
    }

    suspend fun deleteAvatar(dm: Int, user: Int): Boolean {
        val member = dmMemberService.getMember(dm, user) ?: return false
        if (member.dm.author?.id != user || member.dm.type != DmType.group) return false
        return dmAvatarService.removeAvatar(dm)

        // check if user is owner and dm is group
        TODO()
    }

    suspend fun changeName(dm: Int, who: Int, name: String): Boolean {
        val member = dmMemberService.getMember(dm, who) ?: return false
        if (member.dm.author?.id != who) return false
        return dmService.changeName(dm, name)

        // check if user is owner and dm is group
        TODO()
    }

    suspend fun createDm(requester: Int, user: Int): Boolean {
        if (userSettingsService.getValue(user, "accepts_dms")?.value?.toBooleanStrictOrNull() == false) return false
        var canCreate = false
        dmMemberService.getDmChannels(user, requester).forEach {
            if (it.second == DmType.friends.name) {
                return false
            } else if (it.second == DmType.group.name) {
                canCreate = true
            } else {
                return false
            }
        }
        if (guildMemberService.getGuilds(requester, user).isNotEmpty()) {
            canCreate = true
        }

        if (!canCreate) return false

        database.useTransaction {
            val id = dmService.createDm() ?: throw Throwable("whops")
            if (!dmMemberService.addMember(id, requester, null)) throw Throwable("whops")
            if (!dmMemberService.addMember(id, user, null)) throw Throwable("whops")
        }
        return true

        // check if user accepts dms
        // check if already friends or dm exists
        // check if requester is in the same guild
        // check if requester is in the same group
        TODO()
    }

    suspend fun createGroup(user: Int): Boolean {
        database.useTransaction {
            val id = dmService.createGroup(user) ?: throw Throwable("failed to create group $user")
            if (!dmMemberService.addMember(id, user, null)) throw Throwable("failed to add dm member $user")
        }
        return true
    }

    suspend fun friendRequest(requester: Int, user: Int): Boolean {
        // check if dm exists or are already friends
        val request = friendRequestService.getRequestState(requester, user)
        if (request != null && request.state == FriendRequestState.pending) {
            return false
        } else {
            // fetch united dms and return if already friend
            if (areFriends(requester, user)) return false
            /*
            val dms1 = dmMemberService.getDmChannels(requester)
            val dms2 = dmMemberService.getDmChannels(user)
            // search for same dms and check if is friend
            for (ch in dms1) {
                dms2.find {
                    it == ch && it.dm.type == DmType.friends
                }?.also { return false }
            }
             */
            // create friend request
            return friendRequestService.createRequest(requester, user) != null
        }
        TODO()
    }

    suspend fun unFriend(dm: Int, user: Int): Boolean {
        val member = dmMemberService.getMember(dm, user) ?: return false
        if (member.dm.type != DmType.friends) return false
        return dmService.switchToDm(dm)
        // check if is friendship
        TODO()
    }

    suspend fun acceptFriendRequest(user: Int, request: Int): Boolean {
        // check if receiver is the user
        // check if already friends
        // check if dm exists
        // create channel
        val request = friendRequestService.getRequestState(request) ?: return false
        database.useTransaction {
            if (!friendRequestService.changePendingRequestState(
                    request.id,
                    FriendRequestState.accepted,
                    user
                )
            ) throw Throwable("whops")
            val dms1 = dmMemberService.getDmChannels(request.requester.id)
            val dms2 = dmMemberService.getDmChannels(user)
            // search for same dms and check if is friend
            for (ch in dms1) {
                val dm = dms2.find {
                    it == ch
                } ?: continue
                when (dm.dm.type) {
                    DmType.group -> {}
                    DmType.friends -> {
                        return true
                    }

                    DmType.dm -> {
                        if (!dmService.switchToFriend(dm.dm.channel.id)) throw Throwable("whops")
                        return true
                    }
                }
            }
            val id = dmService.createFriend() ?: throw Throwable("whops")
            if (!dmMemberService.addMember(id, user, null)) throw Throwable("whops")
            if (!dmMemberService.addMember(id, request.requester.id, null)) throw Throwable("whops")
            return true
        }
        TODO()
    }

    suspend fun rejectFriendRequest(user: Int, request: Int): Boolean {
        return friendRequestService.changePendingRequestState(request, FriendRequestState.declined, user)
        // check if receiver is the user
        TODO()
    }

    suspend fun sendMessage(user: Int, dm: Int, message: String): Boolean {
        dmMemberService.getMember(dm, user) ?: return false
        return messageService.createMessage(user, dm, message) != null
        // check if user is member
        TODO()
    }

    suspend fun getMessages(user: Int, dm: Int, offsetId: Int?, newer: Boolean): List<Message>? {
        if (!isMember(user, dm)) return null

        return if (offsetId != null) {
            messageService.getMessages(dm, 50, offsetId, newer)
        } else {
            messageService.getMessages(dm, 50)
        }

        // check if user member
        // offset is message id or timestamp
        // newer controls if lookup or look back data from specified offset
        TODO()
    }

    suspend fun getMembers(user: Int, dm: Int): List<DmMember>? {
        val members = dmMemberService.getMembers(dm)
        members.find {
            it.user.id == user
        } ?: return null
        return members
        // check if user is member
        TODO()
    }

    suspend fun getMember(user: Int, dm: Int, member: Int): DmMember? {
        if (!isMember(user, dm)) return null
        return dmMemberService.getMember(dm, member)
    }

    suspend fun kickMember(user: Int, dm: Int, member: Int): Boolean {
        if (!isOwner(user, dm) && user != member) return false
        return dmMemberService.removeMember(dm, member)

        // check if user is owner of dm
        TODO()
    }

    suspend fun banMember(user: Int, dm: Int, member: Int): Boolean {
        TODO()
    }

    suspend fun getDms(user: Int): List<DmMember> {
        return dmMemberService.getDmChannels(user)
        TODO()
    }

    suspend fun getAvatar(user: Int, dm: Int): ByteArray? {
        return dmAvatarService.getAvatar(dm)
        // this
        // check if dm is group
        // return avatar or default avatar
        // or
        // return group avatar/default
        // if friends/dm return second user avatar
        TODO()
    }

    suspend fun deleteMessage(user: Int, dm: Int, message: Int): Boolean {
        val msg = messageService.getMessage(message) ?: return false
        if (msg.author.id != user) return false

        return messageService.deleteMessage(message)
    }

    suspend fun editMessage(user: Int, dm: Int, message: Int, content: String): Boolean {
        val msg = messageService.getMessage(message) ?: return false
        if (msg.author.id != user) return false

        return messageService.editMessage(message, content)
    }

    suspend fun getMessage(user: Int, dm: Int, message: Int): Message? {
        if (!isMember(user, dm)) return null

        return messageService.getMessage(message)
    }

    suspend fun getDm(user: Int, dm: Int): DmMember? {
        return dmMemberService.getMember(dm, user)
    }
}