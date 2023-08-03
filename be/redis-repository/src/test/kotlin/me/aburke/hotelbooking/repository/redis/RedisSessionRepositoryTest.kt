package me.aburke.hotelbooking.repository.redis

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.repository.SessionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import redis.clients.jedis.JedisPooled
import java.time.Instant

private const val SESSION_ID = "session-id"
private const val USER_ID = "user-id"

private val userRoles = setOf(UserRole.MANAGE_USERS, UserRole.MANAGE_ROOMS)
private val sessionExpiryTime = Instant.now().plusSeconds(3000)
val session = UserSession(
    sessionId = SESSION_ID,
    userId = USER_ID,
    userRoles = userRoles,
    anonymousUser = true,
    sessionExpiryTime = sessionExpiryTime,
)

class RedisSessionRepositoryTest {

    private lateinit var app: KoinApplication
    private lateinit var jedisPooled: JedisPooled

    private lateinit var underTest: SessionRepository

    @BeforeEach
    fun init() {
        app = createApp()
        jedisPooled = app.koin.get()
        underTest = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should store session in DB`() {
        underTest.insertUserSession(session)

        val allSessions = loadAllSessions()
        assertThat(allSessions).containsExactly(session)
    }

    @Test
    fun `session should expire after expiry time`() {
        val session = UserSession(
            sessionId = SESSION_ID,
            userId = USER_ID,
            userRoles = userRoles,
            anonymousUser = true,
            sessionExpiryTime = Instant.now().plusSeconds(3),
        )

        underTest.insertUserSession(session)

        Thread.sleep(1000)

        assertThat(loadAllSessions()).containsExactly(session)

        Thread.sleep(3000)

        assertThat(loadAllSessions()).isEmpty()
    }

    @Test
    fun `should return session by ID`() {
        underTest.insertUserSession(session)

        val result = underTest.loadUserSession(SESSION_ID)

        assertThat(result).isEqualTo(session)
    }

    @Test
    fun `should return null when no session with ID exists`() {
        underTest.insertUserSession(session)

        val result = underTest.loadUserSession("wrong-session-id")

        assertThat(result).isNull()
    }

    private fun loadAllSessions(): List<UserSession> =
        jedisPooled.keys("session:*")
            .map { jedisPooled.hgetAll(it) }
            .map { it.toUserSession() }
}
