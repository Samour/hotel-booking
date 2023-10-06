package me.aburke.hotelbooking.session

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Duration
import java.time.Instant

private const val USER_ID = "user-id"
private const val LOGIN_ID = "login-id"

private val userRoles = setOf(UserRole.MANAGE_ROOMS)
private val sessionDuration = Duration.parse("PT30M")
private val instant = Instant.now()

@ExtendWith(MockKExtension::class)
class SessionFactoryTest {

    @MockK
    lateinit var clock: Clock

    private lateinit var underTest: SessionFactory

    @BeforeEach
    fun init() {
        underTest = SessionFactory(clock, sessionDuration)
    }

    @Test
    fun `should generate session for user with secure session ID and expiry time`() {
        every {
            clock.instant()
        } returns instant

        val result = underTest.createForUser(
            userId = USER_ID,
            loginId = LOGIN_ID,
            userRoles = userRoles,
            anonymousUser = false,
        )

        assertSoftly { s ->
            s.assertThat(result).usingRecursiveComparison()
                .ignoringFields("sessionId")
                .isEqualTo(
                    UserSession(
                        sessionId = "",
                        userId = USER_ID,
                        loginId = LOGIN_ID,
                        userRoles = userRoles,
                        anonymousUser = false,
                        sessionExpiryTime = instant.plus(sessionDuration),
                    ),
                )
            s.assertThat(result.sessionId).matches("[a-zA-Z0-9]{36}")
        }
    }
}
