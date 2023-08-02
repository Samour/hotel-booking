package me.aburke.hotelbooking.repository.redis

import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.fileProperties
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.Protocol.Command

fun createApp(): KoinApplication = koinApplication {
    fileProperties()
    modules(redisModule)
}.also {
    it.koin.get<JedisPooled>().sendCommand(Command.FLUSHDB)
}
