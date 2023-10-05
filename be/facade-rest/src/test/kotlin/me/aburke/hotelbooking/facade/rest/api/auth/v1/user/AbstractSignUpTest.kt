package me.aburke.hotelbooking.facade.rest.api.auth.v1.user

import io.javalin.Javalin
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.user.AnonymousSession
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateDetails
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateResult
import me.aburke.hotelbooking.ports.scenario.user.SignUpDetails
import me.aburke.hotelbooking.ports.scenario.user.SignUpResult
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.time.Instant

abstract class AbstractSignUpTest {

    protected val loginId = "login-id"
    protected val password = "password"
    protected val name = "name"
    protected val sessionId = "session-id"
    protected val userId = "user-id"
    protected val roles = setOf(UserRole.CUSTOMER)
    protected val sessionExpiryTime = Instant.now().plusSeconds(90)
    protected val anonymousSessionExpiryTime = Instant.now().plusSeconds(30)

    private val stubs = Stubs()

    protected lateinit var javalin: Javalin

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    protected fun <T : Any> `RUN should create user and set session cookie`(testRequest: TestRequest<T>) {
        every {
            stubs.signUpPort.run(
                SignUpDetails(
                    loginId = loginId,
                    rawPassword = password,
                    name = name,
                    anonymousUser = null,
                ),
            )
        } returns SignUpResult.Success(
            UserSession(
                sessionId = sessionId,
                userId = userId,
                loginId = loginId,
                userRoles = roles,
                anonymousUser = false,
                sessionExpiryTime = sessionExpiryTime,
            ),
        )

        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            s.check {
                verify(exactly = 1) {
                    stubs.signUpPort.run(
                        SignUpDetails(
                            loginId = loginId,
                            rawPassword = password,
                            name = name,
                            anonymousUser = null,
                        ),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should create new user when provided session ID is not valid`(
        testRequest: TestRequest<T>,
    ) {
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(
                    sessionId = sessionId,
                ),
            )
        } returns GetAuthStateResult.SessionDoesNotExist
        every {
            stubs.signUpPort.run(
                SignUpDetails(
                    loginId = loginId,
                    rawPassword = password,
                    name = name,
                    anonymousUser = null,
                ),
            )
        } returns SignUpResult.Success(
            UserSession(
                sessionId = sessionId,
                userId = userId,
                loginId = loginId,
                userRoles = roles,
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
                        GetAuthStateDetails(
                            sessionId = sessionId,
                        ),
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    stubs.signUpPort.run(
                        SignUpDetails(
                            loginId = loginId,
                            rawPassword = password,
                            name = name,
                            anonymousUser = null,
                        ),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should return 409 when username is not available`(testRequest: TestRequest<T>) {
        every {
            stubs.signUpPort.run(
                SignUpDetails(
                    loginId = loginId,
                    rawPassword = password,
                    name = name,
                    anonymousUser = null,
                ),
            )
        } returns SignUpResult.UsernameNotAvailable

        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            s.check {
                verify(exactly = 1) {
                    stubs.signUpPort.run(
                        SignUpDetails(
                            loginId = loginId,
                            rawPassword = password,
                            name = name,
                            anonymousUser = null,
                        ),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should create credentials for anonymous user`(testRequest: TestRequest<T>) {
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(
                    sessionId = sessionId,
                ),
            )
        } returns GetAuthStateResult.SessionExists(
            UserSession(
                sessionId = sessionId,
                userId = userId,
                loginId = null,
                userRoles = roles,
                anonymousUser = true,
                sessionExpiryTime = anonymousSessionExpiryTime,
            ),
        )
        every {
            stubs.signUpPort.run(
                SignUpDetails(
                    loginId = loginId,
                    rawPassword = password,
                    name = name,
                    anonymousUser = AnonymousSession(
                        sessionId = sessionId,
                        userId = userId,
                    ),
                ),
            )
        } returns SignUpResult.Success(
            UserSession(
                sessionId = sessionId,
                userId = userId,
                loginId = loginId,
                userRoles = roles,
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
                        GetAuthStateDetails(
                            sessionId = sessionId,
                        ),
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    stubs.signUpPort.run(
                        SignUpDetails(
                            loginId = loginId,
                            rawPassword = password,
                            name = name,
                            anonymousUser = AnonymousSession(
                                sessionId = sessionId,
                                userId = userId,
                            ),
                        ),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should return 409 when username is not available for anonymous user`(
        testRequest: TestRequest<T>,
    ) {
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(
                    sessionId = sessionId,
                ),
            )
        } returns GetAuthStateResult.SessionExists(
            UserSession(
                sessionId = sessionId,
                userId = userId,
                loginId = null,
                userRoles = roles,
                anonymousUser = true,
                sessionExpiryTime = anonymousSessionExpiryTime,
            ),
        )
        every {
            stubs.signUpPort.run(
                SignUpDetails(
                    loginId = loginId,
                    rawPassword = password,
                    name = name,
                    anonymousUser = AnonymousSession(
                        sessionId = sessionId,
                        userId = userId,
                    ),
                ),
            )
        } returns SignUpResult.UsernameNotAvailable

        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            s.check {
                verify(exactly = 1) {
                    stubs.getAuthStatePort.run(
                        GetAuthStateDetails(
                            sessionId = sessionId,
                        ),
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    stubs.signUpPort.run(
                        SignUpDetails(
                            loginId = loginId,
                            rawPassword = password,
                            name = name,
                            anonymousUser = AnonymousSession(
                                sessionId = sessionId,
                                userId = userId,
                            ),
                        ),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should return 409 when current user is not anonymous`(testRequest: TestRequest<T>) {
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(
                    sessionId = sessionId,
                ),
            )
        } returns GetAuthStateResult.SessionExists(
            UserSession(
                sessionId = sessionId,
                userId = userId,
                loginId = loginId,
                userRoles = roles,
                anonymousUser = false,
                sessionExpiryTime = anonymousSessionExpiryTime,
            ),
        )
        every {
            stubs.signUpPort.run(
                SignUpDetails(
                    loginId = loginId,
                    rawPassword = password,
                    name = name,
                    anonymousUser = AnonymousSession(
                        sessionId = sessionId,
                        userId = userId,
                    ),
                ),
            )
        } returns SignUpResult.UserIsNotAnonymous

        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            s.check {
                verify(exactly = 1) {
                    stubs.getAuthStatePort.run(
                        GetAuthStateDetails(
                            sessionId = sessionId,
                        ),
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    stubs.signUpPort.run(
                        SignUpDetails(
                            loginId = loginId,
                            rawPassword = password,
                            name = name,
                            anonymousUser = AnonymousSession(
                                sessionId = sessionId,
                                userId = userId,
                            ),
                        ),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should return 400 when current user does not exist`(testRequest: TestRequest<T>) {
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(
                    sessionId = sessionId,
                ),
            )
        } returns GetAuthStateResult.SessionExists(
            UserSession(
                sessionId = sessionId,
                userId = userId,
                loginId = null,
                userRoles = roles,
                anonymousUser = true,
                sessionExpiryTime = anonymousSessionExpiryTime,
            ),
        )
        every {
            stubs.signUpPort.run(
                SignUpDetails(
                    loginId = loginId,
                    rawPassword = password,
                    name = name,
                    anonymousUser = AnonymousSession(
                        sessionId = sessionId,
                        userId = userId,
                    ),
                ),
            )
        } returns SignUpResult.AnonymousUserDoesNotExist

        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            s.check {
                verify(exactly = 1) {
                    stubs.getAuthStatePort.run(
                        GetAuthStateDetails(
                            sessionId = sessionId,
                        ),
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    stubs.signUpPort.run(
                        SignUpDetails(
                            loginId = loginId,
                            rawPassword = password,
                            name = name,
                            anonymousUser = AnonymousSession(
                                sessionId = sessionId,
                                userId = userId,
                            ),
                        ),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }
}
