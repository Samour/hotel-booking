package me.aburke.hotelbooking.stubs.repository

import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.repository.SessionRepository

class SessionRepositoryStub : SessionRepository {

    private val sessions = mutableMapOf<String, UserSession>()

    override fun insertUserSession(session: UserSession) {
        sessions[session.sessionId] = session
    }

    fun getSessions(): Map<String, UserSession> = sessions
}
