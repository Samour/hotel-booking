package me.aburke.hotelbooking.facade.rest

import io.javalin.Javalin
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.scenario.room.AddRoomTypeScenario
import me.aburke.hotelbooking.scenario.user.*
import org.assertj.core.api.SoftAssertions
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.time.Instant
import java.util.UUID

class Stubs {

    val logInScenario = mockk<LogInScenario>()
    val getAuthStateScenario = mockk<GetAuthStateScenario>()
    val createAnonymousUserScenario = mockk<CreateAnonymousUserScenario>()
    val signUpScenario = mockk<SignUpScenario>()
    val createUserScenario = mockk<CreateUserScenario>()
    val addRoomTypeScenario = mockk<AddRoomTypeScenario>()

    private lateinit var app: KoinApplication

    private val sessions = mutableListOf<UserSession>()

    fun make(properties: Map<String, String> = mapOf()): Javalin {
        val stubsModule = module {
            single { logInScenario }
            single { getAuthStateScenario }
            single { createAnonymousUserScenario }
            single { signUpScenario }
            single { createUserScenario }
            single { addRoomTypeScenario }
        }
        app = koinApplication {
            properties(properties)
            modules(stubsModule, restModule)
        }

        return app.koin.get<Javalin>()
    }

    fun cleanUp() = app.close()

    fun prepareSession(vararg roles: UserRole): String {
        val sessionId = UUID.randomUUID().toString()
        every {
            getAuthStateScenario.run(
                GetAuthStateDetails(sessionId)
            )
        } returns GetAuthStateResult.SessionExists(
            UserSession(
                sessionId = sessionId,
                userId = UUID.randomUUID().toString(),
                loginId = "stubbed-login-id",
                userRoles = setOf(*roles),
                anonymousUser = false,
                sessionExpiryTime = Instant.now().plusSeconds(10)
            ).also { sessions.add(it) }
        )

        return sessionId
    }

    fun SoftAssertions.verifyStubs() {
        sessions.forEach {
            check {
                verify {
                    getAuthStateScenario.run(GetAuthStateDetails(it.sessionId))
                }
            }
        }
        check {
            confirmVerified(
                logInScenario,
                getAuthStateScenario,
                createAnonymousUserScenario,
                signUpScenario,
                createUserScenario,
                addRoomTypeScenario,
            )
        }
    }
}
