package me.aburke.hotelbooking.facade.rest.api.admin.v1.user

import io.javalin.Javalin
import io.javalin.testtools.JavalinTest.test
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.client
import me.aburke.hotelbooking.facade.rest.parseResponse
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.rest.client.api.AdminApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.model.CreateUser201Response
import me.aburke.hotelbooking.rest.client.model.CreateUserRequest
import me.aburke.hotelbooking.rest.client.model.ProblemResponse
import me.aburke.hotelbooking.scenario.user.CreateUserDetails
import me.aburke.hotelbooking.scenario.user.CreateUserResult
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import me.aburke.hotelbooking.rest.client.model.UserRole as UserRoleDto

private const val LOGIN_ID = "login-id"
private const val PASSWORD = "password"
private const val NAME = "name"
private const val USER_ID = "user-id"

private val roles = setOf(UserRole.MANAGE_ROOMS)

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
    fun `should create user`() = test(javalin) { _, _ ->
        every {
            stubs.createUserScenario.run(
                CreateUserDetails(
                    loginId = LOGIN_ID,
                    rawPassword = PASSWORD,
                    name = NAME,
                    userRoles = roles,
                )
            )
        } returns CreateUserResult.Success(
            userId = USER_ID,
        )

        val sessionId = stubs.prepareSession(UserRole.MANAGE_USERS)
        val response = AdminApi(javalin.client(sessionId)).createUserWithHttpInfo(
            CreateUserRequest().also {
                it.loginId = LOGIN_ID
                it.password = PASSWORD
                it.name = NAME
                it.roles = roles.map {
                    UserRoleDto.fromValue(it.name)
                }
            }
        )

        assertSoftly { s ->
            s.assertThat(response.statusCode).isEqualTo(201)
            s.assertThat(response.data).isEqualTo(
                CreateUser201Response().apply {
                    userId = USER_ID
                }
            )
            s.check {
                verify(exactly = 1) {
                    stubs.createUserScenario.run(
                        CreateUserDetails(
                            loginId = LOGIN_ID,
                            rawPassword = PASSWORD,
                            name = NAME,
                            userRoles = roles,
                        )
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    @Test
    fun `should return 409 when username not available`() = test(javalin) { _, _ ->
        every {
            stubs.createUserScenario.run(
                CreateUserDetails(
                    loginId = LOGIN_ID,
                    rawPassword = PASSWORD,
                    name = NAME,
                    userRoles = roles,
                )
            )
        } returns CreateUserResult.UsernameNotAvailable

        val sessionId = stubs.prepareSession(UserRole.MANAGE_USERS)
        val response = assertThrows<ApiException> {
            AdminApi(javalin.client(sessionId)).createUser(
                CreateUserRequest().also {
                    it.loginId = LOGIN_ID
                    it.password = PASSWORD
                    it.name = NAME
                    it.roles = roles.map {
                        UserRoleDto.fromValue(it.name)
                    }
                }
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
                    instance = "/api/admin/v1/user"
                    extendedDetails = emptyList()
                }
            )
            s.check {
                verify(exactly = 1) {
                    stubs.createUserScenario.run(
                        CreateUserDetails(
                            loginId = LOGIN_ID,
                            rawPassword = PASSWORD,
                            name = NAME,
                            userRoles = roles,
                        )
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    @Test
    fun `should return 403 when user does not have MANAGE_USERS role`() = test(javalin) { _, _ ->
        val sessionId = stubs.prepareSession(UserRole.MANAGE_ROOMS)
        val response = assertThrows<ApiException> {
            AdminApi(javalin.client(sessionId)).createUser(
                CreateUserRequest().also {
                    it.loginId = LOGIN_ID
                    it.password = PASSWORD
                    it.name = NAME
                    it.roles = roles.map {
                        UserRoleDto.fromValue(it.name)
                    }
                }
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
                    instance = "/api/admin/v1/user"
                    extendedDetails = emptyList()
                }
            )
            with(stubs) {
                s.verifyStubs()
            }
        }
    }
}
