package me.aburke.hotelbooking.repository.postgres.hotel

import me.aburke.hotelbooking.ports.repository.HotelRepository
import me.aburke.hotelbooking.repository.postgres.executeQueryWithRollback
import java.sql.Connection
import java.util.*

class PostgresHotelRepository(
    connection: Connection,
) : HotelRepository {

    private val timeZone: TimeZone =
        TimeZone.getTimeZone(
            connection.readHotelQuery()
                .executeQueryWithRollback()
                .apply { next() }
                .getString("time_zone"),
        )

    override fun loadTimeZone(): TimeZone = timeZone
}
