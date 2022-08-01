package websockets.gateway

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.websocket.*

fun Application.configureWebsockets() {
    install(WebSockets)
}