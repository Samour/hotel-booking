package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.assertThatJson
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import okhttp3.Response
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

class GetSessionHandlerTest : AbstractGetSessionTest() {

    @Test
    fun `should return session details when authenticated`() = test(javalin) { _, client ->
        `RUN should return session details when authenticated`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.get("/api/auth/v1/session") {
                    it.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                }

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.code).isEqualTo(200)
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
    fun `should return session details when anonymously authenticated`() = test(javalin) { _, client ->
        `RUN should return session details when anonymously authenticated`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.get("/api/auth/v1/session") {
                    it.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                }

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.code).isEqualTo(200)
                    s.assertThat(response.header("Content-Type")).isEqualTo("application/json")
                    s.assertThatJson(response.body?.string()).isEqualTo(
                        """
                            {
                                "user_id": "$userId",
                                "user_roles": ["${userRoles.first()}"],
                                "anonymous_user": true,
                                "session_expiry_time": "$sessionExpiryTime"
                            }
                        """.trimIndent(),
                    )
                }
            },
        )
    }

    @Test
    fun `should return 401 when invalid session ID provided`() = test(javalin) { _, client ->
        `RUN should return 401 when invalid session ID provided`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.get("/api/auth/v1/session") {
                    it.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                }

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.code).isEqualTo(401)
                    s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
                    s.assertThatJson(response.body?.string()).isEqualTo(
                        """
                            {
                                "title": "Not Authorized",
                                "code": "UNAUTHORIZED",
                                "status": 401,
                                "detail": "Credentials not provided",
                                "instance": "/api/auth/v1/session",
                                "extended_details": []
                            }
                        """.trimIndent(),
                    )
                }
            },
        )
    }

    @Test
    fun `should return 401 when no session ID provided`() = test(javalin) { _, client ->
        `RUN should return 401 when no session ID provided`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.get("/api/auth/v1/session")

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.code).isEqualTo(401)
                    s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
                    s.assertThatJson(response.body?.string()).isEqualTo(
                        """
                            {
                                "title": "Not Authorized",
                                "code": "UNAUTHORIZED",
                                "status": 401,
                                "detail": "Credentials not provided",
                                "instance": "/api/auth/v1/session",
                                "extended_details": []
                            }
                        """.trimIndent(),
                    )
                }
            },
        )
    }
}
