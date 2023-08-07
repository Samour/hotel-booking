package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import io.javalin.Javalin
import io.javalin.testtools.JavalinTest.test
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import me.aburke.hotelbooking.facade.rest.client
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.user.AnonymousUserCreated
import me.aburke.hotelbooking.ports.scenario.user.CreateAnonymousUserPort
import me.aburke.hotelbooking.rest.client.api.AuthApi
import me.aburke.hotelbooking.rest.client.model.SessionResponse
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneOffset

private const val SESSION_ID = "session-id"
private const val USER_ID = "user-id"

private val sessionExpiryTime = Instant.now().plusSeconds(90)

class CreateAnonymousSessionTest {

    private val stubs = Stubs()

    private lateinit var javalin: Javalin

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    @Test
    fun `should create anonymous user and set session cookie`() = test(javalin) { _, _ ->
        every {
            stubs.createAnonymousUserPort.run(CreateAnonymousUserPort.Details)
        } returns AnonymousUserCreated(
            UserSession(
                sessionId = SESSION_ID,
                userId = USER_ID,
                loginId = null,
                userRoles = setOf(UserRole.CUSTOMER),
                anonymousUser = true,
                sessionExpiryTime = sessionExpiryTime,
            )
        )

        val response = AuthApi(javalin.client()).createAnonymousSessionWithHttpInfo()

        assertSoftly { s ->
            s.assertThat(response.statusCode).isEqualTo(201)
            s.assertThat(response.headers["Set-Cookie"]).containsExactly(
                "$AUTH_COOKIE_KEY=$SESSION_ID; Path=/; HttpOnly; SameSite=Strict"
            )
            s.assertThat(response.data).isEqualTo(
                SessionResponse().also {
                    it.userId = USER_ID
                    it.userRoles = listOf("CUSTOMER")
                    it.anonymousUser = true
                    it.sessionExpiryTime = sessionExpiryTime.atOffset(ZoneOffset.UTC)
                }
            )
            s.check {
                verify(exactly = 1) {
                    stubs.createAnonymousUserPort.run(CreateAnonymousUserPort.Details)
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }
}
