package me.aburke.hotelbooking

import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.scenario.user.CreateAnonymousUserScenario
import me.aburke.hotelbooking.scenario.user.CreateUserScenario
import org.koin.dsl.module

val coreModule = module {
    single { CreateAnonymousUserScenario(get()) }

    single { PasswordHasher() }
    single { CreateUserScenario(get(), get()) }
}
