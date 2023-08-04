package me.aburke.hotelbooking.user

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.ports.repository.InsertUserResult
import me.aburke.hotelbooking.scenario.user.LogInCredentials
import me.aburke.hotelbooking.scenario.user.LogInResult
import me.aburke.hotelbooking.scenario.user.LogInScenario
import me.aburke.hotelbooking.sessionDuration
import me.aburke.hotelbooking.stubs.Stubs
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val LOGIN_ID = "login-id"
private const val PASSWORD = "password"
private const val NAME = "name"

private val userRoles = setOf(UserRole.CUSTOMER)

class LogInTest {

    private val stubs = Stubs()

    private lateinit var app: KoinApplication
    private lateinit var passwordHasher: PasswordHasher

    private lateinit var underTest: LogInScenario

    @BeforeEach
    fun init() {
        app = stubs.make()
        passwordHasher = app.koin.get()
        underTest = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should create and return session when credentials valid`() {
        val userId = (insertUser() as InsertUserResult.UserInserted).userId

        val now = Instant.now()
        val result = underTest.run(
            LogInCredentials(
                loginId = LOGIN_ID,
                password = PASSWORD,
            )
        )
        val session = (result as? LogInResult.UserSessionCreated)?.session
        val allSessions = stubs.sessionRepositoryStub.getSessions().values

        assertSoftly { s ->
            s.assertThat(result).usingRecursiveComparison()
                .ignoringFields("session.sessionId", "session.sessionExpiryTime")
                .isEqualTo(
                    LogInResult.UserSessionCreated(
                        UserSession(
                            sessionId = "",
                            userId = userId,
                            loginId = LOGIN_ID,
                            userRoles = userRoles,
                            anonymousUser = false,
                            sessionExpiryTime = now,
                        )
                    )
                )
            s.assertThat(session?.sessionId).matches("[a-zA-Z0-9]{36}")
            s.assertThat(session?.sessionExpiryTime).isCloseTo(
                now.plus(sessionDuration),
                Assertions.within(100, ChronoUnit.MILLIS)
            )
            s.assertThat(allSessions).containsExactly(session)
        }
    }

    @Test
    fun `should return InvalidCredentials when password is incorrect`() {
        insertUser()

        val result = underTest.run(
            LogInCredentials(
                loginId = LOGIN_ID,
                password = "incorrect-password",
            )
        )
        val allSessions = stubs.sessionRepositoryStub.getSessions().values

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                    LogInResult.InvalidCredentials
                )
            s.assertThat(allSessions).isEmpty()
        }
    }

    @Test
    fun `should return InvalidCredentials when user does not exist`() {
        val result = underTest.run(
            LogInCredentials(
                loginId = LOGIN_ID,
                password = PASSWORD,
            )
        )
        val allSessions = stubs.sessionRepositoryStub.getSessions().values

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                LogInResult.InvalidCredentials
            )
            s.assertThat(allSessions).isEmpty()
        }
    }

    private fun insertUser() = stubs.userRepositoryStub.insertUser(
        InsertUserRecord(
            loginId = LOGIN_ID,
            passwordHash = passwordHasher.hashPassword(PASSWORD),
            name = NAME,
            roles = userRoles,
        )
    )
}
