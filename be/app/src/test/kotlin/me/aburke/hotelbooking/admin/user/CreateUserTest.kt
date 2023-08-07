package me.aburke.hotelbooking.admin.user

import me.aburke.hotelbooking.*
import me.aburke.hotelbooking.client.readAllUsers
import me.aburke.hotelbooking.data.TestUser
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.ports.repository.UserCredentialRecord
import me.aburke.hotelbooking.ports.repository.UserRecord
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.rest.client.api.AdminApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.model.CreateUserRequest
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.KoinApplication
import java.sql.Connection
import me.aburke.hotelbooking.rest.client.model.UserRole as UserRoleDto

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
    fun `should create user`() = app.restTest { client, _ ->
        client.authenticateAsAdmin()

        val response = AdminApi(client).createUser(
            CreateUserRequest().also { r ->
                r.loginId = LOGIN_ID
                r.password = PASSWORD
                r.name = NAME
                r.roles = roles.map { UserRoleDto.valueOf(it.name) }
            }
        )

        val allUsers = connection.readAllUsers()
        val newUser = allUsers.firstOrNull { it.userId == response.userId }
        val passwordHashResult = newUser?.credential?.passwordHash?.let {
            passwordHasher.passwordMatches(PASSWORD, it)
        }

        assertSoftly { s ->
            s.assertThat(allUsers.map { it.userId }).containsExactlyInAnyOrder(
                TestUser.admin.userId,
                response.userId,
            )
            s.assertThat(newUser).usingRecursiveComparison()
                .ignoringFields("credential.passwordHash")
                .isEqualTo(
                    UserRecord(
                        userId = response.userId,
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
    fun `should return 409 when username is not available`() = app.restTest { client, _ ->
        client.authenticateAsAdmin()

        val response = assertThrows<ApiException> {
            AdminApi(client).createUser(
                CreateUserRequest().also { r ->
                    r.loginId = TestUser.admin.loginId
                    r.password = PASSWORD
                    r.name = NAME
                    r.roles = roles.map { UserRoleDto.valueOf(it.name) }
                }
            )
        }

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(409)
            s.assertThatJson(response.responseBody).isEqualTo(
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
    fun `should return 403 when user does not have MANAGE_USERS permission`() = app.restTest { client, _ ->
        val reducedUser = client.createUserWithRoles(UserRole.MANAGE_ROOMS)
        client.authenticateAs(reducedUser)

        val response = assertThrows<ApiException> {
            AdminApi(client).createUser(
                CreateUserRequest().also { r ->
                    r.loginId = LOGIN_ID
                    r.password = PASSWORD
                    r.name = NAME
                    r.roles = roles.map { UserRoleDto.valueOf(it.name) }
                }
            )
        }

        val allUsers = connection.readAllUsers()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(403)
            s.assertThatJson(response.responseBody).isEqualTo(
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
