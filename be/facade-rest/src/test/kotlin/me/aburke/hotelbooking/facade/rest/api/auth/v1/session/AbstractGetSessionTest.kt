package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import io.javalin.Javalin
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateDetails
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateResult
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.time.Instant

abstract class AbstractGetSessionTest {

    protected val sessionId = "session-id"
    protected val loginId = "login-id"
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

    protected fun <T : Any> `RUN should return session details when authenticated`(testRequest: TestRequest<T>) {
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(sessionId),
            )
        } returns GetAuthStateResult.SessionExists(
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
                    stubs.getAuthStatePort.run(
                        GetAuthStateDetails(sessionId),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should return session details when anonymously authenticated`(
        testRequest: TestRequest<T>,
    ) {
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(sessionId),
            )
        } returns GetAuthStateResult.SessionExists(
            UserSession(
                sessionId = sessionId,
                userId = userId,
                loginId = null,
                userRoles = userRoles,
                anonymousUser = true,
                sessionExpiryTime = sessionExpiryTime,
            ),
        )

        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            s.check {
                verify(exactly = 1) {
                    stubs.getAuthStatePort.run(
                        GetAuthStateDetails(sessionId),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should return 401 when invalid session ID provided`(testRequest: TestRequest<T>) {
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(sessionId),
            )
        } returns GetAuthStateResult.SessionDoesNotExist

        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            s.check {
                verify(exactly = 1) {
                    stubs.getAuthStatePort.run(
                        GetAuthStateDetails(sessionId),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should return 401 when no session ID provided`(testRequest: TestRequest<T>) {
        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            with(stubs) {
                s.verifyStubs()
            }
        }
    }
}
