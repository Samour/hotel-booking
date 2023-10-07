package me.aburke.hotelbooking

import io.mockk.every
import me.aburke.hotelbooking.lock.FastFailLock

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> FastFailLock.stubUnblocked(lockKey: String, returnOnLockConflict: T) {
    every {
        execute(eq(lockKey), eq(returnOnLockConflict), any())
    } answers {
        (invocation.args[2] as () -> T)()
    }
}

inline fun <reified T : Any> FastFailLock.stubConflict(lockKey: String, returnOnLockConflict: T) {
    every {
        execute(eq(lockKey), eq(returnOnLockConflict), any())
    } returns returnOnLockConflict
}
