package me.aburke.hotelbooking.facade.rest.api.admin.user

import io.javalin.Javalin
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.parseResponse
import me.aburke.hotelbooking.facade.rest.snapshot.snapshotTest
import me.aburke.hotelbooking.facade.rest.withSessionId
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.ports.scenario.user.CreateUserDetails
import me.aburke.hotelbooking.ports.scenario.user.CreateUserResult
import me.aburke.hotelbooking.rest.client.api.AdminUnstableApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.model.CreateUser201Response
import me.aburke.hotelbooking.rest.client.model.CreateUserRequest
import me.aburke.hotelbooking.rest.client.model.ProblemResponse
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import me.aburke.hotelbooking.rest.client.model.UserRole as UserRoleDto

private const val LOGIN_ID = "login-id"
private const val PASSWORD = "password"
private const val NAME = "name"
private const val CREATED_USER_ID = "user-id"
private val roles = setOf(UserRole.MANAGE_ROOMS)

private val createUserDetails = CreateUserDetails(
    loginId = LOGIN_ID,
    rawPassword = PASSWORD,
    name = NAME,
    userRoles = roles,
)
private val createUserRequest = CreateUserRequest().also { req ->
    req.loginId = LOGIN_ID
    req.password = PASSWORD
    req.name = NAME
    req.roles = roles.map {
        UserRoleDto.fromValue(it.name)
    }
}

class CreateUserTest {

    private val stubs = Stubs()

    private lateinit var javalin: Javalin

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    @Test
    fun `should create user`() = snapshotTest(javalin) { client ->
        every {
            stubs.createUserPort.run(createUserDetails)
        } returns CreateUserResult.Success(
            userId = CREATED_USER_ID,
        )
        stubs.prepareSession(UserRole.MANAGE_USERS)

        val response = AdminUnstableApi(client.withSessionId())
            .createUserWithHttpInfo(createUserRequest)

        assertSoftly { s ->
            s.assertThat(response.statusCode).isEqualTo(201)
            s.assertThat(response.data).isEqualTo(
                CreateUser201Response().also {
                    it.userId = CREATED_USER_ID
                },
            )
            s.check {
                verify(exactly = 1) {
                    stubs.createUserPort.run(createUserDetails)
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    @Test
    fun `should return 409 when username not available`() = snapshotTest(javalin) { client ->
        every {
            stubs.createUserPort.run(
                CreateUserDetails(
                    loginId = LOGIN_ID,
                    rawPassword = PASSWORD,
                    name = NAME,
                    userRoles = roles,
                ),
            )
        } returns CreateUserResult.UsernameNotAvailable

        stubs.prepareSession(UserRole.MANAGE_USERS)
        val response = assertThrows<ApiException> {
            AdminUnstableApi(client.withSessionId()).createUser(createUserRequest)
        }

        val responseBody = response.responseBody.parseResponse<ProblemResponse>()
        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(409)
            s.assertThat(response.responseHeaders["Content-Type"])
                .containsExactly("application/problem+json;charset=utf-8")
            s.assertThat(responseBody).isEqualTo(
                ProblemResponse().apply {
                    title = "Username Conflict"
                    code = "CONFLICT"
                    status = 409
                    detail = "Username is not available"
                    instance = "/api/admin/v0/user"
                    extendedDetails = emptyList()
                },
            )
            s.check {
                verify(exactly = 1) {
                    stubs.createUserPort.run(createUserDetails)
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    @Test
    fun `should return 403 when user does not have MANAGE_USERS role`() = snapshotTest(javalin) { client ->
        stubs.prepareSession(UserRole.MANAGE_ROOMS)
        val response = assertThrows<ApiException> {
            AdminUnstableApi(client.withSessionId()).createUser(createUserRequest)
        }

        val responseBody = response.responseBody.parseResponse<ProblemResponse>()
        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(403)
            s.assertThat(response.responseHeaders["Content-Type"])
                .containsExactly("application/problem+json;charset=utf-8")
            s.assertThat(responseBody).isEqualTo(
                ProblemResponse().apply {
                    title = "Forbidden"
                    code = "FORBIDDEN"
                    status = 403
                    detail = "Insufficient permissions to access resource"
                    instance = "/api/admin/v0/user"
                    extendedDetails = emptyList()
                },
            )
        }
    }
}
