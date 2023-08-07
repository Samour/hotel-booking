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
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateDetails
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateResult
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

private const val SESSION_ID = "session-id"
private const val LOGIN_ID = "login-id"
private const val USER_ID = "user-id"

private val userRoles = setOf(UserRole.CUSTOMER)
private val sessionExpiryTime = Instant.now().plusSeconds(90)

class GetSessionHandlerTest {

    private val stubs = Stubs()

    private lateinit var javalin: Javalin

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    @Test
    fun `should return session details when authenticated`() = test(javalin) { _, client ->
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(SESSION_ID)
            )
        } returns GetAuthStateResult.SessionExists(
            UserSession(
                sessionId = SESSION_ID,
                userId = USER_ID,
                loginId = LOGIN_ID,
                userRoles = userRoles,
                anonymousUser = false,
                sessionExpiryTime = sessionExpiryTime,
            )
        )

        val response = client.get("/api/auth/v1/session") {
            it.header("Cookie", "$AUTH_COOKIE_KEY=$SESSION_ID")
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(200)
            s.assertThat(response.header("Content-Type")).isEqualTo("application/json")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "user_id": "$USER_ID",
                        "login_id": "$LOGIN_ID",
                        "user_roles": ["${userRoles.first()}"],
                        "anonymous_user": false,
                        "session_expiry_time": "$sessionExpiryTime"
                    }
                """.trimIndent()
            )
            s.check {
                verify(exactly = 1) {
                    stubs.getAuthStatePort.run(
                        GetAuthStateDetails(SESSION_ID)
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    @Test
    fun `should return session details when anonymously authenticated`() = test(javalin) { _, client ->
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(SESSION_ID)
            )
        } returns GetAuthStateResult.SessionExists(
            UserSession(
                sessionId = SESSION_ID,
                userId = USER_ID,
                loginId = null,
                userRoles = userRoles,
                anonymousUser = true,
                sessionExpiryTime = sessionExpiryTime,
            )
        )

        val response = client.get("/api/auth/v1/session") {
            it.header("Cookie", "$AUTH_COOKIE_KEY=$SESSION_ID")
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(200)
            s.assertThat(response.header("Content-Type")).isEqualTo("application/json")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "user_id": "$USER_ID",
                        "user_roles": ["${userRoles.first()}"],
                        "anonymous_user": true,
                        "session_expiry_time": "$sessionExpiryTime"
                    }
                """.trimIndent()
            )
            s.check {
                verify(exactly = 1) {
                    stubs.getAuthStatePort.run(
                        GetAuthStateDetails(SESSION_ID)
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    @Test
    fun `should return 401 when invalid session ID provided`() = test(javalin) { _, client ->
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(SESSION_ID)
            )
        } returns GetAuthStateResult.SessionDoesNotExist

        val response = client.get("/api/auth/v1/session") {
            it.header("Cookie", "$AUTH_COOKIE_KEY=$SESSION_ID")
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(401)
            s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "title": "Not Authorized",
                        "code": "UNAUTHORIZED",
                        "status": 401,
                        "detail": "Credentials not provided",
                        "instance": "/api/auth/v1/session",
                        "extended_details": []
                    }
                """.trimIndent()
            )
            s.check {
                verify(exactly = 1) {
                    stubs.getAuthStatePort.run(
                        GetAuthStateDetails(SESSION_ID)
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    @Test
    fun `should return 401 when no session ID provided`() = test(javalin) { _, client ->
        val response = client.get("/api/auth/v1/session")

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(401)
            s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "title": "Not Authorized",
                        "code": "UNAUTHORIZED",
                        "status": 401,
                        "detail": "Credentials not provided",
                        "instance": "/api/auth/v1/session",
                        "extended_details": []
                    }
                """.trimIndent()
            )
            with(stubs) {
                s.verifyStubs()
            }
        }
    }
}
