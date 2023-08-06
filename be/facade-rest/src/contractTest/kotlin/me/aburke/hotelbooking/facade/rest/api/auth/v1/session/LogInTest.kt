package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

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
import me.aburke.hotelbooking.rest.client.api.SessionApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.model.LogInRequest
import me.aburke.hotelbooking.rest.client.model.ProblemResponse
import me.aburke.hotelbooking.rest.client.model.SessionResponse
import me.aburke.hotelbooking.scenario.user.LogInCredentials
import me.aburke.hotelbooking.scenario.user.LogInResult
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.*
import java.time.Instant
import java.time.ZoneOffset

private const val LOGIN_ID = "login-id"
private const val PASSWORD = "password"
private const val SESSION_ID = "session-id"
private const val USER_ID = "user-id"

private val userRoles = setOf(UserRole.CUSTOMER)
private val sessionExpiryTime = Instant.now().plusSeconds(90)

class LogInTest {

    private val stubs = Stubs()

    private lateinit var javalin: Javalin

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    @Test
    fun `should log in user & set session cookie on successful authentication`() = test(javalin) { _, _ ->
        every {
            stubs.logInScenario.run(
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

        val response = javalin.client().logInWithHttpInfo(
            LogInRequest().apply {
                loginId = LOGIN_ID
                password = PASSWORD
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
                    it.userRoles = userRoles.map { it.name }
                    it.anonymousUser = false
                    it.sessionExpiryTime = sessionExpiryTime.atOffset(ZoneOffset.UTC)
                }
            )
            s.check {
                verify(exactly = 1) {
                    stubs.logInScenario.run(
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
    fun `should return 401 when invalid credentials supplied`() = test(javalin) { _, _ ->
        every {
            stubs.logInScenario.run(
                LogInCredentials(
                    loginId = LOGIN_ID,
                    password = PASSWORD,
                )
            )
        } returns LogInResult.InvalidCredentials

        val response = assertThrows<ApiException> {
            javalin.client().logIn(
                LogInRequest().apply {
                    loginId = LOGIN_ID
                    password = PASSWORD
                }
            )
        }
        val responseBody = response.responseBody.parseResponse<ProblemResponse>()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(401)
            s.assertThat(response.responseHeaders["Set-Cookie"]).isNull()
            s.assertThat(response.responseHeaders["Content-Type"]).containsExactly("application/problem+json;charset=utf-8")
            s.assertThat(responseBody).isEqualTo(
                ProblemResponse().apply {
                    title = "Invalid Credentials"
                    code = "UNAUTHORIZED"
                    status = 401
                    detail = "Supplied credentials are not valid"
                    instance = "/api/auth/v1/session"
                    extendedDetails = emptyList()
                }
            )
            s.check {
                verify(exactly = 1) {
                    stubs.logInScenario.run(
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
}
