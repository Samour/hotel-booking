package me.aburke.hotelbooking.auth

import me.aburke.hotelbooking.assertThatJson
import me.aburke.hotelbooking.client.readBody
import me.aburke.hotelbooking.createApp
import me.aburke.hotelbooking.data.TestUser
import me.aburke.hotelbooking.facade.rest.api.auth.v1.session.LogInResponse
import me.aburke.hotelbooking.facade.rest.api.auth.v1.session.SessionResponse
import me.aburke.hotelbooking.restTest
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.time.Instant

class LogInTest {

    private lateinit var app: KoinApplication

    @BeforeEach
    fun init() {
        app = createApp()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should log in user & set session cookie on successful authentication`() = app.restTest { client ->
        val logInResponse = client.logIn(TestUser.admin.loginId, TestUser.admin.password)
        val logInResponseBody = logInResponse.readBody<LogInResponse>()

        assertSoftly { s ->
            s.assertThat(logInResponse.code).isEqualTo(201)
            s.assertThat(logInResponseBody).usingRecursiveComparison()
                .ignoringFields("sessionExpiryTime")
                .isEqualTo(
                    LogInResponse(
                        userId = TestUser.admin.userId,
                        loginId = TestUser.admin.loginId,
                        userRoles = listOf("MANAGE_USERS"),
                        anonymousUser = false,
                        sessionExpiryTime = Instant.EPOCH,
                    )
                )
        }

        val sessionResponse = client.getSession()
        val sessionResponseBody = sessionResponse.readBody<SessionResponse>()

        assertSoftly { s ->
            s.assertThat(sessionResponse.code).isEqualTo(200)
            s.assertThat(sessionResponseBody).usingRecursiveComparison()
                .ignoringFields("sessionExpiryTime")
                .isEqualTo(
                    SessionResponse(
                        userId = TestUser.admin.userId,
                        loginId = TestUser.admin.loginId,
                        userRoles = listOf("MANAGE_USERS"),
                        anonymousUser = false,
                        sessionExpiryTime = Instant.EPOCH,
                    )
                )
        }
    }

    @Test
    fun `should return 401 when password is incorrect`() = app.restTest { client ->
        val logInResponse = client.logIn(TestUser.admin.loginId, "wrong-password")

        assertSoftly { s ->
            s.assertThat(logInResponse.code).isEqualTo(401)
            s.assertThat(logInResponse.header("Set-Cookie")).isNull()
            s.assertThatJson(logInResponse.body?.string())
                .isEqualTo(
                    """
                        {
                        "title": "Invalid Credentials",
                        "code": "UNAUTHORIZED",
                        "status": 401,
                        "detail": "Supplied credentials are not valid",
                        "instance": "/api/auth/v1/session",
                        "extended_details": []
                    }
                    """.trimIndent()
                )
        }
    }

    @Test
    fun `should return 401 when user does not exist`() = app.restTest { client ->
        val logInResponse = client.logIn(TestUser.admin.userId, TestUser.admin.password)

        assertSoftly { s ->
            s.assertThat(logInResponse.code).isEqualTo(401)
            s.assertThat(logInResponse.header("Set-Cookie")).isNull()
            s.assertThatJson(logInResponse.body?.string())
                .isEqualTo(
                    """
                        {
                        "title": "Invalid Credentials",
                        "code": "UNAUTHORIZED",
                        "status": 401,
                        "detail": "Supplied credentials are not valid",
                        "instance": "/api/auth/v1/session",
                        "extended_details": []
                    }
                    """.trimIndent()
                )
        }
    }
}
