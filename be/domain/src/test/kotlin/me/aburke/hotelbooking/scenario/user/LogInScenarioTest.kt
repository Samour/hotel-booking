package me.aburke.hotelbooking.scenario.user

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.ports.repository.NonAnonymousUserRecord
import me.aburke.hotelbooking.ports.repository.SessionRepository
import me.aburke.hotelbooking.ports.repository.UserCredentialRecord
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.ports.scenario.user.LogInCredentials
import me.aburke.hotelbooking.ports.scenario.user.LogInResult
import me.aburke.hotelbooking.session.SessionFactory
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

private const val DUMMY_PW_HASH = "\$2a\$06\$8vB.M.kAHzcx2fFItjFG3.nY4UBiHTvV9P2xdsHlNmtAFzZ8.QQc."
private const val LOGIN_ID = "login-id"
private const val PASSWORD = "raw-password"
private const val USER_ID = "user-id"
private const val NAME = "name"
private const val PASSWORD_HASH = "password-hash"
private const val SESSION_IDENTIFIER = "session-identifier"

private val userRoles = setOf(UserRole.CUSTOMER)
private val userRecord = NonAnonymousUserRecord(
    userId = USER_ID,
    userRoles = userRoles,
    name = NAME,
    credential = UserCredentialRecord(
        loginId = LOGIN_ID,
        passwordHash = PASSWORD_HASH,
    ),
)
private val userSession = UserSession(
    sessionId = SESSION_IDENTIFIER,
    userId = USER_ID,
    loginId = LOGIN_ID,
    userRoles = userRoles,
    anonymousUser = false,
    sessionExpiryTime = Instant.now().plusSeconds(3600),
)

@ExtendWith(MockKExtension::class)
class LogInScenarioTest {

    @MockK
    lateinit var passwordHasher: PasswordHasher

    @MockK
    lateinit var sessionFactory: SessionFactory

    @MockK
    lateinit var sessionRepository: SessionRepository

    @MockK
    lateinit var userRepository: UserRepository

    @InjectMockKs
    lateinit var underTest: LogInScenario

    @Test
    fun `should create and return session when credentials valid`() {
        every {
            userRepository.findUserByLoginId(LOGIN_ID)
        } returns userRecord
        every {
            passwordHasher.passwordMatches(PASSWORD, PASSWORD_HASH)
        } returns true
        every {
            sessionFactory.createForUser(
                userId = USER_ID,
                loginId = LOGIN_ID,
                userRoles = userRoles,
                anonymousUser = false,
            )
        } returns userSession
        every {
            sessionRepository.insertUserSession(userSession)
        } returns Unit

        val result = underTest.run(
            LogInCredentials(
                loginId = LOGIN_ID,
                password = PASSWORD,
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                LogInResult.UserSessionCreated(userSession),
            )
            s.check {
                verify(exactly = 1) {
                    userRepository.findUserByLoginId(LOGIN_ID)
                }
            }
            s.check {
                verify(exactly = 1) {
                    passwordHasher.passwordMatches(PASSWORD, PASSWORD_HASH)
                }
            }
            s.check {
                verify(exactly = 1) {
                    sessionFactory.createForUser(
                        userId = USER_ID,
                        loginId = LOGIN_ID,
                        userRoles = userRoles,
                        anonymousUser = false,
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    sessionRepository.insertUserSession(userSession)
                }
            }
            s.check {
                confirmVerified(
                    sessionFactory,
                    passwordHasher,
                    userRepository,
                    sessionRepository,
                )
            }
        }
    }

    @Test
    fun `should return InvalidCredentials when password is incorrect`() {
        every {
            userRepository.findUserByLoginId(LOGIN_ID)
        } returns userRecord
        every {
            passwordHasher.passwordMatches(PASSWORD, PASSWORD_HASH)
        } returns false

        val result = underTest.run(
            LogInCredentials(
                loginId = LOGIN_ID,
                password = PASSWORD,
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                LogInResult.InvalidCredentials,
            )
            s.check {
                verify(exactly = 1) {
                    userRepository.findUserByLoginId(LOGIN_ID)
                }
            }
            s.check {
                verify(exactly = 1) {
                    passwordHasher.passwordMatches(PASSWORD, PASSWORD_HASH)
                }
            }
            s.check {
                confirmVerified(
                    sessionFactory,
                    passwordHasher,
                    userRepository,
                    sessionRepository,
                )
            }
        }
    }

    @Test
    fun `should return InvalidCredentials when user does not exist`() {
        every {
            userRepository.findUserByLoginId(LOGIN_ID)
        } returns null
        every {
            passwordHasher.passwordMatches(DUMMY_PW_HASH, "dummy-password")
        } returns false

        val result = underTest.run(
            LogInCredentials(
                loginId = LOGIN_ID,
                password = PASSWORD,
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                LogInResult.InvalidCredentials,
            )
            s.check {
                verify(exactly = 1) {
                    userRepository.findUserByLoginId(LOGIN_ID)
                }
            }
            s.check {
                verify(exactly = 1) {
                    passwordHasher.passwordMatches(DUMMY_PW_HASH, "dummy-password")
                }
            }
            s.check {
                confirmVerified(
                    sessionFactory,
                    passwordHasher,
                    userRepository,
                    sessionRepository,
                )
            }
        }
    }
}
