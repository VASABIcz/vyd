package api

import Converter
import Parameters
import Responses.Companion.badRequest
import Responses.Companion.serverIssue
import Responses.Companion.success
import data.requests.MessagePayload
import database.servicies.messages.MessageService
import database.servicies.users.UserService
import database.servicies.users.fetchUser
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.messages(userService: UserService, messageService: MessageService) {
    routing {
        authenticate {
            route("messages") {
                get("messages") {
                    val p = Parameters(call.parameters)
                    val channelId by p.parameter("channel", Converter.Int)
                    if (!p.isValid) {
                        return@get call.badRequest(p.getIssues)
                    }

                    val principal = call.principal<JWTPrincipal>()
                    val me = principal!!.fetchUser(userService)!!

                    val messages = messageService.getMessages(me.id, channelId!!)

                    return@get call.respond(messages.map {
                        it.toMessagesMessage()
                    })
                }
                post("send") {
                    val payload = call.receive<MessagePayload>()
                    val p = Parameters(call.parameters)
                    val channelId by p.parameter("channel", Converter.Int)
                    if (!p.isValid) {
                        return@post call.badRequest(p.getIssues)
                    }

                    val principal = call.principal<JWTPrincipal>()
                    val me = principal!!.fetchUser(userService)!!

                    if (messageService.createMessage(me.id, channelId!!, payload.content) != null) {
                        return@post call.success()
                    } else {
                        return@post call.serverIssue()
                    }
                }
                route("messages/{message}") {
                    delete {
                        val payload = call.receive<MessagePayload>()
                        val p = Parameters(call.parameters)
                        val messageId by p.parameter("message", Converter.Int)
                        if (!p.isValid) {
                            return@delete call.badRequest(p.getIssues)
                        }

                        val principal = call.principal<JWTPrincipal>()
                        val me = principal!!.fetchUser(userService)!!

                        messageService.deleteMessage(messageId!!)
                    }
                    get {

                    }
                }
            }
        }
    }
}