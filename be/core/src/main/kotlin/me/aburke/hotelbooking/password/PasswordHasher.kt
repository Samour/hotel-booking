package me.aburke.hotelbooking.password

import at.favre.lib.crypto.bcrypt.BCrypt

class PasswordHasher {

    private val hashCost = 6

    private fun hasher() = BCrypt.withDefaults()

    private fun verifyer() = BCrypt.verifyer()

    fun hashPassword(rawPassword: String): String =
        hasher().hashToString(hashCost, rawPassword.toCharArray())

    fun passwordMatches(rawPassword: String, passwordHash: String): Boolean =
        verifyer().verify(rawPassword.toCharArray(), passwordHash.toCharArray()).verified
}
