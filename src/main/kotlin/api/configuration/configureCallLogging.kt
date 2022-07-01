package api.configuration

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*

fun Application.configureCallLogging() {
    install(CallLogging) {
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val userAgent = call.request.headers["User-Agent"]
            val user = call.authentication.principal<JWTPrincipal>()?.getClaim("id", String::class)
            "Status: $status, HTTP method: $httpMethod, User agent: $userAgent, user : $user"
        }
    }
}