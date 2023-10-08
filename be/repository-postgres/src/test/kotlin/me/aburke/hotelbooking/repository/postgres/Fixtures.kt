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

private val app: KoinApplication by lazy {
    val clockMock = mockk<Clock> {
        every { instant() } returns clockTime
    }

    val testModule = module {
        single { clockMock }
    }

    koinApplication {
        fileProperties()
        modules(testModule, postgresModule)
    }.also { registerAppCleanUp(it) }
}

private fun registerAppCleanUp(app: KoinApplication) {
    Runtime.getRuntime().addShutdownHook(
        Thread {
            runCatching {
                app.close()
            }
        },
    )
}

fun appForTest(): KoinApplication = app.apply {
    koin.get<DataSource>().connection.use {
        it.executeScript("drop_db.sql")
        it.executeScript("bootstrap_db.sql")
    }
}
