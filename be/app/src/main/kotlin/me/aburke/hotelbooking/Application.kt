package me.aburke.hotelbooking

import io.javalin.Javalin
import me.aburke.hotelbooking.facade.rest.restModule
import me.aburke.hotelbooking.repository.postgres.postgresModule
import me.aburke.hotelbooking.repository.redis.redisModule
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.koin.fileProperties
import java.time.Clock

class Application(
    private val javalin: Javalin,
    private val port: Int,
) {

    fun start() = javalin.start(port)

    fun stop() = javalin.stop()
}

val appModules = listOf(
    domainModule,
    postgresModule,
    redisModule,
    restModule,
)

fun main() {
    val appModule = module {
        single<Clock> { Clock.systemUTC() }
        single {
            Application(
                get(),
                getProperty<String>("server.http.port").toInt(),
            )
        } onClose { it?.stop() }
    }

    startKoin {
        fileProperties()
        fileProperties("/endpoints.properties")
        fileProperties("/features.properties")
        modules(
            appModule,
            *appModules.toTypedArray(),
        )
    }.apply {
        koin.get<Application>().start()
    }
}
