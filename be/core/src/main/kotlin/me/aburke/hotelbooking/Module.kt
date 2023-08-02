package me.aburke.hotelbooking

import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.scenario.user.CreateAnonymousUserScenario
import me.aburke.hotelbooking.scenario.user.CreateUserScenario
import me.aburke.hotelbooking.scenario.user.LogInScenario
import me.aburke.hotelbooking.session.SessionFactory
import org.koin.dsl.module
import java.time.Duration

val coreModule = module {
    single { CreateAnonymousUserScenario(get()) }

    single { PasswordHasher() }
    single { CreateUserScenario(get(), get()) }

    single {
        SessionFactory(
            sessionDuration = Duration.parse(getProperty("auth.session.duration")),
        )
    }
    single {
        LogInScenario(
            passwordHasher = get(),
            sessionFactory = get(),
            userRepository = get(),
            sessionRepository = get(),
        )
    }
}
