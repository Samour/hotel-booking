package me.aburke.hotelbooking.stubs.repository

import me.aburke.hotelbooking.ports.repository.LockRepository
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock

class LockRepositoryStub : LockRepository {

    private val managementLock = ReentrantLock()
    private val locks = mutableMapOf<String, String>()

    override fun acquireLock(key: String, nonce: String, expireAfterSeconds: Int): Boolean {
        return inManagementLock {
            if (locks.containsKey(key)) {
                false
            } else {
                locks[key] = nonce
                true
            }
        }
    }

    override fun releaseLock(key: String, nonce: String) {
        inManagementLock {
            if (locks[key] == nonce) {
                locks.remove(key)
            }
        }
    }

    private fun <T> inManagementLock(action: () -> T): T {
        if (!managementLock.tryLock(200, TimeUnit.MILLISECONDS)) {
            throw TimeoutException("Timeout while waiting on management lock")
        }

        return action().also { managementLock.unlock() }
    }
}
