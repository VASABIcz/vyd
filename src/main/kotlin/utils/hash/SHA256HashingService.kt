package utils.hash

import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom

class SHA256HashingService: HashingService {
    override fun generateSaltedHash(value: String, saltLenght: Int): SaltedHash {
        val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLenght)
        val saltAsHex = Hex.encodeHexString(salt)
        val hash = DigestUtils.sha256Hex("$saltAsHex$value")

        return SaltedHash(hash, saltAsHex)
    }

    override fun verify(value: String, saltedHash: SaltedHash): Boolean {
        return DigestUtils.sha256Hex("${saltedHash.salt}${value}") == saltedHash.hash
    }
}