package me.aburke.hotelbooking.user

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.scenario.user.SignUpDetails
import me.aburke.hotelbooking.scenario.user.SignUpResult
import me.aburke.hotelbooking.scenario.user.SignUpScenario
import me.aburke.hotelbooking.sessionDuration
import me.aburke.hotelbooking.stubs.Stubs
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val LOGIN_ID = "login-id"
private const val RAW_PASSWORD = "raw-password"
private const val NAME = "name"

class SignUpTest {

    private val stubs = Stubs()

    private lateinit var app: KoinApplication
    private lateinit var passwordHasher: PasswordHasher
    private lateinit var underTest: SignUpScenario

    @BeforeEach
    fun init() {
        app = stubs.make()
        passwordHasher = app.koin.get()
        underTest = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should insert new user`() {
        val now = Instant.now()
        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = null,
            )
        )

        val session = (result as? SignUpResult.Success)?.session
        val userRecord = session?.userId?.let { stubs.userRepositoryStub.getUsers()[it] }
        val passwordHashResult = userRecord?.passwordHash?.let {
            passwordHasher.passwordMatches(RAW_PASSWORD, it)
        }

        assertSoftly { s ->
            s.assertThat(result).usingRecursiveComparison()
                .ignoringFields("session.sessionId", "session.userId", "session.sessionExpiryTime")
                .isEqualTo(
                    SignUpResult.Success(
                        UserSession(
                            sessionId = "",
                            userId = "",
                            userRoles = setOf(UserRole.CUSTOMER),
                            anonymousUser = false,
                            sessionExpiryTime = Instant.EPOCH,
                        )
                    )
                )
            s.assertThat(session?.sessionExpiryTime).isCloseTo(
                now.plus(sessionDuration),
                Assertions.within(1, ChronoUnit.SECONDS)
            )
            s.assertThat(stubs.userRepositoryStub.getUsers().keys).containsExactly(session?.userId)
            s.assertThat(stubs.userRepositoryStub.getAnonymousUserIds()).isEmpty()
            s.assertThat(userRecord).usingRecursiveComparison()
                .ignoringFields("passwordHash")
                .isEqualTo(
                    InsertUserRecord(
                        loginId = LOGIN_ID,
                        passwordHash = "",
                        name = NAME,
                        roles = setOf(UserRole.CUSTOMER),
                    )
                )
            s.assertThat(passwordHashResult).isTrue
            s.assertThat(stubs.sessionRepositoryStub.getSessions()).isEqualTo(
                mapOf(
                    session?.sessionId to session,
                )
            )
        }
    }

    @Test
    fun `should return UsernameNotAvailable when loginId is not available for new user`() {
        val firstUserId = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = null,
            )
        ).let {
            Assertions.assertThat(it).isInstanceOf(SignUpResult.Success::class.java)
            (it as SignUpResult.Success).session.userId
        }
        stubs.sessionRepositoryStub.clearAllSessions()

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = null,
            )
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(SignUpResult.UsernameNotAvailable)
            s.assertThat(stubs.userRepositoryStub.getUsers().keys).containsExactly(firstUserId)
            s.assertThat(stubs.userRepositoryStub.getAnonymousUserIds()).isEmpty()
            s.assertThat(stubs.sessionRepositoryStub.getSessions()).isEmpty()
        }
    }

    @Test
    fun `should create credentials for anonymous user`() {
        val anonymousUserId = stubs.userRepositoryStub.createAnonymousUser()
        stubs.sessionRepositoryStub.clearAllSessions()

        val now = Instant.now()
        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = anonymousUserId,
            )
        )

        val session = (result as? SignUpResult.Success)?.session
        val userRecord = session?.userId?.let { stubs.userRepositoryStub.getUsers()[it] }
        val passwordHashResult = userRecord?.passwordHash?.let {
            passwordHasher.passwordMatches(RAW_PASSWORD, it)
        }

        assertSoftly { s ->
            s.assertThat(result).usingRecursiveComparison()
                .ignoringFields("session.sessionId", "session.sessionExpiryTime")
                .isEqualTo(
                    SignUpResult.Success(
                        UserSession(
                            sessionId = "",
                            userId = anonymousUserId,
                            userRoles = setOf(UserRole.CUSTOMER),
                            anonymousUser = false,
                            sessionExpiryTime = Instant.EPOCH,
                        )
                    )
                )
            s.assertThat(session?.sessionExpiryTime).isCloseTo(
                now.plus(sessionDuration),
                Assertions.within(100, ChronoUnit.MILLIS)
            )
            s.assertThat(stubs.userRepositoryStub.getUsers().keys).containsExactly(session?.userId)
            s.assertThat(stubs.userRepositoryStub.getAnonymousUserIds()).containsExactly(session?.userId)
            s.assertThat(userRecord).usingRecursiveComparison()
                .ignoringFields("passwordHash")
                .isEqualTo(
                    InsertUserRecord(
                        loginId = LOGIN_ID,
                        passwordHash = "",
                        name = NAME,
                        roles = setOf(UserRole.CUSTOMER),
                    )
                )
            s.assertThat(passwordHashResult).isTrue
            s.assertThat(stubs.sessionRepositoryStub.getSessions()).isEqualTo(
                mapOf(
                    session?.sessionId to session,
                )
            )
        }
    }

    @Test
    fun `should return UserIsNotAnonymous when trying to set credentials on non-anonymous user`() {
        val firstUserId = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = null,
            )
        ).let {
            Assertions.assertThat(it).isInstanceOf(SignUpResult.Success::class.java)
            (it as SignUpResult.Success).session.userId
        }
        stubs.sessionRepositoryStub.clearAllSessions()

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = firstUserId,
            )
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(SignUpResult.UserIsNotAnonymous)
            s.assertThat(stubs.userRepositoryStub.getUsers().keys).containsExactly(firstUserId)
            s.assertThat(stubs.sessionRepositoryStub.getSessions()).isEmpty()
        }
    }

    @Test
    fun `should return UsernameNotAvailable when loginId is not available for anonymous user`() {
        val firstUserId = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = null,
            )
        ).let {
            Assertions.assertThat(it).isInstanceOf(SignUpResult.Success::class.java)
            (it as SignUpResult.Success).session.userId
        }
        val anonymousUserId = stubs.userRepositoryStub.createAnonymousUser()
        stubs.sessionRepositoryStub.clearAllSessions()

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = anonymousUserId,
            )
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(SignUpResult.UsernameNotAvailable)
            s.assertThat(stubs.userRepositoryStub.getUsers().keys).containsExactly(firstUserId)
            s.assertThat(stubs.sessionRepositoryStub.getSessions()).isEmpty()
        }
    }
}