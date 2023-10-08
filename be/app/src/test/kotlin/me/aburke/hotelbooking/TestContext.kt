package me.aburke.hotelbooking

import io.javalin.Javalin
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
        val clock = app.koin.get<Clock>()
        every { clock.instant() } returns time
    }
}

// TODO See what we can do here about re-using app context too
fun createApp(
    populateTestData: Boolean = true,
    useEndpointsProperties: Boolean = true,
): TestContext {
    val testModule = module {
        single<Clock> { mockk() }
    }

    val app = koinApplication {
        fileProperties()
        fileProperties("/features.properties")
        if (useEndpointsProperties) {
            fileProperties("/endpoints.properties")
        }
        modules(testModule, *appModules.toTypedArray())
    }
    app.koin.get<DataSource>().connection.use {
        it.executeScript("drop_db.sql")
        it.executeScript("bootstrap_db.sql")
        if (populateTestData) {
            it.executeScript("test_data.sql")
        }
    }
    app.koin.get<JedisPooled>().sendCommand(Protocol.Command.FLUSHDB)
    app.koin.get<Javalin>().start(0)

    return TestContext(app).apply { stubClock() }
}
