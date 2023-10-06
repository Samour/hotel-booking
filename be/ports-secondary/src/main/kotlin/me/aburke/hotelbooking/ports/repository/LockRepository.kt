package me.aburke.hotelbooking.ports.repository

interface LockRepository {

    /**
     * Returns true iff lock was successfully acquired
     *
     * Returns false if a lock with {@param key} already exists. Nonce is stored in lock record,
     * but otherwise ignored for lock collision.
     */
    fun acquireLock(key: String, nonce: String, expireAfterSeconds: Int): Boolean

    /**
     * Deletes lock where key and nonce both match
     *
     * Nonce is present so that a process does not release a lock that was actually created by a
     * different process
     */
    fun releaseLock(key: String, nonce: String)
}
