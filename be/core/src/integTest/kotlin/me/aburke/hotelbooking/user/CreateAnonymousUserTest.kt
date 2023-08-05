package me.aburke.hotelbooking.user

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.scenario.user.CreateAnonymousUserScenario
import me.aburke.hotelbooking.sessionDuration
import me.aburke.hotelbooking.stubs.Stubs
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication

class CreateAnonymousUserTest {

    private val stubs = Stubs()

    private lateinit var app: KoinApplication
    private lateinit var underTest: CreateAnonymousUserScenario

    @BeforeEach
    fun init() {
        app = stubs.make()
        underTest = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should create anonymous user`() {
        val result = underTest.run(CreateAnonymousUserScenario.Detail)

        assertSoftly { s ->
            s.assertThat(result.session).usingRecursiveComparison()
                .ignoringFields("sessionId", "userId")
                .isEqualTo(
                    UserSession(
                        sessionId = "",
                        userId = "",
                        loginId = null,
                        userRoles = setOf(UserRole.CUSTOMER),
                        anonymousUser = true,
                        sessionExpiryTime = stubs.time.plus(sessionDuration),
                    )
                )
            s.assertThat(stubs.userRepository.getAnonymousUserIds()).containsExactly(result.session.userId)
            s.assertThat(stubs.userRepository.getUsers()).isEmpty()
            s.assertThat(stubs.sessionRepository.getSessions()).isEqualTo(
                mapOf(
                    result.session.sessionId to result.session,
                )
            )
        }
    }
}
