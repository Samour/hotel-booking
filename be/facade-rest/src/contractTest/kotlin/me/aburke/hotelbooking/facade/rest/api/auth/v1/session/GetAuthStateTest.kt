package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import io.javalin.Javalin
import io.javalin.testtools.JavalinTest.test
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.client
import me.aburke.hotelbooking.facade.rest.parseResponse
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateDetails
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateResult
import me.aburke.hotelbooking.rest.client.api.AuthApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.model.ProblemResponse
import me.aburke.hotelbooking.rest.client.model.SessionResponse
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.ZoneOffset

private const val SESSION_ID = "session-id"
private const val LOGIN_ID = "login-id"
private const val USER_ID = "user-id"

private val userRoles = setOf(UserRole.CUSTOMER)
private val sessionExpiryTime = Instant.now().plusSeconds(90)

class GetAuthStateTest {

    private val stubs = Stubs()

    private lateinit var javalin: Javalin

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    @Test
    fun `should return session details when authenticated`() = test(javalin) { _, _ ->
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

        val response = AuthApi(javalin.client(SESSION_ID)).fetchAuthStateWithHttpInfo()

        assertSoftly { s ->
            s.assertThat(response.statusCode).isEqualTo(200)
            s.assertThat(response.headers["Content-Type"]).containsExactly("application/json")
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
    fun `should return session details when anonymously authenticated`() = test(javalin) { _, _ ->
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

        val response = AuthApi(javalin.client(SESSION_ID)).fetchAuthStateWithHttpInfo()

        assertSoftly { s ->
            s.assertThat(response.statusCode).isEqualTo(200)
            s.assertThat(response.headers["Content-Type"]).containsExactly("application/json")
            s.assertThat(response.data).isEqualTo(
                SessionResponse().also {
                    it.userId = USER_ID
                    it.userRoles = userRoles.map { it.name }
                    it.anonymousUser = true
                    it.sessionExpiryTime = sessionExpiryTime.atOffset(ZoneOffset.UTC)
                }
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
    fun `should return 401 when invalid session ID provided`() = test(javalin) { _, _ ->
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(SESSION_ID)
            )
        } returns GetAuthStateResult.SessionDoesNotExist

        val response = assertThrows<ApiException> {
            AuthApi(javalin.client(SESSION_ID)).fetchAuthState()
        }
        val responseBody = response.responseBody.parseResponse<ProblemResponse>()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(401)
            s.assertThat(response.responseHeaders["Content-Type"])
                .containsExactly("application/problem+json;charset=utf-8")
            s.assertThat(responseBody).isEqualTo(
                ProblemResponse().apply {
                    title = "Not Authorized"
                    code = "UNAUTHORIZED"
                    status = 401
                    detail = "Credentials not provided"
                    instance = "/api/auth/v1/session"
                    extendedDetails = emptyList()
                }
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
    fun `should return 401 when no session ID provided`() = test(javalin) { _, _ ->
        val response = assertThrows<ApiException> {
            AuthApi(javalin.client()).fetchAuthState()
        }
        val responseBody = response.responseBody.parseResponse<ProblemResponse>()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(401)
            s.assertThat(response.responseHeaders["Content-Type"])
                .containsExactly("application/problem+json;charset=utf-8")
            s.assertThat(responseBody).isEqualTo(
                ProblemResponse().apply {
                    title = "Not Authorized"
                    code = "UNAUTHORIZED"
                    status = 401
                    detail = "Credentials not provided"
                    instance = "/api/auth/v1/session"
                    extendedDetails = emptyList()
                }
            )
            with(stubs) {
                s.verifyStubs()
            }
        }
    }
}
