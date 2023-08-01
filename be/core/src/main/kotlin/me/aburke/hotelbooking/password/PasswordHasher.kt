package me.aburke.hotelbooking.password

class PasswordHasher {

    private val hashCost = 6

//    private fun hasher() = BCrypt.withDefaults()
//
//    private fun verifyer() = BCrypt.verifyer()

    fun hashPassword(rawPassword: String): String = TODO()
//        hasher().hashToString(hashCost, rawPassword.toCharArray())

    fun passwordMatches(rawPassword: String, passwordHash: String): Boolean = TODO()
//        verifyer().verify(rawPassword.toCharArray(), passwordHash.toCharArray()).verified
}
