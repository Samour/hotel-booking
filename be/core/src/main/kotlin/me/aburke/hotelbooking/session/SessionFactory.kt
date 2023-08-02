package me.aburke.hotelbooking.session

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant

private const val SESSION_ID_LENGTH = 36

class SessionFactory(
    private val sessionDuration: Duration,
) {

    private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    private val secureRandom = SecureRandom()

    fun createForUser(
        userId: String,
        userRoles: Set<UserRole>,
        anonymousUser: Boolean,
    ): UserSession = UserSession(
        sessionId = (1..SESSION_ID_LENGTH).map {
            charPool[secureRandom.nextInt(charPool.size)]
        }.joinToString(""),
        userId = userId,
        userRoles = userRoles,
        anonymousUser = anonymousUser,
        sessionExpiryTime = Instant.now().plus(sessionDuration),
    )
}
