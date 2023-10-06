package me.aburke.hotelbooking.facade.rest.api.admin.user

import io.javalin.Javalin
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.ports.scenario.user.CreateUserDetails
import me.aburke.hotelbooking.ports.scenario.user.CreateUserResult
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class AbstractCreateUserTest {

    protected val loginId = "login-id"
    protected val password = "password"
    protected val name = "name"
    protected val userId = "user-id"
    protected val roles = setOf(UserRole.MANAGE_ROOMS)

    private val stubs = Stubs()

    protected lateinit var javalin: Javalin

    protected lateinit var sessionId: String

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    protected fun <T : Any> `RUN should create user`(testRequest: TestRequest<T>) {
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

        sessionId = stubs.prepareSession(UserRole.MANAGE_USERS).sessionId
        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
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

    protected fun <T : Any> `RUN should return 409 when username not available`(testRequest: TestRequest<T>) {
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

        sessionId = stubs.prepareSession(UserRole.MANAGE_USERS).sessionId
        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
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

    protected fun <T : Any> `RUN should return 403 when user does not have MANAGE_USERS role`(
        testRequest: TestRequest<T>,
    ) {
        sessionId = stubs.prepareSession(UserRole.MANAGE_ROOMS).sessionId
        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            with(stubs) {
                s.verifyStubs()
            }
        }
    }
}
