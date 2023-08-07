package me.aburke.hotelbooking

import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.ports.scenario.room.AddRoomTypePort
import me.aburke.hotelbooking.ports.scenario.user.*
import me.aburke.hotelbooking.scenario.room.AddRoomTypeScenario
import me.aburke.hotelbooking.scenario.user.*
import me.aburke.hotelbooking.session.SessionFactory
import me.aburke.hotelbooking.stock.DatesCalculator
import org.koin.dsl.module
import java.time.Duration

val coreModule = module {
    single { PasswordHasher() }
    single<CreateUserPort> { CreateUserScenario(get(), get()) }

    single {
        SessionFactory(
            get(),
            sessionDuration = Duration.parse(getProperty("auth.session.duration")),
        )
    }
    single<CreateAnonymousUserPort> { CreateAnonymousUserScenario(get(), get(), get()) }
    single<SignUpPort> { SignUpScenario(get(), get(), get(), get()) }

    single<LogInPort> {
        LogInScenario(
            passwordHasher = get(),
            sessionFactory = get(),
            userRepository = get(),
            sessionRepository = get(),
        )
    }

    single<GetAuthStatePort> { GetAuthStateScenario(get()) }

    single { DatesCalculator() }
    single<AddRoomTypePort> {
        AddRoomTypeScenario(
            get(),
            get(),
            get(),
            get(),
            populateRoomRange = getProperty<String>("scenario.room-type-add.populate-room-range").toInt(),
            backPopulateDays = getProperty<String>("scenario.room-type-add.back-populate-days").toInt(),
        )
    }
}
