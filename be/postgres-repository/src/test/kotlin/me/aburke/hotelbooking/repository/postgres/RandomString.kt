package me.aburke.hotelbooking.repository.postgres

import java.util.Random
import kotlin.streams.asSequence

object RandomString {

    private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun make(): String = Random().ints(0, charPool.size)
        .asSequence()
        .take(12)
        .map { charPool[it] }
        .joinToString("")
}
