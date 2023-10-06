package me.aburke.hotelbooking.repository.redis

class Namespace(val name: String) {

    fun key(key: String) = "$name:$key"

    companion object {

        val session = Namespace("session")
        val lock = Namespace("lock")
    }
}
