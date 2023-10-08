package me.aburke.hotelbooking.repository.postgres

import io.mockk.every
import io.mockk.mockk
import me.aburke.hotelbooking.migrations.postgres.executeScript
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.fileProperties
import java.time.Clock
import javax.sql.DataSource

// TODO It looks like test startup is starting to slow quite a bit
// Look in to using a single app context across all tests
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
    }.apply {
        koin.get<DataSource>().connection.use {
            it.executeScript("drop_db.sql")
            it.executeScript("bootstrap_db.sql")
        }
    }
}
