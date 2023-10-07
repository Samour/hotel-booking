package me.aburke.hotelbooking.auth

import me.aburke.hotelbooking.TestContext
import me.aburke.hotelbooking.assertThatJson
import me.aburke.hotelbooking.createApp
import me.aburke.hotelbooking.data.TestUser
import me.aburke.hotelbooking.data.sessionDuration
import me.aburke.hotelbooking.rest.client.api.AuthUnstableApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.model.LogInRequest
import me.aburke.hotelbooking.rest.client.model.SessionResponse
import me.aburke.hotelbooking.restTest
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZoneOffset

class LogInTest {

    private lateinit var testContext: TestContext

    @BeforeEach
    fun init() {
        testContext = createApp()
    }

    @AfterEach
    fun cleanUp() = testContext.app.close()

    @Test
    fun `should log in user & set session cookie on successful authentication`() =
        testContext.app.restTest { client, _ ->
            val logInResponse = AuthUnstableApi(client).logIn(
                LogInRequest().apply {
                    loginId = TestUser.admin.loginId
                    password = TestUser.admin.password
                },
            )

            assertSoftly { s ->
                s.assertThat(logInResponse).isEqualTo(
                    SessionResponse().apply {
                        userId = TestUser.admin.userId
                        loginId = TestUser.admin.loginId
                        userRoles = listOf("MANAGE_USERS")
                        anonymousUser = false
                        sessionExpiryTime = testContext.time.plus(sessionDuration).atOffset(ZoneOffset.UTC)
                    },
                )
            }

            val sessionResponse = AuthUnstableApi(client).fetchAuthState()

            assertSoftly { s ->
                s.assertThat(sessionResponse).isEqualTo(
                    SessionResponse().apply {
                        userId = TestUser.admin.userId
                        loginId = TestUser.admin.loginId
                        userRoles = listOf("MANAGE_USERS")
                        anonymousUser = false
                        sessionExpiryTime = logInResponse.sessionExpiryTime
                    },
                )
            }
        }

    @Test
    fun `should return 401 when password is incorrect`() = testContext.app.restTest { client, _ ->
        val logInException = assertThrows<ApiException> {
            AuthUnstableApi(client).logIn(
                LogInRequest().apply {
                    loginId = TestUser.admin.loginId
                    password = "wrong-password"
                },
            )
        }

        assertSoftly { s ->
            s.assertThat(logInException.code).isEqualTo(401)
            s.assertThat(logInException.responseHeaders["Set-Cookie"]).isNull()
            s.assertThatJson(logInException.responseBody)
                .isEqualTo(
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
    }

    @Test
    fun `should return 401 when user does not exist`() = testContext.app.restTest { client, _ ->
        val logInException = assertThrows<ApiException> {
            AuthUnstableApi(client).logIn(
                LogInRequest().apply {
                    loginId = TestUser.admin.userId
                    password = TestUser.admin.password
                },
            )
        }

        assertSoftly { s ->
            s.assertThat(logInException.code).isEqualTo(401)
            s.assertThat(logInException.responseHeaders["Set-Cookie"]).isNull()
            s.assertThatJson(logInException.responseBody)
                .isEqualTo(
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
    }
}
