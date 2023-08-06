package me.aburke.hotelbooking.data

import java.time.Duration
import java.util.*

val sessionDuration = Duration.parse("PT30M")

const val hotelId = "test-hotel"
val hotelTimeZone = TimeZone.getTimeZone("Australia/Sydney")

data class TestUser(
    val userId: String,
    val loginId: String,
    val password: String,
) {
    companion object {
        val admin = TestUser(
            userId = "test-admin-id",
            loginId = "test-admin",
            password = "test-password",
        )
    }
}

object StockPopulation {
    const val BACK_POPULATE = 5
    const val POPULATE_RANGE = 200
}
