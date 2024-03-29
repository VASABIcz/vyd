package api

import Converter
import Parameters
import Responses.Companion.badRequest
import Responses.Companion.notFound
import Responses.Companion.serverIssue
import Responses.Companion.success
import data.requests.CreateGuildChannel
import data.requests.CreateGuildInvite
import data.requests.MessagePayload
import database.servicies.guilds.GuildMemberService
import database.servicies.users.userId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wrapers.AvatarWrapper
import wrapers.GuildWrapper

fun Application.guilds(
    memberService: GuildMemberService,
    guildWrapper: GuildWrapper,
    avatarWrapper: AvatarWrapper
) {
    routing {
        authenticate {
            route("invites") {

                get("{invite}/accept") {
                    val p = Parameters(call.parameters)
                    val invite by p.parameter("invite", Converter.String)
                    if (!p.isValid) {
                        if (!p.isValid) {
                            return@get call.badRequest(p.getIssues)
                        }
                    }

                    val me = call.principal<JWTPrincipal>()!!.userId!!

                    val res = guildWrapper.joinInvite(me, invite!!)

                    if (res) {
                        call.success()
                    } else {
                        call.serverIssue()
                    }
                }
            }
        }
        route("guilds") {
            authenticate {
                get {
                    val me = call.principal<JWTPrincipal>()!!.userId!!

                    val guilds = memberService.getGuilds(me).map {
                        it.toMembersMember()
                    }
                    call.respond(guilds)
                }
                route("create") {
                    // FIXME       |
                    //             | ???
                    route("{name}") {
                        post {
                            val p = Parameters(call.parameters)
                            val name by p.parameter("name", Converter.String)
                            if (!p.isValid) {
                                if (!p.isValid) {
                                    return@post call.badRequest(p.getIssues)
                                }
                            }

                            val me = call.principal<JWTPrincipal>()!!.userId!!

                            if (guildWrapper.createGuild(me, name!!) != null) {
                                call.success()
                            } else {
                                call.serverIssue()
                            }
                        }
                    }
                }
                route("guild") {
                    route("{guild_id}") {
                        get {
                            val p = Parameters(call.parameters)
                            val guildId by p.parameter("guild_id", Converter.Int)
                            if (!p.isValid) {
                                if (!p.isValid) {
                                    return@get call.badRequest(p.getIssues)
                                }
                            }

                            val me = call.principal<JWTPrincipal>()!!.userId!!

                            val member =
                                memberService.getMember(me, guildId!!)?.toMembersMember() ?: return@get call.notFound()

                            call.respond(member)
                        }
                        delete {
                            val p = Parameters(call.parameters)
                            val guildId by p.parameter("guild_id", Converter.Int)
                            if (!p.isValid) {
                                if (!p.isValid) {
                                    return@delete call.badRequest(p.getIssues)
                                }
                            }

                            val me = call.principal<JWTPrincipal>()!!.userId!!

                            if (guildWrapper.deleteGuild(me, guildId!!)) {
                                call.success()
                            } else {
                                call.serverIssue()
                            }
                        }
                        route("avatar") {
                            get {
                                val p = Parameters(call.parameters)
                                val id by p.parameter("guild_id", Converter.Int)
                                if (!p.isValid) {
                                    return@get call.badRequest(p.getIssues)
                                }

                                val me = call.principal<JWTPrincipal>()!!.userId!!

                                val avatar = avatarWrapper.getGuildAvatar(id!!)

                                call.respondBytes(avatar, ContentType.Image.PNG, HttpStatusCode.OK)
                            }
                        }
                        route("name") {
                            route("{name}") {
                                patch {
                                    val p = Parameters(call.parameters)
                                    val guildId by p.parameter("guild_id", Converter.Int)
                                    val name by p.parameter("name", Converter.String)
                                    if (!p.isValid) {
                                        if (!p.isValid) {
                                            return@patch call.badRequest(p.getIssues)
                                        }
                                    }

                                    val me = call.principal<JWTPrincipal>()!!.userId!!

                                    if (guildWrapper.renameGuild(me, guildId!!, name!!)) {
                                        call.success()
                                    } else {
                                        call.serverIssue()
                                    }
                                }
                            }
                        }
                        route("invites") {
                            get {
                                val p = Parameters(call.parameters)
                                val guildId by p.parameter("guild_id", Converter.Int)
                                if (!p.isValid) {
                                    if (!p.isValid) {
                                        return@get call.badRequest(p.getIssues)
                                    }
                                }

                                val me = call.principal<JWTPrincipal>()!!.userId!!
                                val invites = guildWrapper.getInvites(me, guildId!!)

                                if (invites == null) {
                                    return@get call.respond(HttpStatusCode.Unauthorized)
                                } else {
                                    return@get call.respond(invites)
                                }
                            }
                            post {
                                val p = Parameters(call.parameters)
                                val guildId by p.parameter("guild_id", Converter.Int)
                                if (!p.isValid) {
                                    if (!p.isValid) {
                                        return@post call.badRequest(p.getIssues)
                                    }
                                }

                                val me = call.principal<JWTPrincipal>()!!.userId!!
                                val payload = call.receive<CreateGuildInvite>()

                                val invite = guildWrapper.createInvite(
                                    me,
                                    guildId!!,
                                    payload.url,
                                    payload.expireTimestamp,
                                    payload.maxUses
                                )

                                if (invite == null) {
                                    call.serverIssue()
                                } else {
                                    // TODO add to config or just return with unique part
                                    call.respond("invite" to "http://192.168.0.107:8080/invites/$invite")
                                }
                            }
                            route("{invite}") {
                                delete {
                                    val p = Parameters(call.parameters)
                                    val guildId by p.parameter("guild_id", Converter.Int)
                                    val invite by p.parameter("invite", Converter.String)
                                    if (!p.isValid) {
                                        if (!p.isValid) {
                                            return@delete call.badRequest(p.getIssues)
                                        }
                                    }

                                    val me = call.principal<JWTPrincipal>()!!.userId!!

                                    val res = guildWrapper.deleteInvite(me, guildId!!, invite!!)

                                    if (!res) {
                                        call.serverIssue()
                                    } else {
                                        call.success()
                                    }
                                }
                            }
                        }
                        route("channels") {
                            get {
                                val p = Parameters(call.parameters)
                                val guildId by p.parameter("guild_id", Converter.Int)
                                if (!p.isValid) {
                                    if (!p.isValid) {
                                        return@get call.badRequest(p.getIssues)
                                    }
                                }

                                val me = call.principal<JWTPrincipal>()!!.userId!!

                                val channels =
                                    guildWrapper.getChannelsOrdered(me, guildId!!) ?: return@get call.serverIssue()

                                call.respond(channels.toGuildsChannels())
                            }
                            // Create uncategorized channels
                            route("create") {
                                post {
                                    val p = Parameters(call.parameters)
                                    val guildId by p.parameter("guild_id", Converter.Int)
                                    if (!p.isValid) {
                                        if (!p.isValid) {
                                            return@post call.badRequest(p.getIssues)
                                        }
                                    }
                                    val me = call.principal<JWTPrincipal>()!!.userId!!

                                    val payload = call.receive<CreateGuildChannel>()

                                    if (guildWrapper.createChannel(
                                            guildId!!,
                                            me,
                                            payload.name,
                                            payload.type,
                                            payload.category
                                        )
                                    ) {
                                        call.success()
                                    } else {
                                        call.serverIssue()
                                    }
                                }
                            }
                            route("channel") {
                                route("{channel_id}") {
                                    get {
                                        val p = Parameters(call.parameters)
                                        val guildId by p.parameter("guild_id", Converter.Int)
                                        val channelId by p.parameter("channel_id", Converter.Int)
                                        if (!p.isValid) {
                                            if (!p.isValid) {
                                                return@get call.badRequest(p.getIssues)
                                            }
                                        }

                                        val me = call.principal<JWTPrincipal>()!!.userId!!

                                        val channel = guildWrapper.getChannel(me, guildId!!, channelId!!)
                                            ?: return@get call.serverIssue()

                                        return@get call.respond(channel)
                                    }
                                    delete {
                                        val p = Parameters(call.parameters)
                                        val guildId by p.parameter("guild_id", Converter.Int)
                                        val channelId by p.parameter("channel_id", Converter.Int)
                                        if (!p.isValid) {
                                            if (!p.isValid) {
                                                return@delete call.badRequest(p.getIssues)
                                            }
                                        }

                                        val me = call.principal<JWTPrincipal>()!!.userId!!

                                        if (guildWrapper.deleteChannel(me, guildId!!, channelId!!)) {
                                            call.success()
                                        } else {
                                            call.serverIssue()
                                        }
                                    }
                                    route("edit") {
                                        patch {
                                            val p = Parameters(call.parameters)
                                            val guildId by p.parameter("guild_id", Converter.Int)
                                            val channelId by p.parameter("channel_id", Converter.Int)
                                            if (!p.isValid) {
                                                if (!p.isValid) {
                                                    return@patch call.badRequest(p.getIssues)
                                                }
                                            }
                                            val me = call.principal<JWTPrincipal>()!!.userId!!

                                            val q = Parameters(call.request.queryParameters)
                                            val name by q.parameter("name", Converter.String)
                                            if (!q.isValid) {
                                                return@patch call.badRequest(q.getIssues)
                                            }

                                            if (guildWrapper.renameChannel(me, guildId!!, channelId!!, name!!)) {
                                                call.success()
                                            } else {
                                                call.serverIssue()
                                            }
                                        }
                                    }

                                    route("move") {
                                        patch {
                                            val p = Parameters(call.parameters)
                                            val guildId by p.parameter("guild_id", Converter.Int)
                                            val channelId by p.parameter("channel_id", Converter.Int)
                                            if (!p.isValid) {
                                                if (!p.isValid) {
                                                    return@patch call.badRequest(p.getIssues)
                                                }
                                            }
                                            val me = call.principal<JWTPrincipal>()!!.userId!!

                                            val q = Parameters(call.request.queryParameters)
                                            val position by q.parameter("position", Converter.Int)
                                            val category by q.parameter("category", Converter.Int, optional = true)
                                            if (!q.isValid) {
                                                return@patch call.badRequest(q.getIssues)
                                            }

                                            if (guildWrapper.moveChannel(
                                                    channelId!!,
                                                    me,
                                                    guildId!!,
                                                    position!!,
                                                    category
                                                )
                                            ) {
                                                call.success()
                                            } else {
                                                call.serverIssue()
                                            }
                                        }
                                    }
                                    /*
                                    // Create channels in category
                                    route("create") {
                                        post {
                                            val p = Parameters(call.parameters)
                                            val guildId by p.parameter("guild_id", Converter.Int)
                                            val channelId by p.parameter("channel_id", Converter.Int)
                                            if (!p.isValid) {
                                                if (!p.isValid) {
                                                    return@post call.badRequest(p.getIssues)
                                                }
                                            }
                                            val me = call.principal<JWTPrincipal>()!!.userId!!

                                            val q = Parameters(call.request.queryParameters)
                                            val name by q.parameter("name", Converter.String)
                                            val type by q.parameter("type", Converter.Custom {
                                                _, type -> when (type) {
                                                    "voice" -> {
                                                        ChannelType.voice
                                                    }
                                                "text" -> {
                                                    ChannelType.text
                                                }
                                                else -> {
                                                    throw Exception()
                                                }
                                            }
                                            })
                                            if (!q.isValid) {
                                                return@post call.badRequest(q.getIssues)
                                            }

                                            if (guildWrapper.createChannel(guildId!!, me, name!!, type!!, channelId!!)) {
                                                call.success()
                                            }
                                            else {
                                                call.serverIssue()
                                            }
                                        }
                                    }

                                     */

                                    route("messages") {
                                        get {
                                            val p = Parameters(call.parameters)
                                            val guildId by p.parameter("guild_id", Converter.Int)
                                            val channelId by p.parameter("channel_id", Converter.Int)
                                            if (!p.isValid) {
                                                if (!p.isValid) {
                                                    return@get call.badRequest(p.getIssues)
                                                }
                                            }

                                            val q = Parameters(call.request.queryParameters)
                                            val amount by q.parameter("amount", Converter.Int, optional = true)
                                            val offset by q.parameter("offset", Converter.Int, optional = true)
                                            val id by q.parameter("id", Converter.Int, optional = true)
                                            // TODO
                                            val asc by q.parameter("asc", Converter.Boolean, optional = true)
                                            val start by q.parameter("start", Converter.Int, optional = true)
                                            val end by q.parameter("end", Converter.Int, optional = true)
                                            val author by q.parameter("author", Converter.Int, optional = true)
                                            if (!q.isValid) {
                                                return@get call.badRequest(q.getIssues)
                                            }

                                            val me = call.principal<JWTPrincipal>()!!.userId!!

                                            val messages =
                                                guildWrapper.getMessages(me, guildId!!, channelId!!, amount, offset, id)
                                                    ?: return@get call.serverIssue()

                                            call.respond(messages)
                                        }
                                        route("send") {
                                            post {
                                                val p = Parameters(call.parameters)
                                                val guildId by p.parameter("guild_id", Converter.Int)
                                                val channelId by p.parameter("channel_id", Converter.Int)
                                                if (!p.isValid) {
                                                    if (!p.isValid) {
                                                        return@post call.badRequest(p.getIssues)
                                                    }
                                                }

                                                val message = call.receive<MessagePayload>()

                                                val me = call.principal<JWTPrincipal>()!!.userId!!
                                                if (guildWrapper.sendMessage(
                                                        me,
                                                        guildId!!,
                                                        channelId!!,
                                                        message.content
                                                    )
                                                ) {
                                                    call.success()
                                                } else {
                                                    call.serverIssue()
                                                }
                                            }
                                        }
                                        route("message") {
                                            route("{message_id}") {
                                                get {
                                                    val p = Parameters(call.parameters)
                                                    val guildId by p.parameter("guild_id", Converter.Int)
                                                    val channelId by p.parameter("channel_id", Converter.Int)
                                                    val messageId by p.parameter("message_id", Converter.Int)
                                                    if (!p.isValid) {
                                                        if (!p.isValid) {
                                                            return@get call.badRequest(p.getIssues)
                                                        }
                                                    }

                                                    val me = call.principal<JWTPrincipal>()!!.userId!!

                                                    val message = guildWrapper.getMessage(
                                                        me,
                                                        guildId!!,
                                                        channelId!!,
                                                        messageId!!
                                                    ) ?: return@get call.serverIssue()

                                                    call.respond(message)
                                                }
                                                patch {
                                                    TODO()
                                                }
                                                delete {
                                                    val p = Parameters(call.parameters)
                                                    val guildId by p.parameter("guild_id", Converter.Int)
                                                    val channelId by p.parameter("channel_id", Converter.Int)
                                                    val messageId by p.parameter("message_id", Converter.Int)
                                                    if (!p.isValid) {
                                                        if (!p.isValid) {
                                                            return@delete call.badRequest(p.getIssues)
                                                        }
                                                    }

                                                    val me = call.principal<JWTPrincipal>()!!.userId!!

                                                    if (guildWrapper.deleteMessage(
                                                            me,
                                                            guildId!!,
                                                            channelId!!,
                                                            messageId!!
                                                        )
                                                    ) {
                                                        call.success()
                                                    } else {
                                                        call.serverIssue()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        route("members") {
                            get {
                                val p = Parameters(call.parameters)
                                val guildId by p.parameter("guild_id", Converter.Int)
                                if (!p.isValid) {
                                    if (!p.isValid) {
                                        return@get call.badRequest(p.getIssues)
                                    }
                                }

                                val me = call.principal<JWTPrincipal>()!!.userId!!

                                val members =
                                    guildWrapper.getMembers(me, guildId!!) ?: return@get call.serverIssue()

                                call.respond(members)
                            }
                            route("member") {
                                route("{member_id}") {
                                    get {
                                        val p = Parameters(call.parameters)
                                        val guildId by p.parameter("guild_id", Converter.Int)
                                        val memberId by p.parameter("member_id", Converter.Int)
                                        if (!p.isValid) {
                                            if (!p.isValid) {
                                                return@get call.badRequest(p.getIssues)
                                            }
                                        }

                                        val me = call.principal<JWTPrincipal>()!!.userId!!

                                        val member = guildWrapper.getMember(me, guildId!!, memberId!!)
                                            ?: return@get call.serverIssue()

                                        call.respond(member)
                                    }
                                    delete {
                                        val p = Parameters(call.parameters)
                                        val guildId by p.parameter("guild_id", Converter.Int)
                                        val memberId by p.parameter("member_id", Converter.Int)
                                        if (!p.isValid) {
                                            if (!p.isValid) {
                                                return@delete call.badRequest(p.getIssues)
                                            }
                                        }

                                        val me = call.principal<JWTPrincipal>()!!.userId!!

                                        if (guildWrapper.leaveMember(me, guildId!!, memberId!!)) {
                                            call.success()
                                        } else {
                                            call.serverIssue()
                                        }
                                    }
                                    route("rename") {
                                        patch {
                                            TODO()
                                            val p = Parameters(call.parameters)
                                            val guildId by p.parameter("guild_id", Converter.Int)
                                            val memberId by p.parameter("member_id", Converter.Int)
                                            if (!p.isValid) {
                                                if (!p.isValid) {
                                                    return@patch call.badRequest(p.getIssues)
                                                }
                                            }

                                            val me = call.principal<JWTPrincipal>()!!.userId!!

                                            val member = guildWrapper.getMember(me, guildId!!, memberId!!)
                                                ?: return@patch call.serverIssue()

                                            call.respond(member)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}