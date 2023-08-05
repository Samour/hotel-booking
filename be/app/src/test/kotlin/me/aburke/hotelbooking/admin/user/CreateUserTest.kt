package me.aburke.hotelbooking.admin.user

import me.aburke.hotelbooking.*
import me.aburke.hotelbooking.client.parseBody
import me.aburke.hotelbooking.client.readAllUsers
import me.aburke.hotelbooking.data.TestUser
import me.aburke.hotelbooking.facade.rest.api.admin.v1.user.CreateUserRequest
import me.aburke.hotelbooking.facade.rest.api.admin.v1.user.CreateUserResponse
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.ports.repository.UserCredentialRecord
import me.aburke.hotelbooking.ports.repository.UserRecord
import me.aburke.hotelbooking.ports.repository.UserRepository
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.sql.Connection

private const val LOGIN_ID = "login-id"
private const val PASSWORD = "password"
private const val NAME = "name"

private val roles = setOf(UserRole.MANAGE_ROOMS)

class CreateUserTest {

    private lateinit var app: KoinApplication
    private lateinit var passwordHasher: PasswordHasher
    private lateinit var userRepository: UserRepository
    private lateinit var connection: Connection

    @BeforeEach
    fun init() {
        app = createApp().first
        passwordHasher = app.koin.get()
        userRepository = app.koin.get()
        connection = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should create user`() = app.restTest { client ->
        client.authenticateAsAdmin()

        val response = client.createUser(
            CreateUserRequest(
                loginId = LOGIN_ID,
                password = PASSWORD,
                name = NAME,
                roles = roles.toList(),
            )
        )
        val responseBody = response.parseBody<CreateUserResponse>()

        val allUsers = connection.readAllUsers()
        val newUser = allUsers.firstOrNull { it.userId == responseBody?.userId }
        val passwordHashResult = newUser?.credential?.passwordHash?.let {
            passwordHasher.passwordMatches(PASSWORD, it)
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(201)
            s.assertThat(allUsers.map { it.userId }).containsExactlyInAnyOrder(
                TestUser.admin.userId,
                responseBody?.userId,
            )
            s.assertThat(newUser).usingRecursiveComparison()
                .ignoringFields("credential.passwordHash")
                .isEqualTo(
                    UserRecord(
                        userId = responseBody?.userId ?: "",
                        userRoles = roles,
                        name = NAME,
                        credential = UserCredentialRecord(
                            loginId = LOGIN_ID,
                            passwordHash = "",
                        )
                    )
                )
            s.assertThat(passwordHashResult).isTrue
        }
    }

    @Test
    fun `should return 409 when username is not available`() = app.restTest { client ->
        client.authenticateAsAdmin()

        val response = client.createUser(
            CreateUserRequest(
                loginId = TestUser.admin.loginId,
                password = PASSWORD,
                name = NAME,
                roles = roles.toList(),
            )
        )

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(409)
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "title": "Username Conflict",
                        "code": "CONFLICT",
                        "status": 409,
                        "detail": "Username is not available",
                        "instance": "/api/admin/v1/user",
                        "extended_details": []
                    }
                """.trimIndent()
            )
            s.assertThat(allUsers.map { it.userId }).containsExactly(
                TestUser.admin.userId,
            )
        }
    }

    @Test
    fun `should return 403 when user does not have MANAGE_USERS permission`() = app.restTest { client ->
        val reducedUser = client.createUserWithRoles(UserRole.MANAGE_ROOMS)
        client.authenticateAs(reducedUser)

        val response = client.createUser(
            CreateUserRequest(
                loginId = TestUser.admin.loginId,
                password = PASSWORD,
                name = NAME,
                roles = roles.toList(),
            )
        )

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(403)
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "title": "Forbidden",
                        "code": "FORBIDDEN",
                        "status": 403,
                        "detail": "Insufficient permissions to access resource",
                        "instance": "/api/admin/v1/user",
                        "extended_details": []
                    }
                """.trimIndent()
            )
            s.assertThat(allUsers.map { it.userId }).containsExactlyInAnyOrder(
                TestUser.admin.userId,
                reducedUser.userId,
            )
        }
    }
}
