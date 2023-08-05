package me.aburke.hotelbooking.repository.redis

import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.fileProperties
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.Protocol.Command
import java.time.Clock

private val testModule = module {
    single<Clock> { Clock.systemUTC() }
}

fun createApp(): KoinApplication = koinApplication {
    fileProperties()
    modules(testModule, redisModule)
}.also {
    it.koin.get<JedisPooled>().sendCommand(Command.FLUSHDB)
}
