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

class CreateUserTest {

    private val loginId = "login-id"
    private val password = "password"
    private val name = "name"
    private val userId = "user-id"
    private val roles = setOf(UserRole.MANAGE_ROOMS)

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
            stubs.createUserPort.run(
                CreateUserDetails(
                    loginId = loginId,
                    rawPassword = password,
                    name = name,
                    userRoles = roles,
                ),
            )
        } returns CreateUserResult.Success(
            userId = userId,
        )
        val sessionId = stubs.prepareSession(UserRole.MANAGE_USERS).sessionId

        val response = AdminUnstableApi(client.withSessionId(sessionId)).createUserWithHttpInfo(
            CreateUserRequest().also {
                it.loginId = loginId
                it.password = password
                it.name = name
                it.roles = roles.map {
                    UserRoleDto.fromValue(it.name)
                }
            },
        )

        assertSoftly { s ->
            s.assertThat(response.statusCode).isEqualTo(201)
            s.assertThat(response.data).isEqualTo(
                CreateUser201Response().also {
                    it.userId = userId
                },
            )
            s.check {
                verify(exactly = 1) {
                    stubs.createUserPort.run(
                        CreateUserDetails(
                            loginId = loginId,
                            rawPassword = password,
                            name = name,
                            userRoles = roles,
                        ),
                    )
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
                    loginId = loginId,
                    rawPassword = password,
                    name = name,
                    userRoles = roles,
                ),
            )
        } returns CreateUserResult.UsernameNotAvailable

        val sessionId = stubs.prepareSession(UserRole.MANAGE_USERS).sessionId
        val response = assertThrows<ApiException> {
            AdminUnstableApi(client.withSessionId(sessionId)).createUser(
                CreateUserRequest().also {
                    it.loginId = loginId
                    it.password = password
                    it.name = name
                    it.roles = roles.map {
                        UserRoleDto.fromValue(it.name)
                    }
                },
            )
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
                    stubs.createUserPort.run(
                        CreateUserDetails(
                            loginId = loginId,
                            rawPassword = password,
                            name = name,
                            userRoles = roles,
                        ),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    @Test
    fun `should return 403 when user does not have MANAGE_USERS role`() = snapshotTest(javalin) { client ->
        val sessionId = stubs.prepareSession(UserRole.MANAGE_ROOMS).sessionId
        val response = assertThrows<ApiException> {
            AdminUnstableApi(client.withSessionId(sessionId)).createUser(
                CreateUserRequest().also {
                    it.loginId = loginId
                    it.password = password
                    it.name = name
                    it.roles = roles.map {
                        UserRoleDto.fromValue(it.name)
                    }
                },
            )
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
