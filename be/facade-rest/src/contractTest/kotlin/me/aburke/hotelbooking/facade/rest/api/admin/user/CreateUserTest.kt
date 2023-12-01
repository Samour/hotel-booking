package me.aburke.hotelbooking.facade.rest.api.admin.user

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.client
import me.aburke.hotelbooking.facade.rest.parseResponse
import me.aburke.hotelbooking.facade.rest.snapshot.snapshotTest
import me.aburke.hotelbooking.facade.rest.withSessionId
import me.aburke.hotelbooking.rest.client.api.AdminUnstableApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.invoker.ApiResponse
import me.aburke.hotelbooking.rest.client.model.CreateUser201Response
import me.aburke.hotelbooking.rest.client.model.CreateUserRequest
import me.aburke.hotelbooking.rest.client.model.ProblemResponse
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import me.aburke.hotelbooking.rest.client.model.UserRole as UserRoleDto

class CreateUserTest : AbstractCreateUserTest() {

    @Test
    fun `should create user`() = snapshotTest(javalin) { client ->
        `RUN should create user`(
            object : TestRequest<ApiResponse<CreateUser201Response>>() {
                override fun makeRequest(): ApiResponse<CreateUser201Response> =
                    AdminUnstableApi(client.withSessionId(sessionId)).createUserWithHttpInfo(
                        CreateUserRequest().also {
                            it.loginId = loginId
                            it.password = password
                            it.name = name
                            it.roles = roles.map {
                                UserRoleDto.fromValue(it.name)
                            }
                        },
                    )

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.statusCode).isEqualTo(201)
                    s.assertThat(response.data).isEqualTo(
                        CreateUser201Response().also {
                            it.userId = userId
                        },
                    )
                }
            },
        )
    }

    @Test
    fun `should return 409 when username not available`() = test(javalin) { _, _ ->
        `RUN should return 409 when username not available`(
            object : TestRequest<ApiException>() {
                override fun makeRequest(): ApiException = assertThrows<ApiException> {
                    AdminUnstableApi(javalin.client(sessionId)).createUser(
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

                override fun makeAssertions(s: SoftAssertions) {
                    val responseBody = response.responseBody.parseResponse<ProblemResponse>()
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
                }
            },
        )
    }

    @Test
    fun `should return 403 when user does not have MANAGE_USERS role`() = test(javalin) { _, _ ->
        `RUN should return 403 when user does not have MANAGE_USERS role`(
            object : TestRequest<ApiException>() {
                override fun makeRequest(): ApiException = assertThrows<ApiException> {
                    AdminUnstableApi(javalin.client(sessionId)).createUser(
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

                override fun makeAssertions(s: SoftAssertions) {
                    val responseBody = response.responseBody.parseResponse<ProblemResponse>()
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
            },
        )
    }
}
