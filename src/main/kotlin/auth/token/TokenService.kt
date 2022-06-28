package auth.token

interface TokenService {
    fun generate(
        config: TokenConfig,
        vararg claims: TokenClaim
    ): String
}