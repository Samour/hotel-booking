package me.aburke.hotelbooking.model.user

import java.time.Instant

data class UserSession(
    val sessionId: String,
    val userId: String,
    val loginId: String?,
    val userRoles: Set<UserRole>,
    val anonymousUser: Boolean,
    val sessionExpiryTime: Instant,
)
