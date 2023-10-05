package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.client
import me.aburke.hotelbooking.facade.rest.parseResponse
import me.aburke.hotelbooking.rest.client.api.AuthApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.invoker.ApiResponse
import me.aburke.hotelbooking.rest.client.model.ProblemResponse
import me.aburke.hotelbooking.rest.client.model.SessionResponse
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZoneOffset

class GetAuthStateTest : AbstractGetSessionTest() {

    @Test
    fun `should return session details when authenticated`() = test(javalin) { _, _ ->
        `RUN should return session details when authenticated`(
            object : TestRequest<ApiResponse<SessionResponse>>() {
                override fun makeRequest(): ApiResponse<SessionResponse> =
                    AuthApi(javalin.client(sessionId)).fetchAuthStateWithHttpInfo()

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.statusCode).isEqualTo(200)
                    s.assertThat(response.headers["Content-Type"]).containsExactly("application/json")
                    s.assertThat(response.data).isEqualTo(
                        SessionResponse().also { r ->
                            r.userId = userId
                            r.loginId = loginId
                            r.userRoles = userRoles.map { it.name }
                            r.anonymousUser = false
                            r.sessionExpiryTime = sessionExpiryTime.atOffset(ZoneOffset.UTC)
                        },
                    )
                }
            },
        )
    }

    @Test
    fun `should return session details when anonymously authenticated`() = test(javalin) { _, _ ->
        `RUN should return session details when anonymously authenticated`(
            object : TestRequest<ApiResponse<SessionResponse>>() {
                override fun makeRequest(): ApiResponse<SessionResponse> =
                    AuthApi(javalin.client(sessionId)).fetchAuthStateWithHttpInfo()

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.statusCode).isEqualTo(200)
                    s.assertThat(response.headers["Content-Type"]).containsExactly("application/json")
                    s.assertThat(response.data).isEqualTo(
                        SessionResponse().also { r ->
                            r.userId = userId
                            r.userRoles = userRoles.map { it.name }
                            r.anonymousUser = true
                            r.sessionExpiryTime = sessionExpiryTime.atOffset(ZoneOffset.UTC)
                        },
                    )
                }
            },
        )
    }

    @Test
    fun `should return 401 when invalid session ID provided`() = test(javalin) { _, _ ->
        `RUN should return 401 when invalid session ID provided`(
            object : TestRequest<ApiException>() {
                override fun makeRequest(): ApiException = assertThrows<ApiException> {
                    AuthApi(javalin.client(sessionId)).fetchAuthState()
                }

                override fun makeAssertions(s: SoftAssertions) {
                    val responseBody = response.responseBody.parseResponse<ProblemResponse>()
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
                        },
                    )
                }
            },
        )
    }

    @Test
    fun `should return 401 when no session ID provided`() = test(javalin) { _, _ ->
        `RUN should return 401 when no session ID provided`(
            object : TestRequest<ApiException>() {
                override fun makeRequest(): ApiException = assertThrows<ApiException> {
                    AuthApi(javalin.client()).fetchAuthState()
                }

                override fun makeAssertions(s: SoftAssertions) {
                    val responseBody = response.responseBody.parseResponse<ProblemResponse>()
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
                        },
                    )
                }
            },
        )
    }
}
