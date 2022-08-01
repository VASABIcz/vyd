package utils.random

class BasicRandomStringService : RandomStringService {
    private val chars = (('a'..'z') + ('A'..'Z') + ('0'..'9'))

    override fun generateString(length: Int): String {
        return List(length) {
            chars.random()
        }.joinToString("")
    }
}