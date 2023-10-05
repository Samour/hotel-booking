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
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateDetails
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateResult
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

private const val SESSION_ID = "session-id"

private val session = UserSession(
    sessionId = SESSION_ID,
    userId = "user-id",
    loginId = "login-id",
    userRoles = setOf(UserRole.MANAGE_USERS),
    anonymousUser = false,
    sessionExpiryTime = Instant.now(),
)

@ExtendWith(MockKExtension::class)
class GetAuthStateScenarioTest {

    @MockK
    lateinit var sessionRepository: SessionRepository

    @InjectMockKs
    lateinit var underTest: GetAuthStateScenario

    @Test
    fun `should return session when exists`() {
        every {
            sessionRepository.loadUserSession(SESSION_ID)
        } returns session

        val result = underTest.run(
            GetAuthStateDetails(
                sessionId = SESSION_ID,
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                GetAuthStateResult.SessionExists(session),
            )
            s.check {
                verify(exactly = 1) {
                    sessionRepository.loadUserSession(SESSION_ID)
                }
            }
            s.check {
                confirmVerified(sessionRepository)
            }
        }
    }

    @Test
    fun `should return SessionDoesNotExist when no session exists with ID`() {
        every {
            sessionRepository.loadUserSession(SESSION_ID)
        } returns null

        val result = underTest.run(
            GetAuthStateDetails(
                sessionId = SESSION_ID,
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                GetAuthStateResult.SessionDoesNotExist,
            )
            s.check {
                verify(exactly = 1) {
                    sessionRepository.loadUserSession(SESSION_ID)
                }
            }
            s.check {
                confirmVerified(sessionRepository)
            }
        }
    }
}
