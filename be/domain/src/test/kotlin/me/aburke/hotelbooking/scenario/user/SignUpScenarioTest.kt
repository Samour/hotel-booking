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
import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.ports.repository.InsertUserResult
import me.aburke.hotelbooking.ports.repository.PromoteAnonymousUserResult
import me.aburke.hotelbooking.ports.repository.SessionRepository
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.ports.scenario.user.AnonymousSession
import me.aburke.hotelbooking.ports.scenario.user.SignUpDetails
import me.aburke.hotelbooking.ports.scenario.user.SignUpResult
import me.aburke.hotelbooking.session.SessionFactory
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import me.aburke.hotelbooking.ports.repository.UserSession as DbUserSession

private const val LOGIN_ID = "login-id"
private const val NAME = "name"
private const val RAW_PASSWORD = "raw-password"
private const val HASHED_PASSWORD = "hashed-password"
private const val USER_ID = "user-id"
private const val ANONYMOUS_SESSION_ID = "anonymous-session-id"
private val sessionExpiryTime = Instant.now()

private val session = UserSession(
    sessionId = "session-id",
    userId = "user-id",
    loginId = LOGIN_ID,
    userRoles = setOf(UserRole.CUSTOMER),
    anonymousUser = false,
    sessionExpiryTime = sessionExpiryTime,
)
private val dbSession = DbUserSession(
    sessionId = "session-id",
    userId = "user-id",
    loginId = LOGIN_ID,
    userRoles = setOf(UserRole.CUSTOMER.name),
    anonymousUser = false,
    sessionExpiryTime = sessionExpiryTime,
)

@ExtendWith(MockKExtension::class)
class SignUpScenarioTest {

    @MockK
    lateinit var passwordHasher: PasswordHasher

    @MockK
    lateinit var sessionFactory: SessionFactory

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var sessionRepository: SessionRepository

    @InjectMockKs
    lateinit var underTest: SignUpScenario

