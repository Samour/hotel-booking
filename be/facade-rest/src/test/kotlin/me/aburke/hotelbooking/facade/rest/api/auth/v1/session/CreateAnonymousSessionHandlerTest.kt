package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.assertThatJson
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import okhttp3.Response
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

class CreateAnonymousSessionHandlerTest : AbstractCreateAnonymousSessionTest() {

    @Test
    fun `should create anonymous user and set session cookie`() = test(javalin) { _, client ->
        `RUN should create anonymous user and set session cookie`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.post("/api/auth/v1/session/anonymous")

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
                                "user_roles": ["CUSTOMER"],
                                "anonymous_user": true,
                                "session_expiry_time": "$sessionExpiryTime"
                            }
                        """.trimIndent(),
                    )
                }
            },
        )
    }
}
