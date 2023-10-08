package me.aburke.hotelbooking

import io.javalin.Javalin
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import me.aburke.hotelbooking.migrations.postgres.executeScript
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.fileProperties
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.Protocol
import java.time.Clock
import java.time.Instant
import javax.sql.DataSource

class TestContext(
    val app: KoinApplication,
) {

    var time = Instant.now().minusSeconds(10_000)
        private set

    fun incrementClock() {
        time = time.plusMillis((10L..350L).random())
    }

    fun stubClock() {
        app.koin.get<Clock>().also {
            clearMocks(it)
            every { it.instant() } answers { time }
        }
    }
}

private data class AppParameters(
    val useEndpointsProperties: Boolean,
)

private val appCache = mutableMapOf<AppParameters, KoinApplication>()

private fun memoizedApp(params: AppParameters) = appCache.getOrPut(params) {
    val testModule = module {
        single<Clock> { mockk() }
    }

    koinApplication {
        fileProperties()
        fileProperties("/features.properties")
        if (params.useEndpointsProperties) {
            fileProperties("/endpoints.properties")
        }
        modules(testModule, *appModules.toTypedArray())
    }.also {
        registerAppCleanUp(it)
        it.koin.get<Javalin>().start(0)
    }
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

fun createTestContext(
    populateTestData: Boolean = true,
    useEndpointsProperties: Boolean = true,
): TestContext {
    val app = memoizedApp(
        AppParameters(
            useEndpointsProperties = useEndpointsProperties,
        ),
    )
    app.koin.get<DataSource>().connection.use {
        it.executeScript("drop_db.sql")
        it.executeScript("bootstrap_db.sql")
        if (populateTestData) {
            it.executeScript("test_data.sql")
        }
    }
    app.koin.get<JedisPooled>().sendCommand(Protocol.Command.FLUSHDB)

    return TestContext(app).apply { stubClock() }
}
