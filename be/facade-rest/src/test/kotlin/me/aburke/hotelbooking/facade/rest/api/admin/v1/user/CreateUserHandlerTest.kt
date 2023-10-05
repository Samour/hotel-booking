package me.aburke.hotelbooking.facade.rest.api.admin.v1.user

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.assertThatJson
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

class CreateUserHandlerTest : AbstractCreateUserTest() {

    @Test
    fun `should create user`() = test(javalin) { _, client ->
        `RUN should create user`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/admin/v1/user") {
                    it.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                        .header("Content-Type", "application/json")
                        .post(
                            """
                                {
                                    "login_id": "$loginId",
                                    "password": "$password",
                                    "name": "$name",
                                    "roles": ["${roles.first().name}"]
                                }
                            """.trimIndent().toRequestBody("application/json".toMediaType()),
                        )
                }

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.code).isEqualTo(201)
                    s.assertThat(response.header("Content-Type")).isEqualTo("application/json")
                    s.assertThatJson(response.body?.string()).isEqualTo(
                        """
                            {
                                "user_id": "$userId"
                            }
                        """.trimIndent(),
                    )
                }
            },
        )
    }

    @Test
    fun `should return 409 when username not available`() = test(javalin) { _, client ->
        `RUN should return 409 when username not available`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/admin/v1/user") {
                    it.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                        .header("Content-Type", "application/json")
                        .post(
                            """
                                {
                                    "login_id": "$loginId",
                                    "password": "$password",
                                    "name": "$name",
                                    "roles": ["${roles.first().name}"]
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
                                "instance": "/api/admin/v1/user",
                                "extended_details": []
                            }
                        """.trimIndent(),
                    )
                }
            },
        )
    }

    @Test
    fun `should return 403 when user does not have MANAGE_USERS role`() = test(javalin) { _, client ->
        `RUN should return 403 when user does not have MANAGE_USERS role`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/admin/v1/user") {
                    it.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                        .header("Content-Type", "application/json")
                        .post(
                            """
                                {
                                    "login_id": "$loginId",
                                    "password": "$password",
                                    "name": "$name",
                                    "roles": ["${roles.first().name}"]
                                }
                            """.trimIndent().toRequestBody("application/json".toMediaType()),
                        )
                }

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.code).isEqualTo(403)
                    s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
                    s.assertThatJson(response.body?.string()).isEqualTo(
                        """
                            {
                                "title": "Forbidden",
                                "code": "FORBIDDEN",
                                "status": 403,
                                "detail": "Insufficient permissions to access resource",
                                "instance": "/api/admin/v1/user",
                                "extended_details": []
                            }
                        """.trimIndent(),
                    )
                }
            },
        )
    }
}
