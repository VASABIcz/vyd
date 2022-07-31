package api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import utils.token.TokenConfig

fun Application.configureSecurity(config: TokenConfig) {
    authentication {
        jwt {
            realm = config.realm

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