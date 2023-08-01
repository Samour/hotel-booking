package me.aburke.hotelbooking.stubs

import me.aburke.hotelbooking.coreModule
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.stubs.repository.UserRepositoryStub
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class Stubs {

    val userRepositoryStub = UserRepositoryStub()

    fun make(): KoinApplication {
        val stubsModule = module {
            single<UserRepository> { userRepositoryStub }
        }

        return koinApplication {
            modules(
                stubsModule,
                coreModule,
            )
        }
    }
}
