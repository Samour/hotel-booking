package me.aburke.hotelbooking.repository.redis

import me.aburke.hotelbooking.ports.repository.SessionRepository
import me.aburke.hotelbooking.ports.repository.UserSession
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import redis.clients.jedis.JedisPooled
import java.time.Instant

private const val SESSION_ID = "session-id"
private const val USER_ID = "user-id"

private val userRoles = setOf("MANAGE_USERS", "MANAGE_ROOMS")
private val sessionExpiryTime = Instant.now().plusSeconds(3000)
val session = UserSession(
    sessionId = SESSION_ID,
    userId = USER_ID,
    loginId = "login-id",
    userRoles = userRoles,
    anonymousUser = false,
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
        val session = session.copy(
            sessionExpiryTime = Instant.now().plusSeconds(3),
        )

        underTest.insertUserSession(session)

        Thread.sleep(1000)

        assertThat(loadAllSessions()).containsExactly(session)

        Thread.sleep(3000)

        assertThat(loadAllSessions()).isEmpty()
    }

    @Test
    fun `should return anonymous session by ID`() {
        val session = session.copy(
            loginId = null,
            anonymousUser = true,
        )

        underTest.insertUserSession(session)

        val result = underTest.loadUserSession(SESSION_ID)

        assertThat(result).isEqualTo(session)
    }

    @Test
    fun `should return non-anonymous session by ID`() {
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
        jedisPooled.keys(Namespace.session.key("*"))
            .map { jedisPooled.hgetAll(it) }
            .map { it.toUserSession() }
}
