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
import me.aburke.hotelbooking.ports.scenario.user.LogInCredentials
import me.aburke.hotelbooking.ports.scenario.user.LogInResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

private const val LOGIN_ID = "login-id"
private const val PASSWORD = "password"
private const val SESSION_ID = "session-id"
private const val USER_ID = "user-id"

private val userRoles = setOf(UserRole.CUSTOMER)
private val sessionExpiryTime = Instant.now().plusSeconds(90)

class LogInHandlerTest {

    private val stubs = Stubs()

    private lateinit var javalin: Javalin

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    @Test
    fun `should log in user & set session cookie on successful authentication`() = test(javalin) { _, client ->
        every {
            stubs.logInPort.run(
                LogInCredentials(
                    loginId = LOGIN_ID,
                    password = PASSWORD,
                )
            )
        } returns LogInResult.UserSessionCreated(
            UserSession(
                sessionId = SESSION_ID,
                userId = USER_ID,
                loginId = LOGIN_ID,
                userRoles = userRoles,
                anonymousUser = false,
                sessionExpiryTime = sessionExpiryTime,
            )
        )

        val response = client.request("/api/auth/v1/session") {
            it.header("Content-Type", "application/json")
                .post(
                    """
                        {
                            "login_id": "$LOGIN_ID",
                            "password": "$PASSWORD"
                        }
                    """.trimIndent().toRequestBody("application/json".toMediaType())
                )
        }

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
                        "login_id": "$LOGIN_ID",
                        "user_roles": ["${userRoles.first()}"],
                        "anonymous_user": false,
                        "session_expiry_time": "$sessionExpiryTime"
                    }
                """.trimIndent()
            )
            s.check {
                verify(exactly = 1) {
                    stubs.logInPort.run(
                        LogInCredentials(
                            loginId = LOGIN_ID,
                            password = PASSWORD,
                        )
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    @Test
    fun `should return 401 when invalid credentials supplied`() = test(javalin) { _, client ->
        every {
            stubs.logInPort.run(
                LogInCredentials(
                    loginId = LOGIN_ID,
                    password = PASSWORD,
                )
            )
        } returns LogInResult.InvalidCredentials

        val response = client.request("/api/auth/v1/session") {
            it.header("Content-Type", "application/json")
                .post(
                    """
                        {
                            "login_id": "$LOGIN_ID",
                            "password": "$PASSWORD"
                        }
                    """.trimIndent().toRequestBody("application/json".toMediaType())
                )
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(401)
            s.assertThat(response.header("Set-Cookie")).isNull()
            s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "title": "Invalid Credentials",
                        "code": "UNAUTHORIZED",
                        "status": 401,
                        "detail": "Supplied credentials are not valid",
                        "instance": "/api/auth/v1/session",
                        "extended_details": []
                    }
                """.trimIndent()
            )
            s.check {
                verify(exactly = 1) {
                    stubs.logInPort.run(
                        LogInCredentials(
                            loginId = LOGIN_ID,
                            password = PASSWORD,
                        )
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    @Test
    fun `should return 400 when request body has invalid fields`() = test(javalin) { _, client ->
        val response = client.request("/api/auth/v1/session") {
            it.header("Content-Type", "application/json")
                .post(
                    """
                        {
                            "loginId": "$LOGIN_ID",
                            "password": "$PASSWORD"
                        }
                    """.trimIndent().toRequestBody("application/json".toMediaType())
                )
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(400)
            s.assertThat(response.header("Set-Cookie")).isNull()
            s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "title": "Invalid Request",
                        "code": "BAD_REQUEST",
                        "status": 400,
                        "detail": "Request body is not valid",
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

    @Test
    fun `should return 400 when request body is malformed`() = test(javalin) { _, client ->
        val response = client.request("/api/auth/v1/session") {
            it.header("Content-Type", "application/json")
                .post(
                    """
                        {
                            "login_id": "$LOGIN_ID",
                            "password": "$PASSWORD
                        }
                    """.trimIndent().toRequestBody("application/json".toMediaType())
                )
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(400)
            s.assertThat(response.header("Set-Cookie")).isNull()
            s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "title": "Invalid Request",
                        "code": "BAD_REQUEST",
                        "status": 400,
                        "detail": "Request body is not valid",
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
