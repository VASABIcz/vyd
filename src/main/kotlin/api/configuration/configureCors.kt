package api.configuration

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCors() {
    install(CORS) {
        // jesus i hate cors
        allowNonSimpleContentTypes = true
        allowCredentials = true
        anyHost()
        allowHeadersPrefixed("")
        allowMethod(HttpMethod.Post)
    }
}