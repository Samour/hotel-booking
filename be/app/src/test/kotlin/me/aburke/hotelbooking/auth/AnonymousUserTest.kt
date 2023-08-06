package me.aburke.hotelbooking.auth

import me.aburke.hotelbooking.client.parseBody
import me.aburke.hotelbooking.client.readAllUsers
import me.aburke.hotelbooking.createApp
import me.aburke.hotelbooking.data.sessionDuration
import me.aburke.hotelbooking.facade.rest.responses.SessionResponse
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.ports.repository.UserRecord
import me.aburke.hotelbooking.restTest
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.sql.Connection
import java.time.Instant

class AnonymousUserTest {

    private lateinit var app: KoinApplication
    private lateinit var instant: Instant
    private lateinit var connection: Connection

    @BeforeEach
    fun init() {
        createApp(populateTestData = false).let {
            app = it.first
            instant = it.second
        }
        connection = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should create session for anonymous user`() = app.restTest { client ->
        val createSessionResponse = client.createAnonymousSession()
        val createSessionResponseBody = createSessionResponse.parseBody<SessionResponse>()

        assertSoftly { s ->
            s.assertThat(createSessionResponse.code).isEqualTo(201)
            s.assertThat(createSessionResponseBody).usingRecursiveComparison()
                .ignoringFields("userId")
                .isEqualTo(
                    SessionResponse(
                        userId = "",
                        loginId = null,
                        userRoles = listOf("CUSTOMER"),
                        anonymousUser = true,
                        sessionExpiryTime = instant.plus(sessionDuration),
                    )
                )
        }

        val sessionResponse = client.getSession()
        val sessionResponseBody = sessionResponse.parseBody<SessionResponse>()

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(sessionResponse.code).isEqualTo(200)
            s.assertThat(sessionResponseBody).isEqualTo(
                SessionResponse(
                    userId = createSessionResponseBody!!.userId,
                    loginId = null,
                    userRoles = listOf("CUSTOMER"),
                    anonymousUser = true,
                    sessionExpiryTime = createSessionResponseBody.sessionExpiryTime,
                )
            )
            s.assertThat(allUsers).containsExactly(
                UserRecord(
                    userId = createSessionResponseBody.userId,
                    userRoles = setOf(UserRole.CUSTOMER),
                    name = "",
                    credential = null,
                )
            )
        }
    }
}
