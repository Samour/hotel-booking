package me.aburke.hotelbooking.repository.postgres.hotel

import me.aburke.hotelbooking.ports.repository.HotelRepository
import me.aburke.hotelbooking.repository.postgres.executeQueryWithRollback
import java.util.*
import javax.sql.DataSource

class PostgresHotelRepository(
    dataSource: DataSource,
) : HotelRepository {

    private val timeZone: TimeZone = dataSource.connection.use { connection ->
        TimeZone.getTimeZone(
            connection.readHotelQuery()
                .executeQueryWithRollback()
                .apply { next() }
                .getString("time_zone"),
        )
    }

    override fun loadTimeZone(): TimeZone = timeZone
}
