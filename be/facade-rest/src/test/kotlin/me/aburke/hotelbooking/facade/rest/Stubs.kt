package me.aburke.hotelbooking.facade.rest

import io.javalin.Javalin
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication

class Stubs {

    private lateinit var app: KoinApplication

    fun make(): Javalin {
        app = koinApplication {
            modules(restModule)
        }

        return app.koin.get<Javalin>()
    }

    fun cleanUp() = app.close()
}
