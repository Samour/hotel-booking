package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import me.aburke.hotelbooking.facade.rest.client
import me.aburke.hotelbooking.rest.client.api.AuthApi
import me.aburke.hotelbooking.rest.client.invoker.ApiResponse
import me.aburke.hotelbooking.rest.client.model.SessionResponse
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import java.time.ZoneOffset

class CreateAnonymousSessionTest : AbstractCreateAnonymousSessionTest() {

    @Test
    fun `should create anonymous user and set session cookie`() = test(javalin) { _, _ ->
        `RUN should create anonymous user and set session cookie`(
            object : TestRequest<ApiResponse<SessionResponse>>() {
                override fun makeRequest(): ApiResponse<SessionResponse> =
                    AuthApi(javalin.client()).createAnonymousSessionWithHttpInfo()

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.statusCode).isEqualTo(201)
                    s.assertThat(response.headers["Set-Cookie"]).containsExactly(
                        "$AUTH_COOKIE_KEY=$sessionId; Path=/; HttpOnly; SameSite=Strict"
                    )
                    s.assertThat(response.data).isEqualTo(
                        SessionResponse().also {
                            it.userId = userId
                            it.userRoles = listOf("CUSTOMER")
                            it.anonymousUser = true
                            it.sessionExpiryTime = sessionExpiryTime.atOffset(ZoneOffset.UTC)
                        }
                    )
                }
            }
        )
    }
}
