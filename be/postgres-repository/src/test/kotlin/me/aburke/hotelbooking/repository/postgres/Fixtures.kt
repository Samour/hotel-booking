package me.aburke.hotelbooking.repository.postgres

import me.aburke.hotelbooking.migrations.postgres.executeScript
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.fileProperties
import java.sql.Connection

fun createApp(): KoinApplication = koinApplication {
    fileProperties()
    modules(postgresModule)
}.also {
    with(it.koin.get<Connection>()) {
        executeScript("drop_db.sql")
        executeScript("bootstrap_db.sql")
    }
}
