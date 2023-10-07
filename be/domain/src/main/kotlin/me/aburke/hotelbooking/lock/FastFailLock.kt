package me.aburke.hotelbooking.lock

import me.aburke.hotelbooking.ports.repository.LockRepository
import java.util.UUID.randomUUID

class FastFailLock(
    private val lockNamespace: String,
    private val lockExpirySeconds: Int,
    private val lockRepository: LockRepository,
) {

    fun <T> execute(lockKey: String, returnOnLockConflict: T, action: () -> T): T {
        val lockId = "$lockNamespace:$lockKey"
        val nonce = randomUUID().toString()

        return if (lockRepository.acquireLock(lockId, nonce, lockExpirySeconds)) {
            try {
                action()
            } finally {
                lockRepository.releaseLock(lockId, nonce)
            }
        } else {
            returnOnLockConflict
        }
    }
}
