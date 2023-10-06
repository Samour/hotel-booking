package me.aburke.hotelbooking.repository.redis

import me.aburke.hotelbooking.ports.repository.LockRepository
import me.aburke.hotelbooking.ports.repository.SessionRepository
import org.koin.dsl.module
import org.koin.dsl.onClose
import redis.clients.jedis.JedisPooled

val redisModule = module {
    single {
        JedisPooled(
            getProperty<String>("redis.host"),
            getProperty<String>("redis.port").toInt(),
        )
    } onClose { it?.close() }

    single<SessionRepository> { RedisSessionRepository(get(), get()) }
    single<LockRepository> { RedisLockRepository(get()) }
}
