package me.aburke.hotelbooking.facade.rest.api.auth.v1.user

import io.javalin.Javalin
import io.javalin.testtools.JavalinTest.test
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.assertThatJson
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.user.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

private const val LOGIN_ID = "login-id"
private const val PASSWORD = "password"
private const val NAME = "name"
private const val SESSION_ID = "session-id"
private const val USER_ID = "user-id"

private val roles = setOf(UserRole.CUSTOMER)
private val sessionExpiryTime = Instant.now().plusSeconds(90)
private val anonymousSessionExpiryTime = Instant.now().plusSeconds(30)

class SignUpHandlerTest {

    private val stubs = Stubs()

    private lateinit var javalin: Javalin

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    @Test
    fun `should create user and set session cookie`() = test(javalin) { _, client ->
        every {
            stubs.signUpPort.run(
                SignUpDetails(
                    loginId = LOGIN_ID,
                    rawPassword = PASSWORD,
                    name = NAME,
                    anonymousUser = null,
                )
            )
        } returns SignUpResult.Success(
            UserSession(
                sessionId = SESSION_ID,
                userId = USER_ID,
                loginId = LOGIN_ID,
                userRoles = roles,
                anonymousUser = false,
                sessionExpiryTime = sessionExpiryTime,
            )
        )

        val response = client.request("/api/auth/v1/user") {
            it.header("Content-Type", "application/json")
                .post(
                    """
                        {
                            "login_id": "$LOGIN_ID",
                            "password": "$PASSWORD",
                            "name": "$NAME"
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
                        "user_roles": ["${roles.first()}"],
                        "anonymous_user": false,
                        "session_expiry_time": "$sessionExpiryTime"
                    }
                """.trimIndent()
            )
            s.check {
                verify(exactly = 1) {
                    stubs.signUpPort.run(
                        SignUpDetails(
                            loginId = LOGIN_ID,
                            rawPassword = PASSWORD,
                            name = NAME,
                            anonymousUser = null,
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
    fun `should create new user when provided session ID is not valid`() = test(javalin) { _, client ->
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(
                    sessionId = SESSION_ID,
                )
            )
        } returns GetAuthStateResult.SessionDoesNotExist
        every {
            stubs.signUpPort.run(
                SignUpDetails(
                    loginId = LOGIN_ID,
                    rawPassword = PASSWORD,
                    name = NAME,
                    anonymousUser = null,
                )
            )
        } returns SignUpResult.Success(
            UserSession(
                sessionId = SESSION_ID,
                userId = USER_ID,
                loginId = LOGIN_ID,
                userRoles = roles,
                anonymousUser = false,
                sessionExpiryTime = sessionExpiryTime,
            )
        )

        val response = client.request("/api/auth/v1/user") {
            it.header("Cookie", "$AUTH_COOKIE_KEY=$SESSION_ID")
                .header("Content-Type", "application/json")
                .post(
                    """
                        {
                            "login_id": "$LOGIN_ID",
                            "password": "$PASSWORD",
                            "name": "$NAME"
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
                        "user_roles": ["${roles.first()}"],
                        "anonymous_user": false,
                        "session_expiry_time": "$sessionExpiryTime"
                    }
                """.trimIndent()
            )
            s.check {
                verify(exactly = 1) {
                    stubs.getAuthStatePort.run(
                        GetAuthStateDetails(
                            sessionId = SESSION_ID,
                        )
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    stubs.signUpPort.run(
                        SignUpDetails(
                            loginId = LOGIN_ID,
                            rawPassword = PASSWORD,
                            name = NAME,
                            anonymousUser = null,
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
    fun `should return 409 when username is not available`() = test(javalin) { _, client ->
        every {
            stubs.signUpPort.run(
                SignUpDetails(
                    loginId = LOGIN_ID,
                    rawPassword = PASSWORD,
                    name = NAME,
                    anonymousUser = null,
                )
            )
        } returns SignUpResult.UsernameNotAvailable

        val response = client.request("/api/auth/v1/user") {
            it.header("Content-Type", "application/json")
                .post(
                    """
                        {
                            "login_id": "$LOGIN_ID",
                            "password": "$PASSWORD",
                            "name": "$NAME"
                        }
                    """.trimIndent().toRequestBody("application/json".toMediaType())
                )
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(409)
            s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "title": "Username Conflict",
                        "code": "CONFLICT",
                        "status": 409,
                        "detail": "Username is not available",
                        "instance": "/api/auth/v1/user",
                        "extended_details": []
                    }
                """.trimIndent()
            )
            s.check {
                verify(exactly = 1) {
                    stubs.signUpPort.run(
                        SignUpDetails(
                            loginId = LOGIN_ID,
                            rawPassword = PASSWORD,
                            name = NAME,
                            anonymousUser = null,
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
    fun `should create credentials for anonymous user`() = test(javalin) { _, client ->
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(
                    sessionId = SESSION_ID,
                )
            )
        } returns GetAuthStateResult.SessionExists(
            UserSession(
                sessionId = SESSION_ID,
                userId = USER_ID,
                loginId = null,
                userRoles = roles,
                anonymousUser = true,
                sessionExpiryTime = anonymousSessionExpiryTime,
            )
        )
        every {
            stubs.signUpPort.run(
                SignUpDetails(
                    loginId = LOGIN_ID,
                    rawPassword = PASSWORD,
                    name = NAME,
                    anonymousUser = AnonymousSession(
                        sessionId = SESSION_ID,
                        userId = USER_ID,
                    ),
                )
            )
        } returns SignUpResult.Success(
            UserSession(
                sessionId = SESSION_ID,
                userId = USER_ID,
                loginId = LOGIN_ID,
                userRoles = roles,
                anonymousUser = false,
                sessionExpiryTime = sessionExpiryTime,
            )
        )

        val response = client.request("/api/auth/v1/user") {
            it.header("Cookie", "$AUTH_COOKIE_KEY=$SESSION_ID")
                .header("Content-Type", "application/json")
                .post(
                    """
                        {
                            "login_id": "$LOGIN_ID",
                            "password": "$PASSWORD",
                            "name": "$NAME"
                        }
                    """.trimIndent().toRequestBody("application/json".toMediaType())
                )
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(201)
            s.assertThat(response.header("Set-Cookie")).isNull()
            s.assertThat(response.header("Content-Type")).isEqualTo("application/json")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "user_id": "$USER_ID",
                        "login_id": "$LOGIN_ID",
                        "user_roles": ["${roles.first()}"],
                        "anonymous_user": false,
                        "session_expiry_time": "$sessionExpiryTime"
                    }
                """.trimIndent()
            )
            s.check {
                verify(exactly = 1) {
                    stubs.getAuthStatePort.run(
                        GetAuthStateDetails(
                            sessionId = SESSION_ID,
                        )
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    stubs.signUpPort.run(
                        SignUpDetails(
                            loginId = LOGIN_ID,
                            rawPassword = PASSWORD,
                            name = NAME,
                            anonymousUser = AnonymousSession(
                                sessionId = SESSION_ID,
                                userId = USER_ID,
                            ),
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
    fun `should return 409 when username is not available for anonymous user`() = test(javalin) { _, client ->
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(
                    sessionId = SESSION_ID,
                )
            )
        } returns GetAuthStateResult.SessionExists(
            UserSession(
                sessionId = SESSION_ID,
                userId = USER_ID,
                loginId = null,
                userRoles = roles,
                anonymousUser = true,
                sessionExpiryTime = anonymousSessionExpiryTime,
            )
        )
        every {
            stubs.signUpPort.run(
                SignUpDetails(
                    loginId = LOGIN_ID,
                    rawPassword = PASSWORD,
                    name = NAME,
                    anonymousUser = AnonymousSession(
                        sessionId = SESSION_ID,
                        userId = USER_ID,
                    ),
                )
            )
        } returns SignUpResult.UsernameNotAvailable

        val response = client.request("/api/auth/v1/user") {
            it.header("Cookie", "$AUTH_COOKIE_KEY=$SESSION_ID")
                .header("Content-Type", "application/json")
                .post(
                    """
                        {
                            "login_id": "$LOGIN_ID",
                            "password": "$PASSWORD",
                            "name": "$NAME"
                        }
                    """.trimIndent().toRequestBody("application/json".toMediaType())
                )
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(409)
            s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "title": "Username Conflict",
                        "code": "CONFLICT",
                        "status": 409,
                        "detail": "Username is not available",
                        "instance": "/api/auth/v1/user",
                        "extended_details": []
                    }
                """.trimIndent()
            )
            s.check {
                verify(exactly = 1) {
                    stubs.getAuthStatePort.run(
                        GetAuthStateDetails(
                            sessionId = SESSION_ID,
                        )
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    stubs.signUpPort.run(
                        SignUpDetails(
                            loginId = LOGIN_ID,
                            rawPassword = PASSWORD,
                            name = NAME,
                            anonymousUser = AnonymousSession(
                                sessionId = SESSION_ID,
                                userId = USER_ID,
                            ),
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
    fun `should return 409 when current user is not anonymous`() = test(javalin) { _, client ->
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(
                    sessionId = SESSION_ID,
                )
            )
        } returns GetAuthStateResult.SessionExists(
            UserSession(
                sessionId = SESSION_ID,
                userId = USER_ID,
                loginId = LOGIN_ID,
                userRoles = roles,
                anonymousUser = false,
                sessionExpiryTime = anonymousSessionExpiryTime,
            )
        )
        every {
            stubs.signUpPort.run(
                SignUpDetails(
                    loginId = LOGIN_ID,
                    rawPassword = PASSWORD,
                    name = NAME,
                    anonymousUser = AnonymousSession(
                        sessionId = SESSION_ID,
                        userId = USER_ID,
                    ),
                )
            )
        } returns SignUpResult.UserIsNotAnonymous

        val response = client.request("/api/auth/v1/user") {
            it.header("Cookie", "$AUTH_COOKIE_KEY=$SESSION_ID")
                .header("Content-Type", "application/json")
                .post(
                    """
                        {
                            "login_id": "$LOGIN_ID",
                            "password": "$PASSWORD",
                            "name": "$NAME"
                        }
                    """.trimIndent().toRequestBody("application/json".toMediaType())
                )
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(409)
            s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "title": "User is not anonymous",
                        "code": "CONFLICT",
                        "status": 409,
                        "detail": "User is not anonymous",
                        "instance": "/api/auth/v1/user",
                        "extended_details": []
                    }
                """.trimIndent()
            )
            s.check {
                verify(exactly = 1) {
                    stubs.getAuthStatePort.run(
                        GetAuthStateDetails(
                            sessionId = SESSION_ID,
                        )
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    stubs.signUpPort.run(
                        SignUpDetails(
                            loginId = LOGIN_ID,
                            rawPassword = PASSWORD,
                            name = NAME,
                            anonymousUser = AnonymousSession(
                                sessionId = SESSION_ID,
                                userId = USER_ID,
                            ),
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
    fun `should return 400 when current user does not exist`() = test(javalin) { _, client ->
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(
                    sessionId = SESSION_ID,
                )
            )
        } returns GetAuthStateResult.SessionExists(
            UserSession(
                sessionId = SESSION_ID,
                userId = USER_ID,
                loginId = null,
                userRoles = roles,
                anonymousUser = true,
                sessionExpiryTime = anonymousSessionExpiryTime,
            )
        )
        every {
            stubs.signUpPort.run(
                SignUpDetails(
                    loginId = LOGIN_ID,
                    rawPassword = PASSWORD,
                    name = NAME,
                    anonymousUser = AnonymousSession(
                        sessionId = SESSION_ID,
                        userId = USER_ID,
                    ),
                )
            )
        } returns SignUpResult.AnonymousUserDoesNotExist

        val response = client.request("/api/auth/v1/user") {
            it.header("Cookie", "$AUTH_COOKIE_KEY=$SESSION_ID")
                .header("Content-Type", "application/json")
                .post(
                    """
                        {
                            "login_id": "$LOGIN_ID",
                            "password": "$PASSWORD",
                            "name": "$NAME"
                        }
                    """.trimIndent().toRequestBody("application/json".toMediaType())
                )
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(400)
            s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "title": "User does not exist",
                        "code": "BAD_REQUEST",
                        "status": 400,
                        "detail": "User does not exist",
                        "instance": "/api/auth/v1/user",
                        "extended_details": []
                    }
                """.trimIndent()
            )
            s.check {
                verify(exactly = 1) {
                    stubs.getAuthStatePort.run(
                        GetAuthStateDetails(
                            sessionId = SESSION_ID,
                        )
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    stubs.signUpPort.run(
                        SignUpDetails(
                            loginId = LOGIN_ID,
                            rawPassword = PASSWORD,
                            name = NAME,
                            anonymousUser = AnonymousSession(
                                sessionId = SESSION_ID,
                                userId = USER_ID,
                            ),
                        )
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }
}
