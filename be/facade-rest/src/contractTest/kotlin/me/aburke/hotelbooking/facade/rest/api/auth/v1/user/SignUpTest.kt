package me.aburke.hotelbooking.facade.rest.api.auth.v1.user

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import me.aburke.hotelbooking.facade.rest.client
import me.aburke.hotelbooking.facade.rest.parseResponse
import me.aburke.hotelbooking.rest.client.api.AuthApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.invoker.ApiResponse
import me.aburke.hotelbooking.rest.client.model.ProblemResponse
import me.aburke.hotelbooking.rest.client.model.SessionResponse
import me.aburke.hotelbooking.rest.client.model.SignUpRequest
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZoneOffset

class SignUpTest : AbstractSignUpTest() {

    @Test
    fun `should create user and set session cookie`() = test(javalin) { _, _ ->
        `RUN should create user and set session cookie`(
            object : TestRequest<ApiResponse<SessionResponse>>() {
                override fun makeRequest(): ApiResponse<SessionResponse> =
                    AuthApi(javalin.client()).signUpWithHttpInfo(
                        SignUpRequest().also {
                            it.loginId = loginId
                            it.password = password
                            it.name = name
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
                            r.userRoles = roles.map { it.name }
                            r.anonymousUser = false
                            r.sessionExpiryTime = sessionExpiryTime.atOffset(ZoneOffset.UTC)
                        },
                    )
                }
            },
        )
    }

    @Test
    fun `should create new user when provided session ID is not valid`() = test(javalin) { _, _ ->
        `RUN should return 401 when provided session ID is not valid`(
            object : TestRequest<ApiException>() {
                override fun makeRequest(): ApiException = assertThrows {
                    AuthApi(javalin.client(sessionId)).signUpWithHttpInfo(
                        SignUpRequest().also {
                            it.loginId = loginId
                            it.password = password
                            it.name = name
                        },
                    )
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
                            instance = "/api/auth/v1/user"
                            extendedDetails = emptyList()
                        },
                    )
                }
            },
        )
    }

    @Test
    fun `should return 409 when username is not available`() = test(javalin) { _, _ ->
        `RUN should return 409 when username is not available`(
            object : TestRequest<ApiException>() {
                override fun makeRequest(): ApiException = assertThrows<ApiException> {
                    AuthApi(javalin.client()).signUp(
                        SignUpRequest().also {
                            it.loginId = loginId
                            it.password = password
                            it.name = name
                        },
                    )
                }

                override fun makeAssertions(s: SoftAssertions) {
                    val responseBody = response.responseBody.parseResponse<ProblemResponse>()
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
                        },
                    )
                }
            },
        )
    }

    @Test
    fun `should create credentials for anonymous user`() = test(javalin) { _, _ ->
        `RUN should create credentials for anonymous user`(
            object : TestRequest<ApiResponse<SessionResponse>>() {
                override fun makeRequest(): ApiResponse<SessionResponse> =
                    AuthApi(javalin.client(sessionId)).signUpWithHttpInfo(
                        SignUpRequest().also {
                            it.loginId = loginId
                            it.password = password
                            it.name = name
                        },
                    )

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.statusCode).isEqualTo(201)
                    s.assertThat(response.headers["Set-Cookie"]).isNull()
                    s.assertThat(response.data).isEqualTo(
                        SessionResponse().also { r ->
                            r.userId = userId
                            r.loginId = loginId
                            r.userRoles = roles.map { it.name }
                            r.anonymousUser = false
                            r.sessionExpiryTime = sessionExpiryTime.atOffset(ZoneOffset.UTC)
                        },
                    )
                }
            },
        )
    }

    @Test
    fun `should return 409 when username is not available for anonymous user`() = test(javalin) { _, _ ->
        `RUN should return 409 when username is not available for anonymous user`(
            object : TestRequest<ApiException>() {
                override fun makeRequest(): ApiException = assertThrows<ApiException> {
                    AuthApi(javalin.client(sessionId)).signUp(
                        SignUpRequest().also {
                            it.loginId = loginId
                            it.password = password
                            it.name = name
                        },
                    )
                }

                override fun makeAssertions(s: SoftAssertions) {
                    val responseBody = response.responseBody.parseResponse<ProblemResponse>()
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
                        },
                    )
                }
            },
        )
    }

    @Test
    fun `should return 409 when current user is not anonymous`() = test(javalin) { _, _ ->
        `RUN should return 409 when current user is not anonymous`(
            object : TestRequest<ApiException>() {
                override fun makeRequest(): ApiException = assertThrows<ApiException> {
                    AuthApi(javalin.client(sessionId)).signUp(
                        SignUpRequest().also {
                            it.loginId = loginId
                            it.password = password
                            it.name = name
                        },
                    )
                }

                override fun makeAssertions(s: SoftAssertions) {
                    val responseBody = response.responseBody.parseResponse<ProblemResponse>()
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
                        },
                    )
                }
            },
        )
    }
}
