package me.aburke.hotelbooking.facade.rest.api.auth.v1.user

import io.javalin.Javalin
import io.javalin.testtools.JavalinTest.test
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import me.aburke.hotelbooking.facade.rest.client
import me.aburke.hotelbooking.facade.rest.parseResponse
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.user.*
import me.aburke.hotelbooking.rest.client.api.AuthApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.model.ProblemResponse
import me.aburke.hotelbooking.rest.client.model.SessionResponse
import me.aburke.hotelbooking.rest.client.model.SignUpRequest
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.ZoneOffset

private const val LOGIN_ID = "login-id"
private const val PASSWORD = "password"
private const val NAME = "name"
private const val SESSION_ID = "session-id"
private const val USER_ID = "user-id"

private val roles = setOf(UserRole.CUSTOMER)
private val sessionExpiryTime = Instant.now().plusSeconds(90)
private val anonymousSessionExpiryTime = Instant.now().plusSeconds(30)

class SignUpTest {

    private val stubs = Stubs()

    private lateinit var javalin: Javalin

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    @Test
    fun `should create user and set session cookie`() = test(javalin) { _, _ ->
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

        val response = AuthApi(javalin.client()).signUpWithHttpInfo(
            SignUpRequest().apply {
                loginId = LOGIN_ID
                password = PASSWORD
                name = NAME
            }
        )

        assertSoftly { s ->
            s.assertThat(response.statusCode).isEqualTo(201)
            s.assertThat(response.headers["Set-Cookie"]).containsExactly(
                "$AUTH_COOKIE_KEY=$SESSION_ID; Path=/; HttpOnly; SameSite=Strict"
            )
            s.assertThat(response.data).isEqualTo(
                SessionResponse().also {
                    it.userId = USER_ID
                    it.loginId = LOGIN_ID
                    it.userRoles = roles.map { it.name }
                    it.anonymousUser = false
                    it.sessionExpiryTime = sessionExpiryTime.atOffset(ZoneOffset.UTC)
                }
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
    fun `should create new user when provided session ID is not valid`() = test(javalin) { _, _ ->
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

        val response = AuthApi(javalin.client(SESSION_ID)).signUpWithHttpInfo(
            SignUpRequest().apply {
                loginId = LOGIN_ID
                password = PASSWORD
                name = NAME
            }
        )

        assertSoftly { s ->
            s.assertThat(response.statusCode).isEqualTo(201)
            s.assertThat(response.headers["Set-Cookie"]).containsExactly(
                "$AUTH_COOKIE_KEY=$SESSION_ID; Path=/; HttpOnly; SameSite=Strict"
            )
            s.assertThat(response.data).isEqualTo(
                SessionResponse().also {
                    it.userId = USER_ID
                    it.loginId = LOGIN_ID
                    it.userRoles = roles.map { it.name }
                    it.anonymousUser = false
                    it.sessionExpiryTime = sessionExpiryTime.atOffset(ZoneOffset.UTC)
                }
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
    fun `should return 409 when username is not available`() = test(javalin) { _, _ ->
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

        val response = assertThrows<ApiException> {
            AuthApi(javalin.client()).signUp(
                SignUpRequest().apply {
                    loginId = LOGIN_ID
                    password = PASSWORD
                    name = NAME
                }
            )
        }
        val responseBody = response.responseBody.parseResponse<ProblemResponse>()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(409)
            s.assertThat(response.responseHeaders["Content-Type"])
                .containsExactly("application/problem+json;charset=utf-8")
            s.assertThat(responseBody).isEqualTo(
                ProblemResponse().apply {
                    title = "Username Conflict"
                    code = "CONFLICT"
                    status = 409
                    detail = "Username is not available"
                    instance = "/api/auth/v1/user"
                    extendedDetails = emptyList()
                }
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
    fun `should create credentials for anonymous user`() = test(javalin) { _, _ ->
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

        val response = AuthApi(javalin.client(SESSION_ID)).signUpWithHttpInfo(
            SignUpRequest().apply {
                loginId = LOGIN_ID
                password = PASSWORD
                name = NAME
            }
        )

        assertSoftly { s ->
            s.assertThat(response.statusCode).isEqualTo(201)
            s.assertThat(response.headers["Set-Cookie"]).isNull()
            s.assertThat(response.data).isEqualTo(
                SessionResponse().also {
                    it.userId = USER_ID
                    it.loginId = LOGIN_ID
                    it.userRoles = roles.map { it.name }
                    it.anonymousUser = false
                    it.sessionExpiryTime = sessionExpiryTime.atOffset(ZoneOffset.UTC)
                }
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
    fun `should return 409 when username is not available for anonymous user`() = test(javalin) { _, _ ->
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

        val response = assertThrows<ApiException> {
            AuthApi(javalin.client(SESSION_ID)).signUp(
                SignUpRequest().apply {
                    loginId = LOGIN_ID
                    password = PASSWORD
                    name = NAME
                }
            )
        }
        val responseBody = response.responseBody.parseResponse<ProblemResponse>()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(409)
            s.assertThat(response.responseHeaders["Content-Type"])
                .containsExactly("application/problem+json;charset=utf-8")
            s.assertThat(responseBody).isEqualTo(
                ProblemResponse().apply {
                    title = "Username Conflict"
                    code = "CONFLICT"
                    status = 409
                    detail = "Username is not available"
                    instance = "/api/auth/v1/user"
                    extendedDetails = emptyList()
                }
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
    fun `should return 409 when current user is not anonymous`() = test(javalin) { _, _ ->
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

        val response = assertThrows<ApiException> {
            AuthApi(javalin.client(SESSION_ID)).signUp(
                SignUpRequest().apply {
                    loginId = LOGIN_ID
                    password = PASSWORD
                    name = NAME
                }
            )
        }
        val responseBody = response.responseBody.parseResponse<ProblemResponse>()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(409)
            s.assertThat(response.responseHeaders["Content-Type"])
                .containsExactly("application/problem+json;charset=utf-8")
            s.assertThat(responseBody).isEqualTo(
                ProblemResponse().apply {
                    title = "User is not anonymous"
                    code = "CONFLICT"
                    status = 409
                    detail = "User is not anonymous"
                    instance = "/api/auth/v1/user"
                    extendedDetails = emptyList()
                }
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
