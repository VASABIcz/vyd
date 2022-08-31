package database.servicies.dms

import websockets.DispatcherService

class EventDmMemberService(private val dmMemberService: DmMemberService, val dispatcher: DispatcherService) :
    DmMemberService by dmMemberService {
    // TODO
    override suspend fun addMember(dm: Int, user: Int, inviter: Int?): Boolean {
        val res = dmMemberService.addMember(dm, user, inviter).also {

        }
        return res
    }

    override suspend fun removeMember(dm: Int, user: Int): Boolean {
        val res = dmMemberService.removeMember(dm, user).also {

        }
        return res
    }
}