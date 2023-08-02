package me.aburke.hotelbooking.stubs

import me.aburke.hotelbooking.coreModule
import me.aburke.hotelbooking.ports.repository.SessionRepository
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.stubs.repository.SessionRepositoryStub
import me.aburke.hotelbooking.stubs.repository.UserRepositoryStub
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.fileProperties

class Stubs {

    val userRepositoryStub = UserRepositoryStub()
    val sessionRepositoryStub = SessionRepositoryStub()

    fun make(): KoinApplication {
        val stubsModule = module {
            single<UserRepository> { userRepositoryStub }
            single<SessionRepository> { sessionRepositoryStub }
        }

        return koinApplication {
            fileProperties()
            modules(
                stubsModule,
                coreModule,
            )
        }
    }
}
