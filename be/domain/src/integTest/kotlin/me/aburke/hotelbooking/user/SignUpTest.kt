package me.aburke.hotelbooking.user

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.model.user.toDbModel
import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.ports.scenario.user.AnonymousSession
import me.aburke.hotelbooking.ports.scenario.user.SignUpDetails
import me.aburke.hotelbooking.ports.scenario.user.SignUpPort
import me.aburke.hotelbooking.ports.scenario.user.SignUpResult
import me.aburke.hotelbooking.sessionDuration
import me.aburke.hotelbooking.stubs.Stubs
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.time.Instant

private const val LOGIN_ID = "login-id"
private const val RAW_PASSWORD = "raw-password"
private const val NAME = "name"
private const val ANONYMOUS_SESSION_ID = "anonymous-session-id"

class SignUpTest {

    private val stubs = Stubs()

    private lateinit var app: KoinApplication
    private lateinit var passwordHasher: PasswordHasher
    private lateinit var underTest: SignUpPort

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
        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUser = null,
            ),
        )
        val now = stubs.time

        val session = (result as? SignUpResult.Success)?.session
        val userRecord = session?.userId?.let { stubs.userRepository.getUsers()[it] }
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
                            loginId = LOGIN_ID,
                            userRoles = setOf(UserRole.CUSTOMER),
                            anonymousUser = false,
                            sessionExpiryTime = Instant.EPOCH,
                        ),
                    ),
                )
            s.assertThat(session?.sessionExpiryTime).isEqualTo(now.plus(sessionDuration))
            s.assertThat(stubs.userRepository.getUsers().keys).containsExactly(session?.userId)
            s.assertThat(stubs.userRepository.getAnonymousUserIds()).isEmpty()
            s.assertThat(userRecord).usingRecursiveComparison()
                .ignoringFields("passwordHash")
                .isEqualTo(
                    InsertUserRecord(
                        loginId = LOGIN_ID,
                        passwordHash = "",
                        name = NAME,
                        roles = setOf(UserRole.CUSTOMER.name),
                    ),
                )
            s.assertThat(passwordHashResult).isTrue
            s.assertThat(stubs.sessionRepository.getSessions()).isEqualTo(
                mapOf(
                    session?.sessionId to session?.toDbModel(),
                ),
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
                anonymousUser = null,
            ),
        ).let {
            Assertions.assertThat(it).isInstanceOf(SignUpResult.Success::class.java)
            (it as SignUpResult.Success).session.userId
        }
        stubs.sessionRepository.clearAllSessions()

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUser = null,
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(SignUpResult.UsernameNotAvailable)
            s.assertThat(stubs.userRepository.getUsers().keys).containsExactly(firstUserId)
            s.assertThat(stubs.userRepository.getAnonymousUserIds()).isEmpty()
            s.assertThat(stubs.sessionRepository.getSessions()).isEmpty()
        }
    }

    @Test
    fun `should create credentials for anonymous user`() {
        val anonymousUserId = stubs.userRepository.createAnonymousUser()

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUser = AnonymousSession(
                    sessionId = ANONYMOUS_SESSION_ID,
                    userId = anonymousUserId,
                ),
            ),
        )
        val now = stubs.time

        val session = (result as? SignUpResult.Success)?.session
        val userRecord = session?.userId?.let { stubs.userRepository.getUsers()[it] }
        val passwordHashResult = userRecord?.passwordHash?.let {
            passwordHasher.passwordMatches(RAW_PASSWORD, it)
        }

        assertSoftly { s ->
            s.assertThat(result).usingRecursiveComparison()
                .ignoringFields("session.sessionExpiryTime")
                .isEqualTo(
                    SignUpResult.Success(
                        UserSession(
                            sessionId = ANONYMOUS_SESSION_ID,
                            userId = anonymousUserId,
                            loginId = LOGIN_ID,
                            userRoles = setOf(UserRole.CUSTOMER),
                            anonymousUser = false,
                            sessionExpiryTime = Instant.EPOCH,
                        ),
                    ),
                )
            s.assertThat(session?.sessionExpiryTime).isEqualTo(now.plus(sessionDuration))
            s.assertThat(stubs.userRepository.getUsers().keys).containsExactly(session?.userId)
            s.assertThat(stubs.userRepository.getAnonymousUserIds()).containsExactly(session?.userId)
            s.assertThat(userRecord).usingRecursiveComparison()
                .ignoringFields("passwordHash")
                .isEqualTo(
                    InsertUserRecord(
                        loginId = LOGIN_ID,
                        passwordHash = "",
                        name = NAME,
                        roles = setOf(UserRole.CUSTOMER.name),
                    ),
                )
            s.assertThat(passwordHashResult).isTrue
            s.assertThat(stubs.sessionRepository.getSessions()).isEqualTo(
                mapOf(
                    ANONYMOUS_SESSION_ID to session?.toDbModel(),
                ),
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
                anonymousUser = null,
            ),
        ).let {
            Assertions.assertThat(it).isInstanceOf(SignUpResult.Success::class.java)
            (it as SignUpResult.Success).session.userId
        }
        stubs.sessionRepository.clearAllSessions()

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUser = AnonymousSession(
                    sessionId = ANONYMOUS_SESSION_ID,
                    userId = firstUserId,
                ),
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(SignUpResult.UserIsNotAnonymous)
            s.assertThat(stubs.userRepository.getUsers().keys).containsExactly(firstUserId)
            s.assertThat(stubs.sessionRepository.getSessions()).isEmpty()
        }
    }

    @Test
    fun `should return UsernameNotAvailable when loginId is not available for anonymous user`() {
        val firstUserId = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUser = null,
            ),
        ).let {
            Assertions.assertThat(it).isInstanceOf(SignUpResult.Success::class.java)
            (it as SignUpResult.Success).session.userId
        }
        val anonymousUserId = stubs.userRepository.createAnonymousUser()
        stubs.sessionRepository.clearAllSessions()

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUser = AnonymousSession(
                    sessionId = ANONYMOUS_SESSION_ID,
                    userId = anonymousUserId,
                ),
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(SignUpResult.UsernameNotAvailable)
            s.assertThat(stubs.userRepository.getUsers().keys).containsExactly(firstUserId)
            s.assertThat(stubs.sessionRepository.getSessions()).isEmpty()
        }
    }
}
