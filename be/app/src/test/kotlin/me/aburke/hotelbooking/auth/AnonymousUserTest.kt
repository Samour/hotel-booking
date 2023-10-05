package me.aburke.hotelbooking.auth

import me.aburke.hotelbooking.client.readAllUsers
import me.aburke.hotelbooking.createApp
import me.aburke.hotelbooking.data.sessionDuration
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.ports.repository.UserRecord
import me.aburke.hotelbooking.rest.client.api.AuthApi
import me.aburke.hotelbooking.rest.client.model.SessionResponse
import me.aburke.hotelbooking.restTest
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.sql.Connection
import java.time.Instant
import java.time.ZoneOffset

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
    fun `should create session for anonymous user`() = app.restTest { client, _ ->
        val createSessionResponse = AuthApi(client).createAnonymousSession()

        assertSoftly { s ->
            s.assertThat(createSessionResponse).usingRecursiveComparison()
                .ignoringFields("userId")
                .isEqualTo(
                    SessionResponse().apply {
                        userId = ""
                        loginId = null
                        userRoles = listOf("CUSTOMER")
                        anonymousUser = true
                        sessionExpiryTime = instant.plus(sessionDuration).atOffset(ZoneOffset.UTC)
                    },
                )
        }

        val sessionResponse = AuthApi(client).fetchAuthState()

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(sessionResponse).isEqualTo(
                SessionResponse().apply {
                    userId = createSessionResponse.userId
                    loginId = null
                    userRoles = listOf("CUSTOMER")
                    anonymousUser = true
                    sessionExpiryTime = createSessionResponse.sessionExpiryTime
                },
            )
            s.assertThat(allUsers).containsExactly(
                UserRecord(
                    userId = createSessionResponse.userId,
                    userRoles = setOf(UserRole.CUSTOMER),
                    name = "",
                    credential = null,
                ),
            )
        }
    }
}
