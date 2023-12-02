package me.aburke.hotelbooking.facade.rest

import io.javalin.Javalin
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.room.AddRoomTypePort
import me.aburke.hotelbooking.ports.scenario.room.HoldRoomPort
import me.aburke.hotelbooking.ports.scenario.room.ListRoomsPort
import me.aburke.hotelbooking.ports.scenario.user.CreateAnonymousUserPort
import me.aburke.hotelbooking.ports.scenario.user.CreateUserPort
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateDetails
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStatePort
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateResult
import me.aburke.hotelbooking.ports.scenario.user.LogInPort
import me.aburke.hotelbooking.ports.scenario.user.SignUpPort
import org.assertj.core.api.SoftAssertions
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.time.Instant

class Stubs {

    val logInPort = mockk<LogInPort>()
    val getAuthStatePort = mockk<GetAuthStatePort>()
    val createAnonymousUserPort = mockk<CreateAnonymousUserPort>()
    val signUpPort = mockk<SignUpPort>()
    val createUserPort = mockk<CreateUserPort>()
    val addRoomTypePort = mockk<AddRoomTypePort>()
    val listRoomsPort = mockk<ListRoomsPort>()
    val holdRoomPort = mockk<HoldRoomPort>()

    private lateinit var app: KoinApplication

    private val sessions = mutableListOf<UserSession>()

    fun make(properties: Map<String, String> = mapOf()): Javalin {
        val stubsModule = module {
            single { logInPort }
            single { getAuthStatePort }
            single { createAnonymousUserPort }
            single { signUpPort }
            single { createUserPort }
            single { addRoomTypePort }
            single { listRoomsPort }
            single { holdRoomPort }
        }
        app = koinApplication {
            properties(properties)
            modules(stubsModule, restModule)
        }

        return app.koin.get<Javalin>()
    }

    fun cleanUp() = app.close()

    fun prepareSession(vararg roles: UserRole): UserSession {
        val session = createSession(*roles)

        sessions.add(session)
        every {
            getAuthStatePort.run(
                GetAuthStateDetails(session.sessionId),
            )
        } returns GetAuthStateResult.SessionExists(session)

        return session
    }

    fun SoftAssertions.verifyStubs() {
        sessions.forEach {
            check {
                verify {
                    getAuthStatePort.run(GetAuthStateDetails(it.sessionId))
                }
            }
        }
        check {
            confirmVerified(
                logInPort,
                getAuthStatePort,
                createAnonymousUserPort,
                signUpPort,
                createUserPort,
                addRoomTypePort,
                listRoomsPort,
                holdRoomPort,
            )
        }
    }
}

const val SESSION_ID = "b6f7cdfe-7168-4882-94e0-8da9bca47200"
const val USER_ID = "b3df1a2c-7a00-4dfc-babe-8aeaa217bdb5"

fun createSession(vararg roles: UserRole) = UserSession(
    sessionId = SESSION_ID,
    userId = USER_ID,
    loginId = "stubbed-login-id",
    userRoles = setOf(*roles),
    anonymousUser = false,
    sessionExpiryTime = Instant.now().plusSeconds(10),
)