    @Test
    fun `should insert new user`() {
        every {
            passwordHasher.hashPassword(RAW_PASSWORD)
        } returns HASHED_PASSWORD
        every {
            userRepository.insertUser(
                InsertUserRecord(
                    loginId = LOGIN_ID,
                    passwordHash = HASHED_PASSWORD,
                    name = NAME,
                    roles = setOf(UserRole.CUSTOMER.name),
                ),
            )
        } returns InsertUserResult.UserInserted(
            userId = USER_ID,
        )
        every {
            sessionFactory.createForUser(
                userId = USER_ID,
                loginId = LOGIN_ID,
                userRoles = setOf(UserRole.CUSTOMER),
                anonymousUser = false,
            )
        } returns session
        every {
            sessionRepository.insertUserSession(dbSession)
        } returns Unit

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUser = null,
            ),
        )

        SoftAssertions.assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                SignUpResult.Success(session),
            )
            s.check {
                verify(exactly = 1) {
                    passwordHasher.hashPassword(RAW_PASSWORD)
                }
            }
            s.check {
                verify(exactly = 1) {
                    userRepository.insertUser(
                        InsertUserRecord(
                            loginId = LOGIN_ID,
                            passwordHash = HASHED_PASSWORD,
                            name = NAME,
                            roles = setOf(UserRole.CUSTOMER.name),
                        ),
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    sessionFactory.createForUser(
                        userId = USER_ID,
                        loginId = LOGIN_ID,
                        userRoles = setOf(UserRole.CUSTOMER),
                        anonymousUser = false,
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    sessionRepository.insertUserSession(dbSession)
                }
            }
            s.confirmMocks()
        }
    }

    @Test
    fun `should return UsernameNotAvailable when loginId is not available for new user`() {
        every {
            passwordHasher.hashPassword(RAW_PASSWORD)
        } returns HASHED_PASSWORD
        every {
            userRepository.insertUser(
                InsertUserRecord(
                    loginId = LOGIN_ID,
                    passwordHash = HASHED_PASSWORD,
                    name = NAME,
                    roles = setOf(UserRole.CUSTOMER.name),
                ),
            )
        } returns InsertUserResult.LoginIdUniquenessViolation

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUser = null,
            ),
        )

        SoftAssertions.assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                SignUpResult.UsernameNotAvailable,
            )
            s.check {
                verify(exactly = 1) {
                    passwordHasher.hashPassword(RAW_PASSWORD)
                }
            }
            s.check {
                verify(exactly = 1) {
                    userRepository.insertUser(
                        InsertUserRecord(
                            loginId = LOGIN_ID,
                            passwordHash = HASHED_PASSWORD,
                            name = NAME,
                            roles = setOf(UserRole.CUSTOMER.name),
                        ),
                    )
                }
            }
            s.confirmMocks()
        }
    }

    @Test
    fun `should create credentials for anonymous user`() {
        every {
            passwordHasher.hashPassword(RAW_PASSWORD)
        } returns HASHED_PASSWORD
        every {
            userRepository.createCredentialsForAnonymousUser(
                USER_ID,
                InsertUserRecord(
                    loginId = LOGIN_ID,
                    passwordHash = HASHED_PASSWORD,
                    name = NAME,
                    roles = setOf(UserRole.CUSTOMER.name),
                ),
            )
        } returns PromoteAnonymousUserResult.UserCredentialsInserted(
            userId = USER_ID,
        )
        every {
            sessionFactory.createForUser(
                userId = USER_ID,
                loginId = LOGIN_ID,
                userRoles = setOf(UserRole.CUSTOMER),
                anonymousUser = false,
            )
        } returns session
        every {
            sessionRepository.insertUserSession(
                dbSession.copy(sessionId = ANONYMOUS_SESSION_ID),
            )
        } returns Unit

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUser = AnonymousSession(
                    sessionId = ANONYMOUS_SESSION_ID,
                    userId = USER_ID,
                ),
            ),
        )

        SoftAssertions.assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                SignUpResult.Success(
                    session.copy(sessionId = ANONYMOUS_SESSION_ID),
                ),
            )
            s.check {
                verify(exactly = 1) {
                    passwordHasher.hashPassword(RAW_PASSWORD)
                }
            }
            s.check {
                verify(exactly = 1) {
                    userRepository.createCredentialsForAnonymousUser(
                        USER_ID,
                        InsertUserRecord(
                            loginId = LOGIN_ID,
                            passwordHash = HASHED_PASSWORD,
                            name = NAME,
                            roles = setOf(UserRole.CUSTOMER.name),
                        ),
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    sessionFactory.createForUser(
                        userId = USER_ID,
                        loginId = LOGIN_ID,
                        userRoles = setOf(UserRole.CUSTOMER),
                        anonymousUser = false,
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    sessionRepository.insertUserSession(
                        dbSession.copy(sessionId = ANONYMOUS_SESSION_ID),
                    )
                }
            }
            s.confirmMocks()
        }
    }

    @Test
    fun `should return UserIsNotAnonymous when trying to set credentials on non-anonymous user`() {
        every {
            passwordHasher.hashPassword(RAW_PASSWORD)
        } returns HASHED_PASSWORD
        every {
            userRepository.createCredentialsForAnonymousUser(
                USER_ID,
                InsertUserRecord(
                    loginId = LOGIN_ID,
                    passwordHash = HASHED_PASSWORD,
                    name = NAME,
                    roles = setOf(UserRole.CUSTOMER.name),
                ),
            )
        } returns PromoteAnonymousUserResult.UserIsNotAnonymous

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUser = AnonymousSession(
                    sessionId = ANONYMOUS_SESSION_ID,
                    userId = USER_ID,
                ),
            ),
        )

        SoftAssertions.assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                SignUpResult.UserIsNotAnonymous,
            )
            s.check {
                verify(exactly = 1) {
                    passwordHasher.hashPassword(RAW_PASSWORD)
                }
            }
            s.check {
                verify(exactly = 1) {
                    userRepository.createCredentialsForAnonymousUser(
                        USER_ID,
                        InsertUserRecord(
                            loginId = LOGIN_ID,
                            passwordHash = HASHED_PASSWORD,
                            name = NAME,
                            roles = setOf(UserRole.CUSTOMER.name),
                        ),
                    )
                }
            }
            s.confirmMocks()
        }
    }

    @Test
    fun `should return UsernameNotAvailable when loginId is not available for anonymous user`() {
        every {
            passwordHasher.hashPassword(RAW_PASSWORD)
        } returns HASHED_PASSWORD
        every {
            userRepository.createCredentialsForAnonymousUser(
                USER_ID,
                InsertUserRecord(
                    loginId = LOGIN_ID,
                    passwordHash = HASHED_PASSWORD,
                    name = NAME,
                    roles = setOf(UserRole.CUSTOMER.name),
                ),
            )
        } returns PromoteAnonymousUserResult.LoginIdUniquenessViolation

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUser = AnonymousSession(
                    userId = USER_ID,
                    sessionId = ANONYMOUS_SESSION_ID,
                ),
            ),
        )

        SoftAssertions.assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                SignUpResult.UsernameNotAvailable,
            )
            s.check {
                verify(exactly = 1) {
                    passwordHasher.hashPassword(RAW_PASSWORD)
                }
            }
            s.check {
                verify(exactly = 1) {
                    userRepository.createCredentialsForAnonymousUser(
                        USER_ID,
                        InsertUserRecord(
                            loginId = LOGIN_ID,
                            passwordHash = HASHED_PASSWORD,
                            name = NAME,
                            roles = setOf(UserRole.CUSTOMER.name),
                        ),
                    )
                }
            }
            s.confirmMocks()
        }
    }

    @Test
    fun `should return AnonymousUserDoesNotExist when user ID does not exist`() {
        every {
            passwordHasher.hashPassword(RAW_PASSWORD)
        } returns HASHED_PASSWORD
        every {
            userRepository.createCredentialsForAnonymousUser(
                USER_ID,
                InsertUserRecord(
                    loginId = LOGIN_ID,
                    passwordHash = HASHED_PASSWORD,
                    name = NAME,
                    roles = setOf(UserRole.CUSTOMER.name),
                ),
            )
        } returns PromoteAnonymousUserResult.AnonymousUserDoesNotExist

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUser = AnonymousSession(
                    sessionId = ANONYMOUS_SESSION_ID,
                    userId = USER_ID,
                ),
            ),
        )

        SoftAssertions.assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                SignUpResult.AnonymousUserDoesNotExist,
            )
            s.check {
                verify(exactly = 1) {
                    passwordHasher.hashPassword(RAW_PASSWORD)
                }
            }
            s.check {
                verify(exactly = 1) {
                    userRepository.createCredentialsForAnonymousUser(
                        USER_ID,
                        InsertUserRecord(
                            loginId = LOGIN_ID,
                            passwordHash = HASHED_PASSWORD,
                            name = NAME,
                            roles = setOf(UserRole.CUSTOMER.name),
                        ),
                    )
                }
            }
            s.confirmMocks()
        }
    }

    private fun SoftAssertions.confirmMocks() = check {
        confirmVerified(
            passwordHasher,
            sessionFactory,
            userRepository,
            sessionRepository,
        )
    }
}
