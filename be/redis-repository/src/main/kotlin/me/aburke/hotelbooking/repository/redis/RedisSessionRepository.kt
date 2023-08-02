package me.aburke.hotelbooking.repository.redis

import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.repository.SessionRepository
import redis.clients.jedis.JedisPooled
import java.time.Duration
import java.time.Instant

class RedisSessionRepository(
    private val jedisPooled: JedisPooled,
) : SessionRepository {

    override fun insertUserSession(session: UserSession) {
        jedisPooled.hset(
            "session:${session.sessionId}",
            mapOf(
                "session-id" to session.sessionId,
                "user-id" to session.userId,
                "user-roles" to session.userRoles.joinToString("|"),
                "anonymous-user" to "${session.anonymousUser}",
                "session-expiry-time" to "${session.sessionExpiryTime}",
            )
        )
        jedisPooled.expire(
            "session:${session.sessionId}",
            Duration.between(Instant.now(), session.sessionExpiryTime).toSeconds(),
        )
    }
}
