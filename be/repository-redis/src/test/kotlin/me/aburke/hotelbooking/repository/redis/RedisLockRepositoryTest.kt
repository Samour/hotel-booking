package me.aburke.hotelbooking.repository.redis

import me.aburke.hotelbooking.ports.repository.LockRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import redis.clients.jedis.JedisPooled

private const val LOCK_KEY = "lock-key"
private const val LOCK_NONCE = "lock-nonce"
private const val PRIOR_NONCE = "prior-nonce"
private const val LOCK_EXPIRY_SECONDS = 60

class RedisLockRepositoryTest {

    private lateinit var app: KoinApplication
    private lateinit var jedisPooled: JedisPooled
    private lateinit var underTest: LockRepository

    @BeforeEach
    fun init() {
        app = createApp()
        jedisPooled = app.koin.get()
        underTest = app.koin.get()
    }

    @AfterEach
    fun cleanUp() {
        app.close()
    }

    @Test
    fun `should insert lock record and return true`() {
        val result = underTest.acquireLock(LOCK_KEY, LOCK_NONCE, LOCK_EXPIRY_SECONDS)

        val allLocks = loadAllLocks()

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(true)
            s.assertThat(allLocks).containsExactly(
                Namespace.lock.key(LOCK_KEY) to LOCK_NONCE,
            )
        }
    }

    @Test
    fun `should not insert lock record, then return false when lock already exists`() {
        underTest.acquireLock(LOCK_KEY, PRIOR_NONCE, LOCK_EXPIRY_SECONDS).also {
            assertThat(it).isTrue()
        }

        val result = underTest.acquireLock(LOCK_KEY, LOCK_NONCE, LOCK_EXPIRY_SECONDS)

        val allLocks = loadAllLocks()

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(false)
            s.assertThat(allLocks).containsExactly(
                Namespace.lock.key(LOCK_KEY) to PRIOR_NONCE,
            )
        }
    }

    @Test
    fun `lock should expire after expiry time`() {
        underTest.acquireLock(LOCK_KEY, LOCK_NONCE, 1).also {
            assertThat(it).isTrue()
        }

        Thread.sleep(1500)

        val allLocks = loadAllLocks()

        assertThat(allLocks).isEmpty()
    }

    @Test
    fun `should delete lock record when nonce matches`() {
        underTest.acquireLock(LOCK_KEY, LOCK_NONCE, LOCK_EXPIRY_SECONDS).also {
            assertThat(it).isTrue()
        }

        underTest.releaseLock(LOCK_KEY, LOCK_NONCE)

        val allLocks = loadAllLocks()

        assertThat(allLocks).isEmpty()
    }

    @Test
    fun `should not delete lock record when nonce does not match`() {
        underTest.acquireLock(LOCK_KEY, LOCK_NONCE, LOCK_EXPIRY_SECONDS).also {
            assertThat(it).isTrue()
        }

        underTest.releaseLock(LOCK_KEY, PRIOR_NONCE)

        val allLocks = loadAllLocks()

        assertThat(allLocks).containsExactly(
            Namespace.lock.key(LOCK_KEY) to LOCK_NONCE,
        )
    }

    @Test
    fun `should do nothing & return when attempting to release lock that does not exist`() {
        underTest.releaseLock(LOCK_KEY, LOCK_NONCE)

        val allLocks = loadAllLocks()

        assertThat(allLocks).isEmpty()
    }

    private fun loadAllLocks(): List<Pair<String, String>> =
        jedisPooled.keys(Namespace.lock.key("*"))
            .map { it to jedisPooled.get(it) }
}
