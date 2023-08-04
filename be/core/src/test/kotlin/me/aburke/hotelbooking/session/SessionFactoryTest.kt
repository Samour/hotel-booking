package me.aburke.hotelbooking.session

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import org.assertj.core.api.Assertions.within
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val USER_ID = "user-id"
private const val LOGIN_ID = "login-id"

private val userRoles = setOf(UserRole.MANAGE_ROOMS)
private val sessionDuration = Duration.parse("PT30M")

class SessionFactoryTest {

    private val underTest = SessionFactory(sessionDuration)

    @Test
    fun `should generate session for user with secure session ID and expiry time`() {
        val now = Instant.now()
        val result = underTest.createForUser(
            userId = USER_ID,
            loginId = LOGIN_ID,
            userRoles = userRoles,
            anonymousUser = false,
        )

        assertSoftly { s ->
            s.assertThat(result).usingRecursiveComparison()
                .ignoringFields("sessionId", "sessionExpiryTime")
                .isEqualTo(
                    UserSession(
                        sessionId = "",
                        userId = USER_ID,
                        loginId = LOGIN_ID,
                        userRoles = userRoles,
                        anonymousUser = false,
                        sessionExpiryTime = now,
                    )
                )
            s.assertThat(result.sessionId).matches("[a-zA-Z0-9]{36}")
            s.assertThat(result.sessionExpiryTime).isCloseTo(now.plus(sessionDuration), within(50, ChronoUnit.MILLIS))
        }
    }
}
