package me.aburke.hotelbooking.lock

class FastFailLock(private val lockNamespace: String, private val lockExpirySeconds: Int) {

    fun <T> execute(lockId: String, returnOnLockConflict: T, action: () -> T): T {
        TODO()
    }
}
