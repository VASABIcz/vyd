package websockets.gateway

import database.servicies.friends.FriendService
import database.servicies.guilds.GuildMemberService
import database.servicies.users.userId
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import websockets.worker.RedisConnection

fun Application.gateway(
    guildMemberService: GuildMemberService,
    friendService: FriendService
) {
    routing {
        authenticate {
            webSocket("gateway") {
                val me = call.principal<JWTPrincipal>()!!.userId!!
                val con = RedisConnection(
                    Endpoint.from(System.getenv("redis_host")),
                    me,
                    this,
                    guildMemberService,
                    friendService
                )
                con.connect().await()
            }
        }
    }
}
/*
fun Application.gate(
    guildMemberService: GuildMemberService,
    friendService: FriendService,
    worker: Worker
) {
    routing {
        authenticate {
            webSocket("gateway") {
                this
                val me = call.principal<JWTPrincipal>()!!.userId!!
                worker.connect(me)
                this.coroutineContext.job.join()
            }
        }
    }
}

 */