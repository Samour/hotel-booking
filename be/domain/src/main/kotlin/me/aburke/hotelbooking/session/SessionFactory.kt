package me.aburke.hotelbooking.session

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration

private const val SESSION_ID_LENGTH = 36

class SessionFactory(
    private val clock: Clock,
    private val sessionDuration: Duration,
) {

    private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    private val secureRandom = SecureRandom()

    fun createForUser(
        userId: String,
        loginId: String?,
        userRoles: Set<UserRole>,
        anonymousUser: Boolean,
    ): UserSession = UserSession(
        sessionId = (1..SESSION_ID_LENGTH).map {
            charPool[secureRandom.nextInt(charPool.size)]
        }.joinToString(""),
        userId = userId,
        loginId = loginId,
        userRoles = userRoles,
        anonymousUser = anonymousUser,
        sessionExpiryTime = clock.instant().plus(sessionDuration),
    )
}
