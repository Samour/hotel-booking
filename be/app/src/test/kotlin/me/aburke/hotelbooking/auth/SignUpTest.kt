package me.aburke.hotelbooking.auth

import me.aburke.hotelbooking.assertThatJson
import me.aburke.hotelbooking.client.AppTestClient
import me.aburke.hotelbooking.client.parseBody
import me.aburke.hotelbooking.client.readAllUsers
import me.aburke.hotelbooking.createApp
import me.aburke.hotelbooking.data.sessionDuration
import me.aburke.hotelbooking.facade.rest.api.auth.v1.session.CreateAnonymousSessionResponse
import me.aburke.hotelbooking.facade.rest.api.auth.v1.session.LogInRequest
import me.aburke.hotelbooking.facade.rest.api.auth.v1.user.SignUpRequest
import me.aburke.hotelbooking.facade.rest.api.auth.v1.user.SignUpResponse
import me.aburke.hotelbooking.facade.rest.responses.SessionResponse
import me.aburke.hotelbooking.migrations.postgres.executeScript
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.ports.repository.*
import me.aburke.hotelbooking.restTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.KoinApplication
import java.sql.Connection
import java.time.Instant

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
    fun `should create user and set session cookie`() = app.restTest { client ->
        val signUpResponse = signUp(client)
        client.verifySession(signUpResponse.userId, signUpResponse.sessionExpiryTime)

        client.clearSession()
        val logInResponse = logIn(client, signUpResponse.userId)
        client.verifySession(signUpResponse.userId, logInResponse.sessionExpiryTime)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `should return 409 when username is not available`(asAnonymousUser: Boolean) = app.restTest { client ->
        val (existingUserId) = userRepository.insertUser(
            InsertUserRecord(
                loginId = LOGIN_ID,
                passwordHash = "password-hash",
                name = NAME,
                roles = setOf(UserRole.MANAGE_USERS),
            )
        ) as InsertUserResult.UserInserted

        val anonymousUserId = client.takeIf { asAnonymousUser }
            ?.let {
                it.createAnonymousSession()
                    .parseBody<CreateAnonymousSessionResponse>()!!
                    .userId
            }

        val response = client.signUp(
            SignUpRequest(
                loginId = LOGIN_ID,
                password = PASSWORD,
                name = NAME,
            )
        )

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(409)
            s.assertThat(response.header("Set-Cookie")).isNull()
            s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "title": "Username Conflict",
                        "code": "CONFLICT",
                        "status": 409,
                        "detail": "Username is not available",
                        "instance": "/api/auth/v1/user",
                        "extended_details": []
                    }
                """.trimIndent()
            )
            s.assertThat(allUsers.map { it.userId }).isEqualTo(
                listOfNotNull(
                    existingUserId,
                    anonymousUserId,
                )
            )
        }
    }

    @Test
    fun `should create credentials for anonymous user`() = app.restTest { client ->
        val anonymousUserId = client.createAnonymousSession()
            .parseBody<CreateAnonymousSessionResponse>()!!
            .userId

        val signUpResponse = signUpWithAnonymous(client, anonymousUserId)
        client.verifySession(anonymousUserId, signUpResponse.sessionExpiryTime)

        client.clearSession()
        val logInResponse = logIn(client, signUpResponse.userId)
        client.verifySession(anonymousUserId, logInResponse.sessionExpiryTime)
    }

    @Test
    fun `should return 409 when current user is not anonymous`() = app.restTest { client ->
        val firstSignUpResponse = client.signUp(
            SignUpRequest(
                loginId = LOGIN_ID,
                password = PASSWORD,
                name = NAME,
            )
        ).parseBody<SignUpResponse>()!!

        val secondSignUpResponse = client.signUp(
            SignUpRequest(
                loginId = LOGIN_ID,
                password = PASSWORD,
                name = NAME,
            )
        )

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(secondSignUpResponse.code).isEqualTo(409)
            s.assertThat(secondSignUpResponse.header("Set-Cookie")).isNull()
            s.assertThat(secondSignUpResponse.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
            s.assertThatJson(secondSignUpResponse.body?.string()).isEqualTo(
                """
                    {
                        "title": "User is not anonymous",
                        "code": "CONFLICT",
                        "status": 409,
                        "detail": "User is not anonymous",
                        "instance": "/api/auth/v1/user",
                        "extended_details": []
                    }
                """.trimIndent()
            )
            s.assertThat(allUsers.map { it.userId }).containsExactly(firstSignUpResponse.userId)
        }
    }

    @Test
    fun `should return 400 when current user does not exist`() = app.restTest { client ->
        client.createAnonymousSession()
        connection.executeScript("clear_db.sql")

        val secondSignUpResponse = client.signUp(
            SignUpRequest(
                loginId = LOGIN_ID,
                password = PASSWORD,
                name = NAME,
            )
        )

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(secondSignUpResponse.code).isEqualTo(400)
            s.assertThat(secondSignUpResponse.header("Set-Cookie")).isNull()
            s.assertThat(secondSignUpResponse.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
            s.assertThatJson(secondSignUpResponse.body?.string()).isEqualTo(
                """
                    {
                        "title": "User does not exist",
                        "code": "BAD_REQUEST",
                        "status": 400,
                        "detail": "User does not exist",
                        "instance": "/api/auth/v1/user",
                        "extended_details": []
                    }
                """.trimIndent()
            )
            s.assertThat(allUsers).isEmpty()
        }
    }

    private fun signUp(client: AppTestClient): SignUpResponse {
        val response = client.signUp(
            SignUpRequest(
                loginId = LOGIN_ID,
                password = PASSWORD,
                name = NAME,
            )
        )
        val responseBody = response.parseBody<SignUpResponse>()

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(201)
            s.assertThat(responseBody).usingRecursiveComparison()
                .ignoringFields("userId")
                .isEqualTo(
                    SignUpResponse(
                        userId = "",
                        loginId = LOGIN_ID,
                        userRoles = listOf("CUSTOMER"),
                        anonymousUser = false,
                        sessionExpiryTime = instant.plus(sessionDuration),
                    )
                )
            s.assertThat(allUsers).hasSize(1)
            s.assertThat(allUsers.firstOrNull()).usingRecursiveComparison()
                .ignoringFields("credential.passwordHash")
                .isEqualTo(
                    UserRecord(
                        userId = responseBody?.userId ?: "",
                        userRoles = setOf(UserRole.CUSTOMER),
                        name = NAME,
                        credential = UserCredentialRecord(
                            loginId = LOGIN_ID,
                            passwordHash = "",
                        ),
                    )
                )
        }

        return responseBody!!
    }

    private fun signUpWithAnonymous(client: AppTestClient, anonymousUserId: String): SignUpResponse {
        val response = client.signUp(
            SignUpRequest(
                loginId = LOGIN_ID,
                password = PASSWORD,
                name = NAME,
            )
        )
        val responseBody = response.parseBody<SignUpResponse>()

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(201)
            s.assertThat(response.header("Set-Cookie")).isNull()
            s.assertThat(responseBody).isEqualTo(
                    SignUpResponse(
                        userId = anonymousUserId,
                        loginId = LOGIN_ID,
                        userRoles = listOf("CUSTOMER"),
                        anonymousUser = false,
                        sessionExpiryTime = instant.plus(sessionDuration),
                    )
                )
            s.assertThat(allUsers).hasSize(1)
            s.assertThat(allUsers.firstOrNull()).usingRecursiveComparison()
                .ignoringFields("credential.passwordHash")
                .isEqualTo(
                    UserRecord(
                        userId = anonymousUserId,
                        userRoles = setOf(UserRole.CUSTOMER),
                        name = NAME,
                        credential = UserCredentialRecord(
                            loginId = LOGIN_ID,
                            passwordHash = "",
                        ),
                    )
                )
        }

        return responseBody!!
    }

    private fun logIn(client: AppTestClient, userId: String): SessionResponse {
        val response = client.logIn(
            LogInRequest(
                loginId = LOGIN_ID,
                password = PASSWORD,
            )
        )
        val responseBody = response.parseBody<SessionResponse>()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(201)
            s.assertThat(responseBody).isEqualTo(
                SessionResponse(
                        userId = userId,
                        loginId = LOGIN_ID,
                        userRoles = listOf("CUSTOMER"),
                        anonymousUser = false,
                        sessionExpiryTime = instant.plus(sessionDuration),
                    )
                )
        }

        return responseBody!!
    }

    private fun AppTestClient.verifySession(userId: String, sessionExpiryTime: Instant) {
        val response = getSession().parseBody<SessionResponse>()
        assertThat(response).isEqualTo(
            SessionResponse(
                userId = userId,
                loginId = LOGIN_ID,
                userRoles = listOf("CUSTOMER"),
                anonymousUser = false,
                sessionExpiryTime = sessionExpiryTime,
            )
        )
    }
}
