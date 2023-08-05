package me.aburke.hotelbooking

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.client.AppTestClient
import me.aburke.hotelbooking.migrations.postgres.executeScript
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.fileProperties
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.Protocol
import java.sql.Connection

fun createApp(): KoinApplication = koinApplication {
    fileProperties()
    modules(*appModules.toTypedArray())
}.also {
    it.koin.get<Connection>().apply {
        executeScript("drop_db.sql")
        executeScript("bootstrap_db.sql")
        executeScript("test_data.sql")
    }
    it.koin.get<JedisPooled>().sendCommand(Protocol.Command.FLUSHDB)
}

fun KoinApplication.restTest(case: (AppTestClient) -> Unit) = test(koin.get()) { _, client ->
    case(AppTestClient(client))
}
