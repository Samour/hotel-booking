package me.aburke.hotelbooking.repository.postgres.hotel

import java.sql.Connection

fun Connection.readHotelQuery() = prepareStatement(
    """
        select hotel_id, time_zone from hotel limit 1
    """.trimIndent(),
)
