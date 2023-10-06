package me.aburke.hotelbooking.ports.repository

import java.time.Instant

data class UserSession(
    val sessionId: String,
    val userId: String,
    val loginId: String?,
    val userRoles: Set<String>,
    val anonymousUser: Boolean,
    val sessionExpiryTime: Instant,
)

interface SessionRepository {

    fun insertUserSession(session: UserSession)

    fun loadUserSession(sessionId: String): UserSession?
}
