package me.aburke.hotelbooking.facade.rest.api.auth.session

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import me.aburke.hotelbooking.facade.rest.client
import me.aburke.hotelbooking.facade.rest.parseResponse
import me.aburke.hotelbooking.rest.client.api.AuthUnstableApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.invoker.ApiResponse
import me.aburke.hotelbooking.rest.client.model.LogInRequest
import me.aburke.hotelbooking.rest.client.model.ProblemResponse
import me.aburke.hotelbooking.rest.client.model.SessionResponse
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZoneOffset

class LogInTest : AbstractLogInTest() {

    @Test
    fun `should log in user & set session cookie on successful authentication`() = test(javalin) { _, _ ->
        `RUN should log in user & set session cookie on successful authentication`(
            object : TestRequest<ApiResponse<SessionResponse>>() {
                override fun makeRequest(): ApiResponse<SessionResponse> =
                    AuthUnstableApi(javalin.client()).logInWithHttpInfo(
                        LogInRequest().also {
                            it.loginId = loginId
                            it.password = password
                        },
                    )

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.statusCode).isEqualTo(201)
                    s.assertThat(response.headers["Set-Cookie"]).containsExactly(
                        "$AUTH_COOKIE_KEY=$sessionId; Path=/; HttpOnly; SameSite=Strict",
                    )
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
    fun `should return 401 when invalid credentials supplied`() = test(javalin) { _, _ ->
        `RUN should return 401 when invalid credentials supplied`(
            object : TestRequest<ApiException>() {
                override fun makeRequest(): ApiException = assertThrows<ApiException> {
                    AuthUnstableApi(javalin.client()).logIn(
                        LogInRequest().also {
                            it.loginId = loginId
                            it.password = password
                        },
                    )
                }

                override fun makeAssertions(s: SoftAssertions) {
                    val responseBody = response.responseBody.parseResponse<ProblemResponse>()
                    s.assertThat(response.code).isEqualTo(401)
                    s.assertThat(response.responseHeaders["Set-Cookie"]).isNull()
                    s.assertThat(response.responseHeaders["Content-Type"])
                        .containsExactly("application/problem+json;charset=utf-8")
                    s.assertThat(responseBody).isEqualTo(
                        ProblemResponse().apply {
                            title = "Invalid Credentials"
                            code = "UNAUTHORIZED"
                            status = 401
                            detail = "Supplied credentials are not valid"
                            instance = "/api/auth/v0/session"
                            extendedDetails = emptyList()
                        },
                    )
                }
            },
        )
    }
}
