package auth.hash

data class SaltedHash(val hash: String, val salt: String)
