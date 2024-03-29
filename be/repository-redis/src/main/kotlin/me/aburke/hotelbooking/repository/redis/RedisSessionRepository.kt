package me.aburke.hotelbooking.repository.redis

import me.aburke.hotelbooking.ports.repository.SessionRepository
import me.aburke.hotelbooking.ports.repository.UserSession
import redis.clients.jedis.JedisPooled
import java.time.Clock
import java.time.Duration

class RedisSessionRepository(
    private val jedisPooled: JedisPooled,
    private val clock: Clock,
) : SessionRepository {

    override fun insertUserSession(session: UserSession) {
        jedisPooled.hset(
            Namespace.session.key(session.sessionId),
            session.toRedisMap(),
        )
        jedisPooled.expire(
            Namespace.session.key(session.sessionId),
            Duration.between(clock.instant(), session.sessionExpiryTime).toSeconds(),
        )
    }

    override fun loadUserSession(sessionId: String): UserSession? =
        jedisPooled.hgetAll(Namespace.session.key(sessionId))
            .takeUnless { it.isEmpty() }
            ?.toUserSession()
}
