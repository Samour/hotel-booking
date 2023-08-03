package me.aburke.hotelbooking.user

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.scenario.user.CreateAnonymousUserScenario
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
        val now = Instant.now()
        val result = underTest.run(CreateAnonymousUserScenario.Detail)

        assertSoftly { s ->
            s.assertThat(result.session).usingRecursiveComparison()
                .ignoringFields("sessionId", "userId", "sessionExpiryTime")
                .isEqualTo(
                    UserSession(
                        sessionId = "",
                        userId = "",
                        userRoles = setOf(UserRole.CUSTOMER),
                        anonymousUser = true,
                        sessionExpiryTime = Instant.EPOCH,
                    )
                )
            s.assertThat(result.session.sessionExpiryTime).isCloseTo(
                now.plus(sessionDuration),
                Assertions.within(100, ChronoUnit.MILLIS)
            )
            s.assertThat(stubs.userRepositoryStub.getAnonymousUserIds()).containsExactly(result.session.userId)
            s.assertThat(stubs.userRepositoryStub.getUsers()).isEmpty()
            s.assertThat(stubs.sessionRepositoryStub.getSessions()).isEqualTo(
                mapOf(
                    result.session.sessionId to result.session,
                )
            )
        }
    }
}
