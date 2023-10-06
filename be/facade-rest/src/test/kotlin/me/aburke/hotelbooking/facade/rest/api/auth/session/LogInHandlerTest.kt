package me.aburke.hotelbooking.facade.rest.api.auth.session

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.assertThatJson
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

class LogInHandlerTest : AbstractLogInTest() {

    @Test
    fun `should log in user & set session cookie on successful authentication`() = test(javalin) { _, client ->
        `RUN should log in user & set session cookie on successful authentication`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/auth/v0/session") {
                    it.header("Content-Type", "application/json")
                        .post(
                            """
                                {
                                    "login_id": "$loginId",
                                    "password": "$password"
                                }
                            """.trimIndent().toRequestBody("application/json".toMediaType()),
                        )
                }

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.code).isEqualTo(201)
                    s.assertThat(response.header("Set-Cookie")).isEqualTo(
                        "$AUTH_COOKIE_KEY=$sessionId; Path=/; HttpOnly; SameSite=Strict",
                    )
                    s.assertThat(response.header("Content-Type")).isEqualTo("application/json")
                    s.assertThatJson(response.body?.string()).isEqualTo(
                        """
                            {
                                "user_id": "$userId",
                                "login_id": "$loginId",
                                "user_roles": ["${userRoles.first()}"],
                                "anonymous_user": false,
                                "session_expiry_time": "$sessionExpiryTime"
                            }
                        """.trimIndent(),
                    )
                }
            },
        )
    }

    @Test
    fun `should return 401 when invalid credentials supplied`() = test(javalin) { _, client ->
        `RUN should return 401 when invalid credentials supplied`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/auth/v0/session") {
                    it.header("Content-Type", "application/json")
                        .post(
                            """
                                {
                                    "login_id": "$loginId",
                                    "password": "$password"
                                }
                            """.trimIndent().toRequestBody("application/json".toMediaType()),
                        )
                }

                override fun makeAssertions(s: SoftAssertions) {
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
                                "instance": "/api/auth/v0/session",
                                "extended_details": []
                            }
                        """.trimIndent(),
                    )
                }
            },
        )
    }

    @Test
    fun `should return 400 when request body has invalid fields`() = test(javalin) { _, client ->
        `RUN should return 400 when request body has invalid fields`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/auth/v0/session") {
                    it.header("Content-Type", "application/json")
                        .post(
                            """
                                {
                                    "loginId": "$loginId",
                                    "password": "$password"
                                }
                            """.trimIndent().toRequestBody("application/json".toMediaType()),
                        )
                }

                override fun makeAssertions(s: SoftAssertions) {
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
                                "instance": "/api/auth/v0/session",
                                "extended_details": []
                            }
                        """.trimIndent(),
                    )
                }
            },
        )
    }

    @Test
    fun `should return 400 when request body is malformed`() = test(javalin) { _, client ->
        `RUN should return 400 when request body is malformed`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/auth/v0/session") {
                    it.header("Content-Type", "application/json")
                        .post(
                            """
                                {
                                    "login_id": "$loginId",
                                    "password": "$password
                                }
                            """.trimIndent().toRequestBody("application/json".toMediaType()),
                        )
                }

                override fun makeAssertions(s: SoftAssertions) {
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
                                "instance": "/api/auth/v0/session",
                                "extended_details": []
                            }
                        """.trimIndent(),
                    )
                }
            },
        )
    }
}
