package me.aburke.hotelbooking.facade.rest.api.auth.v1.user

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.assertThatJson
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

class SignUpHandlerTest : AbstractSignUpTest() {

    @Test
    fun `should create user and set session cookie`() = test(javalin) { _, client ->
        `RUN should create user and set session cookie`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/auth/v1/user") {
                    it.header("Content-Type", "application/json")
                        .post(
                            """
                                {
                                    "login_id": "$loginId",
                                    "password": "$password",
                                    "name": "$name"
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
                                "user_roles": ["${roles.first()}"],
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
    fun `should return 401 when provided session ID is not valid`() = test(javalin) { _, client ->
        `RUN should return 401 when provided session ID is not valid`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/auth/v1/user") {
                    it.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                        .header("Content-Type", "application/json")
                        .post(
                            """
                                {
                                    "login_id": "$loginId",
                                    "password": "$password",
                                    "name": "$name"
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
                                "title": "Not Authorized",
                                "code": "UNAUTHORIZED",
                                "status": 401,
                                "detail": "Credentials not provided",
                                "instance": "/api/auth/v1/user",
                                "extended_details": []
                            }
                        """.trimIndent(),
                    )
                }
            },
        )
    }

    @Test
    fun `should return 409 when username is not available`() = test(javalin) { _, client ->
        `RUN should return 409 when username is not available`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/auth/v1/user") {
                    it.header("Content-Type", "application/json")
                        .post(
                            """
                                {
                                    "login_id": "$loginId",
                                    "password": "$password",
                                    "name": "$name"
                                }
                            """.trimIndent().toRequestBody("application/json".toMediaType()),
                        )
                }

                override fun makeAssertions(s: SoftAssertions) {
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
                        """.trimIndent(),
                    )
                }
            },
        )
    }

    @Test
    fun `should create credentials for anonymous user`() = test(javalin) { _, client ->
        `RUN should create credentials for anonymous user`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/auth/v1/user") {
                    it.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                        .header("Content-Type", "application/json")
                        .post(
                            """
                                {
                                    "login_id": "$loginId",
                                    "password": "$password",
                                    "name": "$name"
                                }
                            """.trimIndent().toRequestBody("application/json".toMediaType()),
                        )
                }

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.code).isEqualTo(201)
                    s.assertThat(response.header("Set-Cookie")).isNull()
                    s.assertThat(response.header("Content-Type")).isEqualTo("application/json")
                    s.assertThatJson(response.body?.string()).isEqualTo(
                        """
                            {
                                "user_id": "$userId",
                                "login_id": "$loginId",
                                "user_roles": ["${roles.first()}"],
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
    fun `should return 409 when username is not available for anonymous user`() = test(javalin) { _, client ->
        `RUN should return 409 when username is not available for anonymous user`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/auth/v1/user") {
                    it.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                        .header("Content-Type", "application/json")
                        .post(
                            """
                                {
                                    "login_id": "$loginId",
                                    "password": "$password",
                                    "name": "$name"
                                }
                            """.trimIndent().toRequestBody("application/json".toMediaType()),
                        )
                }

                override fun makeAssertions(s: SoftAssertions) {
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
                        """.trimIndent(),
                    )
                }
            },
        )
    }

    @Test
    fun `should return 409 when current user is not anonymous`() = test(javalin) { _, client ->
        `RUN should return 409 when current user is not anonymous`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/auth/v1/user") {
                    it.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                        .header("Content-Type", "application/json")
                        .post(
                            """
                                {
                                    "login_id": "$loginId",
                                    "password": "$password",
                                    "name": "$name"
                                }
                            """.trimIndent().toRequestBody("application/json".toMediaType()),
                        )
                }

                override fun makeAssertions(s: SoftAssertions) {
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
                        """.trimIndent(),
                    )
                }
            },
        )
    }

    @Test
    fun `should return 400 when current user does not exist`() = test(javalin) { _, client ->
        `RUN should return 400 when current user does not exist`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/auth/v1/user") {
                    it.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                        .header("Content-Type", "application/json")
                        .post(
                            """
                                {
                                    "login_id": "$loginId",
                                    "password": "$password",
                                    "name": "$name"
                                }
                            """.trimIndent().toRequestBody("application/json".toMediaType()),
                        )
                }

                override fun makeAssertions(s: SoftAssertions) {
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
                        """.trimIndent(),
                    )
                }
            },
        )
    }
}
