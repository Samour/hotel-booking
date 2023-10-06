package me.aburke.hotelbooking.facade.rest.api.auth.session

import io.javalin.Javalin
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.user.LogInCredentials
import me.aburke.hotelbooking.ports.scenario.user.LogInResult
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.time.Instant

abstract class AbstractLogInTest {

    protected val loginId = "login-id"
    protected val password = "password"
    protected val sessionId = "session-id"
    protected val userId = "user-id"
    protected val userRoles = setOf(UserRole.CUSTOMER)
    protected val sessionExpiryTime = Instant.now().plusSeconds(90)

    private val stubs = Stubs()

    protected lateinit var javalin: Javalin

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    protected fun <T : Any> `RUN should log in user & set session cookie on successful authentication`(
        testRequest: TestRequest<T>,
    ) {
        every {
            stubs.logInPort.run(
                LogInCredentials(
                    loginId = loginId,
                    password = password,
                ),
            )
        } returns LogInResult.UserSessionCreated(
            UserSession(
                sessionId = sessionId,
                userId = userId,
                loginId = loginId,
                userRoles = userRoles,
                anonymousUser = false,
                sessionExpiryTime = sessionExpiryTime,
            ),
        )

        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            s.check {
                verify(exactly = 1) {
                    stubs.logInPort.run(
                        LogInCredentials(
                            loginId = loginId,
                            password = password,
                        ),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should return 401 when invalid credentials supplied`(testRequest: TestRequest<T>) {
        every {
            stubs.logInPort.run(
                LogInCredentials(
                    loginId = loginId,
                    password = password,
                ),
            )
        } returns LogInResult.InvalidCredentials

        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            s.check {
                verify(exactly = 1) {
                    stubs.logInPort.run(
                        LogInCredentials(
                            loginId = loginId,
                            password = password,
                        ),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should return 400 when request body has invalid fields`(testRequest: TestRequest<T>) {
        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should return 400 when request body is malformed`(testRequest: TestRequest<T>) {
        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            with(stubs) {
                s.verifyStubs()
            }
        }
    }
}
