package me.aburke.hotelbooking.facade.rest

import io.javalin.Javalin
import io.mockk.confirmVerified
import io.mockk.mockk
import me.aburke.hotelbooking.scenario.user.GetAuthStateScenario
import me.aburke.hotelbooking.scenario.user.LogInScenario
import org.assertj.core.api.SoftAssertions
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class Stubs {

    val logInScenario = mockk<LogInScenario>()
    val getAuthStateScenario = mockk<GetAuthStateScenario>()

    private lateinit var app: KoinApplication

    fun make(): Javalin {
        val stubsModule = module {
            single { logInScenario }
            single { getAuthStateScenario }
        }
        app = koinApplication {
            modules(stubsModule, restModule)
        }

        return app.koin.get<Javalin>()
    }

    fun cleanUp() = app.close()

    fun SoftAssertions.verifyStubs() = check {
        confirmVerified(
            logInScenario,
            getAuthStateScenario,
        )
    }
}
