package me.aburke.hotelbooking.model.user

import me.aburke.hotelbooking.ports.repository.UserSession as DbUserSession

fun UserSession.toDbModel() = DbUserSession(
    sessionId = sessionId,
    userId = userId,
    loginId = loginId,
    userRoles = userRoles.toNameSet(),
    anonymousUser = anonymousUser,
    sessionExpiryTime = sessionExpiryTime,
)

fun DbUserSession.toUserSession() = UserSession(
    sessionId = sessionId,
    userId = userId,
    loginId = loginId,
    userRoles = userRoles.toUserRoles(),
    anonymousUser = anonymousUser,
    sessionExpiryTime = sessionExpiryTime,
)
