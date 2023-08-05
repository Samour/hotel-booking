package me.aburke.hotelbooking.auth

import me.aburke.hotelbooking.client.parseBody
import me.aburke.hotelbooking.createApp
import me.aburke.hotelbooking.facade.rest.api.auth.v1.session.CreateAnonymousSessionResponse
import me.aburke.hotelbooking.facade.rest.api.auth.v1.session.SessionResponse
import me.aburke.hotelbooking.restTest
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.time.Instant

class AnonymousUserTest {

    private lateinit var app: KoinApplication

    @BeforeEach
    fun init() {
        app = createApp()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should create session for anonymous user`() = app.restTest { client ->
        val createSessionResponse = client.createAnonymousSession()
        val createSessionResponseBody = createSessionResponse.parseBody<CreateAnonymousSessionResponse>()

        assertSoftly { s ->
            s.assertThat(createSessionResponse.code).isEqualTo(201)
            s.assertThat(createSessionResponseBody).usingRecursiveComparison()
                .ignoringFields("userId", "sessionExpiryTime")
                .isEqualTo(
                    CreateAnonymousSessionResponse(
                        userId = "",
                        userRoles = listOf("CUSTOMER"),
                        anonymousUser = true,
                        sessionExpiryTime = Instant.EPOCH,
                    )
                )
        }

        val sessionResponse = client.getSession()
        val sessionResponseBody = sessionResponse.parseBody<SessionResponse>()

        assertSoftly { s ->
            s.assertThat(sessionResponse.code).isEqualTo(200)
            s.assertThat(sessionResponseBody).isEqualTo(
                SessionResponse(
                    userId = sessionResponseBody!!.userId,
                    loginId = null,
                    userRoles = listOf("CUSTOMER"),
                    anonymousUser = true,
                    sessionExpiryTime = sessionResponseBody.sessionExpiryTime,
                )
            )
        }
    }
}
