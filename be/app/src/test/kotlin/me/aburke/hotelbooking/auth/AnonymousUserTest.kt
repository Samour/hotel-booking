package me.aburke.hotelbooking.auth

import me.aburke.hotelbooking.TestContext
import me.aburke.hotelbooking.client.readAllUsers
import me.aburke.hotelbooking.createTestContext
import me.aburke.hotelbooking.data.sessionDuration
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.ports.repository.UserRecord
import me.aburke.hotelbooking.rest.client.api.AuthUnstableApi
import me.aburke.hotelbooking.rest.client.model.SessionResponse
import me.aburke.hotelbooking.restTest
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.time.ZoneOffset
import javax.sql.DataSource

class AnonymousUserTest {

    private lateinit var testContext: TestContext
    private lateinit var connection: Connection

    @BeforeEach
    fun init() {
        testContext = createTestContext(populateTestData = false)
        connection = testContext.app.koin.get<DataSource>().connection
    }

    @AfterEach
    fun cleanUp() = connection.close()

    @Test
    fun `should create session for anonymous user`() = testContext.app.restTest { client, _ ->
        val createSessionResponse = AuthUnstableApi(client).createAnonymousSession()

        assertSoftly { s ->
            s.assertThat(createSessionResponse).usingRecursiveComparison()
                .ignoringFields("userId")
                .isEqualTo(
                    SessionResponse().apply {
                        userId = ""
                        loginId = null
                        userRoles = listOf("CUSTOMER")
                        anonymousUser = true
                        sessionExpiryTime = testContext.time.plus(sessionDuration).atOffset(ZoneOffset.UTC)
                    },
                )
        }

        val sessionResponse = AuthUnstableApi(client).fetchAuthState()

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
                    userRoles = setOf(UserRole.CUSTOMER.name),
                    name = "",
                    credential = null,
                ),
            )
        }
    }
}
