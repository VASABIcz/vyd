package api

import auth.token.TokenConfig
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity(config: TokenConfig) {
    authentication {
        jwt {
            realm = System.getenv("realm") // FIXME not sure about this

            verifier(
                JWT
                    .require(Algorithm.HMAC256(config.secret))
                    .withAudience(config.audience)
                    .withIssuer(config.issuer)
                    .build()
            )
            validate { credentials ->
                if (credentials.payload.audience.contains(config.audience)) {
                    JWTPrincipal(credentials.payload)
                } else {
                    null
                }
            }
        }
    }
}