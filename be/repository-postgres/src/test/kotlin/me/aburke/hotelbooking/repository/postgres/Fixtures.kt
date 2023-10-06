package me.aburke.hotelbooking.repository.postgres

import io.mockk.every
import io.mockk.mockk
import me.aburke.hotelbooking.migrations.postgres.executeScript
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.fileProperties
import java.sql.Connection
import java.time.Clock

fun createApp(): KoinApplication {
    val clockMock = mockk<Clock> {
        every { instant() } returns clockTime
    }

    val testModule = module {
        single { clockMock }
    }

    return koinApplication {
        fileProperties()
        modules(testModule, postgresModule)
    }.also {
        with(it.koin.get<Connection>()) {
            executeScript("drop_db.sql")
            executeScript("bootstrap_db.sql")
        }
    }
}
