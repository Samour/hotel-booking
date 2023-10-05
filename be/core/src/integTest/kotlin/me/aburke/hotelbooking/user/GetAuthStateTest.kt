package me.aburke.hotelbooking.user

import me.aburke.hotelbooking.ports.scenario.user.CreateAnonymousUserPort
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateDetails
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStatePort
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateResult
import me.aburke.hotelbooking.stubs.Stubs
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication

class GetAuthStateTest {

    private val stubs = Stubs()

    private lateinit var app: KoinApplication
    private lateinit var createAnonymousUserScenario: CreateAnonymousUserPort

    private lateinit var underTest: GetAuthStatePort

    @BeforeEach
    fun init() {
        app = stubs.make()
        createAnonymousUserScenario = app.koin.get()
        underTest = app.koin.get()
    }

    @AfterEach
    fun tearDown() = app.close()

    @Test
    fun `should return session when exists`() {
        val anonymousUser = createAnonymousUserScenario.run(CreateAnonymousUserPort.Details)

        val result = underTest.run(
            GetAuthStateDetails(
                sessionId = anonymousUser.session.sessionId,
            ),
        )

        assertThat(result).isEqualTo(
            GetAuthStateResult.SessionExists(anonymousUser.session),
        )
    }

    @Test
    fun `should return SessionDoesNotExist when no session exists with ID`() {
        createAnonymousUserScenario.run(CreateAnonymousUserPort.Details)

        val result = underTest.run(
            GetAuthStateDetails(
                sessionId = "wrong-session-id",
            ),
        )

        assertThat(result).isEqualTo(
            GetAuthStateResult.SessionDoesNotExist,
        )
    }
}
