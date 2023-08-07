package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import io.javalin.Javalin
import io.javalin.testtools.JavalinTest.test
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.assertThatJson
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.user.AnonymousUserCreated
import me.aburke.hotelbooking.ports.scenario.user.CreateAnonymousUserPort
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

private const val SESSION_ID = "session-id"
private const val USER_ID = "user-id"

private val sessionExpiryTime = Instant.now().plusSeconds(90)

class CreateAnonymousSessionHandlerTest {

    private val stubs = Stubs()

    private lateinit var javalin: Javalin

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    @Test
    fun `should create anonymous user and set session cookie`() = test(javalin) { _, client ->
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

        val response = client.post("/api/auth/v1/session/anonymous")

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(201)
            s.assertThat(response.header("Set-Cookie")).isEqualTo(
                "$AUTH_COOKIE_KEY=$SESSION_ID; Path=/; HttpOnly; SameSite=Strict"
            )
            s.assertThat(response.header("Content-Type")).isEqualTo("application/json")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "user_id": "$USER_ID",
                        "user_roles": ["CUSTOMER"],
                        "anonymous_user": true,
                        "session_expiry_time": "$sessionExpiryTime"
                    }
                """.trimIndent()
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
