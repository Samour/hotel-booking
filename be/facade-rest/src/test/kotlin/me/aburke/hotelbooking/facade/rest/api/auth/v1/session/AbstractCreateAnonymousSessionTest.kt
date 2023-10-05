package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import io.javalin.Javalin
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.user.AnonymousUserCreated
import me.aburke.hotelbooking.ports.scenario.user.CreateAnonymousUserPort
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.time.Instant

abstract class AbstractCreateAnonymousSessionTest {

    protected val sessionId = "session-id"
    protected val userId = "user-id"
    protected val sessionExpiryTime = Instant.now().plusSeconds(90)

    private val stubs = Stubs()

    protected lateinit var javalin: Javalin

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    protected fun <T : Any> `RUN should create anonymous user and set session cookie`(testRequest: TestRequest<T>) {
        every {
            stubs.createAnonymousUserPort.run(CreateAnonymousUserPort.Details)
        } returns AnonymousUserCreated(
            UserSession(
                sessionId = sessionId,
                userId = userId,
                loginId = null,
                userRoles = setOf(UserRole.CUSTOMER),
                anonymousUser = true,
                sessionExpiryTime = sessionExpiryTime,
            ),
        )

        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            s.check {
                verify(exactly = 1) {
                    stubs.createAnonymousUserPort.run(CreateAnonymousUserPort.Details)
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }
}
