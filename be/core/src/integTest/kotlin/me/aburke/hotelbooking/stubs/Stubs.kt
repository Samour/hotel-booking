package me.aburke.hotelbooking.stubs

import io.mockk.every
import io.mockk.mockk
import me.aburke.hotelbooking.coreModule
import me.aburke.hotelbooking.ports.repository.SessionRepository
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.stubs.repository.SessionRepositoryStub
import me.aburke.hotelbooking.stubs.repository.UserRepositoryStub
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.fileProperties
import java.time.Clock
import java.time.Instant

class Stubs {

    val clock = mockk<Clock>()
    val userRepository = UserRepositoryStub()
    val sessionRepository = SessionRepositoryStub()

    val time = Instant.now()

    fun make(): KoinApplication {
        every { clock.instant() } returns time

        val stubsModule = module {
            single { clock }
            single<UserRepository> { userRepository }
            single<SessionRepository> { sessionRepository }
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
