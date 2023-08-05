package me.aburke.hotelbooking.facade.rest.api.admin.v1.user

import io.javalin.Javalin
import io.javalin.testtools.JavalinTest.test
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.assertThatJson
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.scenario.user.CreateUserDetails
import me.aburke.hotelbooking.scenario.user.CreateUserResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val LOGIN_ID = "login-id"
private const val PASSWORD = "password"
private const val NAME = "name"
private const val USER_ID = "user-id"

private val roles = setOf(UserRole.MANAGE_ROOMS)

class CreateUserHandlerTest {

    private val stubs = Stubs()

    private lateinit var javalin: Javalin

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    @Test
    fun `should create user`() = test(javalin) { _, client ->
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

        val sessionId = stubs.prepareSession(setOf(UserRole.MANAGE_USERS))
        val response = client.request("/api/admin/v1/user") {
            it.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                .header("Content-Type", "application/json")
                .post(
                    """
                        {
                            "login_id": "$LOGIN_ID",
                            "password": "$PASSWORD",
                            "name": "$NAME",
                            "roles": ["${roles.first().name}"]
                        }
                    """.trimIndent().toRequestBody("application/json".toMediaType())
                )
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(201)
            s.assertThat(response.header("Content-Type")).isEqualTo("application/json")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "user_id": "$USER_ID"
                    }
                """.trimIndent()
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
    fun `should return 409 when username not available`() = test(javalin) { _, client ->
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

        val sessionId = stubs.prepareSession(setOf(UserRole.MANAGE_USERS))
        val response = client.request("/api/admin/v1/user") {
            it.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                .header("Content-Type", "application/json")
                .post(
                    """
                        {
                            "login_id": "$LOGIN_ID",
                            "password": "$PASSWORD",
                            "name": "$NAME",
                            "roles": ["${roles.first().name}"]
                        }
                    """.trimIndent().toRequestBody("application/json".toMediaType())
                )
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(409)
            s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
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
    fun `should return 403 when user does not have MANAGE_USERS role`() = test(javalin) { _, client ->
        val sessionId = stubs.prepareSession(setOf(UserRole.MANAGE_ROOMS))
        val response = client.request("/api/admin/v1/user") {
            it.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                .header("Content-Type", "application/json")
                .post(
                    """
                        {
                            "login_id": "$LOGIN_ID",
                            "password": "$PASSWORD",
                            "name": "$NAME",
                            "roles": ["${roles.first().name}"]
                        }
                    """.trimIndent().toRequestBody("application/json".toMediaType())
                )
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(403)
            s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
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
            with(stubs) {
                s.verifyStubs()
            }
        }
    }
}
