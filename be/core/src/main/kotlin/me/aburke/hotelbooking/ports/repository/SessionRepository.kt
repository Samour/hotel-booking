package me.aburke.hotelbooking.ports.repository

import me.aburke.hotelbooking.model.user.UserSession

interface SessionRepository {

    fun insertUserSession(session: UserSession)

    fun loadUserSession(sessionId: String): UserSession?
}
