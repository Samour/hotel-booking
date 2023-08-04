package me.aburke.hotelbooking.repository.redis

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import java.time.Instant

fun UserSession.toRedisMap() = mapOf(
    "session-id" to sessionId,
    "user-id" to userId,
    "login-id" to loginId,
    "user-roles" to userRoles.joinToString("|"),
    "anonymous-user" to "$anonymousUser",
    "session-expiry-time" to "$sessionExpiryTime",
).purgeNullValues()

fun Map<String, String>.toUserSession() = UserSession(
    sessionId = this["session-id"]!!,
    userId = this["user-id"]!!,
    loginId = this["login-id"],
    userRoles = this["user-roles"]!!.split("|")
        .map(UserRole::valueOf)
        .toSet(),
    anonymousUser = this["anonymous-user"]!!.toBoolean(),
    sessionExpiryTime = Instant.parse(this["session-expiry-time"]),
)
