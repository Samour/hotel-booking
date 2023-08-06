package me.aburke.hotelbooking.facade.rest.responses

import java.time.Instant

data class SessionResponse(
    val userId: String,
    val loginId: String?,
    val userRoles: List<String>,
    val anonymousUser: Boolean,
    val sessionExpiryTime: Instant,
)
