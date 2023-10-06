package me.aburke.hotelbooking.repository.redis

import me.aburke.hotelbooking.ports.repository.LockRepository
import redis.clients.jedis.JedisPooled

class RedisLockRepository(private val jedisPooled: JedisPooled) : LockRepository {

    override fun acquireLock(key: String, nonce: String, expireAfterSeconds: Int): Boolean {
        return jedisPooled.setnx(Namespace.lock.key(key), nonce)
            .takeIf { it == 1L }
            ?.also {
                jedisPooled.expire(Namespace.lock.key(key), expireAfterSeconds.toLong())
            } != null
    }

    override fun releaseLock(key: String, nonce: String) {
        jedisPooled.eval(
            """
                if redis.call("get", KEYS[1]) == ARGV[1] then
                    return redis.call("del", KEYS[1])
                else
                    return 0
                end
            """.trimIndent(),
            1,
            Namespace.lock.key(key),
            nonce,
        )
    }
}
