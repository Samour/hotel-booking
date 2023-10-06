package me.aburke.hotelbooking.scenario.user

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.repository.SessionRepository
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.ports.scenario.user.AnonymousUserCreated
import me.aburke.hotelbooking.ports.scenario.user.CreateAnonymousUserPort
import me.aburke.hotelbooking.session.SessionFactory
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import me.aburke.hotelbooking.ports.repository.UserSession as DbUserSession

private const val USER_ID = "user-id"
private val sessionExpiryTime = Instant.now()

private val session = UserSession(
    sessionId = "session-id",
    userId = USER_ID,
    loginId = null,
    userRoles = setOf(UserRole.CUSTOMER),
    anonymousUser = true,
    sessionExpiryTime = sessionExpiryTime,
)
private val dbSession = DbUserSession(
    sessionId = "session-id",
    userId = USER_ID,
    loginId = null,
    userRoles = setOf(UserRole.CUSTOMER.name),
    anonymousUser = true,
    sessionExpiryTime = sessionExpiryTime,
)

@ExtendWith(MockKExtension::class)
class CreateAnonymousUserScenarioTest {

    @MockK
    lateinit var sessionFactory: SessionFactory

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var sessionRepository: SessionRepository

    @InjectMockKs
    lateinit var underTest: CreateAnonymousUserScenario

    @Test
    fun `should create anonymous user`() {
        every {
            userRepository.createAnonymousUser()
        } returns USER_ID
        every {
            sessionFactory.createForUser(
                userId = USER_ID,
                loginId = null,
                userRoles = setOf(UserRole.CUSTOMER),
                anonymousUser = true,
            )
        } returns session
        every {
            sessionRepository.insertUserSession(dbSession)
        } returns Unit

        val result = underTest.run(CreateAnonymousUserPort.Details)

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                AnonymousUserCreated(session),
            )
            s.check {
                verify(exactly = 1) {
                    userRepository.createAnonymousUser()
                }
            }
            s.check {
                verify(exactly = 1) {
                    sessionFactory.createForUser(
                        userId = USER_ID,
                        loginId = null,
                        userRoles = setOf(UserRole.CUSTOMER),
                        anonymousUser = true,
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    sessionRepository.insertUserSession(dbSession)
                }
            }
            s.check {
                confirmVerified(
                    sessionFactory,
                    userRepository,
                    sessionRepository,
                )
            }
        }
    }
}
