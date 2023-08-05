package me.aburke.hotelbooking

import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.scenario.user.*
import me.aburke.hotelbooking.session.SessionFactory
import org.koin.dsl.module
import java.time.Duration

val coreModule = module {
    single { PasswordHasher() }
    single { CreateUserScenario(get(), get()) }

    single {
        SessionFactory(
            get(),
            sessionDuration = Duration.parse(getProperty("auth.session.duration")),
        )
    }
    single { CreateAnonymousUserScenario(get(), get(), get()) }
    single { SignUpScenario(get(), get(), get(), get()) }

    single {
        LogInScenario(
            passwordHasher = get(),
            sessionFactory = get(),
            userRepository = get(),
            sessionRepository = get(),
        )
    }

    single { GetAuthStateScenario(get()) }
}
