package me.aburke.hotelbooking.facade.rest

import io.javalin.Javalin
import io.mockk.confirmVerified
import io.mockk.mockk
import me.aburke.hotelbooking.scenario.user.CreateAnonymousUserScenario
import me.aburke.hotelbooking.scenario.user.GetAuthStateScenario
import me.aburke.hotelbooking.scenario.user.LogInScenario
import me.aburke.hotelbooking.scenario.user.SignUpScenario
import org.assertj.core.api.SoftAssertions
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class Stubs {

    val logInScenario = mockk<LogInScenario>()
    val getAuthStateScenario = mockk<GetAuthStateScenario>()
    val createAnonymousUserScenario = mockk<CreateAnonymousUserScenario>()
    val signUpScenario = mockk<SignUpScenario>()

    private lateinit var app: KoinApplication

    fun make(properties: Map<String, String> = mapOf()): Javalin {
        val stubsModule = module {
            single { logInScenario }
            single { getAuthStateScenario }
            single { createAnonymousUserScenario }
            single { signUpScenario }
        }
        app = koinApplication {
            properties(properties)
            modules(stubsModule, restModule)
        }

        return app.koin.get<Javalin>()
    }

    fun cleanUp() = app.close()

    fun SoftAssertions.verifyStubs() = check {
        confirmVerified(
            logInScenario,
            getAuthStateScenario,
            createAnonymousUserScenario,
            signUpScenario,
        )
    }
}
