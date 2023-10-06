package me.aburke.hotelbooking.auth

import me.aburke.hotelbooking.assertThatJson
import me.aburke.hotelbooking.client.readAllUsers
import me.aburke.hotelbooking.createApp
import me.aburke.hotelbooking.data.sessionDuration
import me.aburke.hotelbooking.migrations.postgres.executeScript
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.ports.repository.InsertUserResult
import me.aburke.hotelbooking.ports.repository.UserCredentialRecord
import me.aburke.hotelbooking.ports.repository.UserRecord
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.rest.client.api.AuthUnstableApi
import me.aburke.hotelbooking.rest.client.invoker.ApiClient
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.model.LogInRequest
import me.aburke.hotelbooking.rest.client.model.SessionResponse
import me.aburke.hotelbooking.rest.client.model.SignUpRequest
import me.aburke.hotelbooking.restTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.KoinApplication
import java.sql.Connection
import java.time.Instant
import java.time.ZoneOffset

private const val LOGIN_ID = "login-id"
private const val PASSWORD = "password"
private const val NAME = "name"

class SignUpTest {

    private lateinit var app: KoinApplication
    private lateinit var instant: Instant
    private lateinit var userRepository: UserRepository
    private lateinit var connection: Connection

    @BeforeEach
    fun init() {
        createApp(populateTestData = false).let {
            app = it.first
            instant = it.second
        }
        connection = app.koin.get()
        userRepository = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should create user and set session cookie`() = app.restTest { client, cookieJar ->
        val signUpResponse = signUp(client)
        client.verifySession(signUpResponse.userId, signUpResponse.sessionExpiryTime.toInstant())

        cookieJar.clearAllCookies()
        val logInResponse = logIn(client, signUpResponse.userId)
        client.verifySession(signUpResponse.userId, logInResponse.sessionExpiryTime.toInstant())
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `should return 409 when username is not available`(asAnonymousUser: Boolean) = app.restTest { client, _ ->
        val (existingUserId) = userRepository.insertUser(
            InsertUserRecord(
                loginId = LOGIN_ID,
                passwordHash = "password-hash",
                name = NAME,
                roles = setOf(UserRole.MANAGE_USERS.name),
            ),
        ) as InsertUserResult.UserInserted

        val anonymousUserId = client.takeIf { asAnonymousUser }
            ?.let {
                AuthUnstableApi(it).createAnonymousSession()
                    .userId
            }

        val response = assertThrows<ApiException> {
            AuthUnstableApi(client).signUp(
                SignUpRequest().apply {
                    loginId = LOGIN_ID
                    password = PASSWORD
                    name = NAME
                },
            )
        }

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(409)
            s.assertThat(response.responseHeaders["Set-Cookie"]).isNull()
            s.assertThatJson(response.responseBody).isEqualTo(
                """
                    {
                        "title": "Username Conflict",
                        "code": "CONFLICT",
                        "status": 409,
                        "detail": "Username is not available",
                        "instance": "/api/auth/v0/user",
                        "extended_details": []
                    }
                """.trimIndent(),
            )
            s.assertThat(allUsers.map { it.userId }).isEqualTo(
                listOfNotNull(
                    existingUserId,
                    anonymousUserId,
                ),
            )
        }
    }

    @Test
    fun `should create credentials for anonymous user`() = app.restTest { client, cookieJar ->
        val anonymousUserId = AuthUnstableApi(client).createAnonymousSession()
            .userId

        val signUpResponse = signUpWithAnonymous(client, anonymousUserId)
        client.verifySession(anonymousUserId, signUpResponse.sessionExpiryTime.toInstant())

        cookieJar.clearAllCookies()
        val logInResponse = logIn(client, signUpResponse.userId)
        client.verifySession(anonymousUserId, logInResponse.sessionExpiryTime.toInstant())
    }

    @Test
    fun `should return 409 when current user is not anonymous`() = app.restTest { client, _ ->
        val firstSignUpResponse = AuthUnstableApi(client).signUp(
            SignUpRequest().apply {
                loginId = LOGIN_ID
                password = PASSWORD
                name = NAME
            },
        )

        val secondSignUpResponse = assertThrows<ApiException> {
            AuthUnstableApi(client).signUp(
                SignUpRequest().apply {
                    loginId = LOGIN_ID
                    password = PASSWORD
                    name = NAME
                },
            )
        }

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(secondSignUpResponse.code).isEqualTo(409)
            s.assertThat(secondSignUpResponse.responseHeaders["Set-Cookie"]).isNull()
            s.assertThatJson(secondSignUpResponse.responseBody).isEqualTo(
                """
                    {
                        "title": "User is not anonymous",
                        "code": "CONFLICT",
                        "status": 409,
                        "detail": "User is not anonymous",
                        "instance": "/api/auth/v0/user",
                        "extended_details": []
                    }
                """.trimIndent(),
            )
            s.assertThat(allUsers.map { it.userId }).containsExactly(firstSignUpResponse.userId)
        }
    }

    @Test
    fun `should return 400 when current user does not exist`() = app.restTest { client, _ ->
        AuthUnstableApi(client).createAnonymousSession()
        connection.executeScript("clear_db.sql")

        val secondSignUpResponse = assertThrows<ApiException> {
            AuthUnstableApi(client).signUp(
                SignUpRequest().apply {
                    loginId = LOGIN_ID
                    password = PASSWORD
                    name = NAME
                },
            )
        }

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(secondSignUpResponse.code).isEqualTo(400)
            s.assertThat(secondSignUpResponse.responseHeaders["Set-Cookie"]).isNull()
            s.assertThatJson(secondSignUpResponse.responseBody).isEqualTo(
                """
                    {
                        "title": "User does not exist",
                        "code": "BAD_REQUEST",
                        "status": 400,
                        "detail": "User does not exist",
                        "instance": "/api/auth/v0/user",
                        "extended_details": []
                    }
                """.trimIndent(),
            )
            s.assertThat(allUsers).isEmpty()
        }
    }

    private fun signUp(client: ApiClient): SessionResponse {
        val response = AuthUnstableApi(client).signUp(
            SignUpRequest().apply {
                loginId = LOGIN_ID
                password = PASSWORD
                name = NAME
            },
        )

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(response).usingRecursiveComparison()
                .ignoringFields("userId")
                .isEqualTo(
                    SessionResponse().apply {
                        userId = ""
                        loginId = LOGIN_ID
                        userRoles = listOf("CUSTOMER")
                        anonymousUser = false
                        sessionExpiryTime = instant.plus(sessionDuration).atOffset(ZoneOffset.UTC)
                    },
                )
            s.assertThat(allUsers).hasSize(1)
            s.assertThat(allUsers.firstOrNull()).usingRecursiveComparison()
                .ignoringFields("credential.passwordHash")
                .isEqualTo(
                    UserRecord(
                        userId = response.userId,
                        userRoles = setOf(UserRole.CUSTOMER.name),
                        name = NAME,
                        credential = UserCredentialRecord(
                            loginId = LOGIN_ID,
                            passwordHash = "",
                        ),
                    ),
                )
        }

        return response
    }

    private fun signUpWithAnonymous(client: ApiClient, anonymousUserId: String): SessionResponse {
        val response = AuthUnstableApi(client).signUp(
            SignUpRequest().apply {
                loginId = LOGIN_ID
                password = PASSWORD
                name = NAME
            },
        )

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(response).isEqualTo(
                SessionResponse().apply {
                    userId = anonymousUserId
                    loginId = LOGIN_ID
                    userRoles = listOf("CUSTOMER")
                    anonymousUser = false
                    sessionExpiryTime = instant.plus(sessionDuration).atOffset(ZoneOffset.UTC)
                },
            )
            s.assertThat(allUsers).hasSize(1)
            s.assertThat(allUsers.firstOrNull()).usingRecursiveComparison()
                .ignoringFields("credential.passwordHash")
                .isEqualTo(
                    UserRecord(
                        userId = anonymousUserId,
                        userRoles = setOf(UserRole.CUSTOMER.name),
                        name = NAME,
                        credential = UserCredentialRecord(
                            loginId = LOGIN_ID,
                            passwordHash = "",
                        ),
                    ),
                )
        }

        return response
    }

    private fun logIn(client: ApiClient, userId: String): SessionResponse {
        val response = AuthUnstableApi(client).logIn(
            LogInRequest().apply {
                loginId = LOGIN_ID
                password = PASSWORD
            },
        )

        assertSoftly { s ->
            s.assertThat(response).isEqualTo(
                SessionResponse().also {
                    it.userId = userId
                    it.loginId = LOGIN_ID
                    it.userRoles = listOf("CUSTOMER")
                    it.anonymousUser = false
                    it.sessionExpiryTime = instant.plus(sessionDuration).atOffset(ZoneOffset.UTC)
                },
            )
        }

        return response
    }

    private fun ApiClient.verifySession(userId: String, sessionExpiryTime: Instant) {
        val response = AuthUnstableApi(this).fetchAuthState()
        assertThat(response).isEqualTo(
            SessionResponse().also {
                it.userId = userId
                it.loginId = LOGIN_ID
                it.userRoles = listOf("CUSTOMER")
                it.anonymousUser = false
                it.sessionExpiryTime = sessionExpiryTime.atOffset(ZoneOffset.UTC)
            },
        )
    }
}
