package me.aburke.hotelbooking.repository.postgres

import me.aburke.hotelbooking.ports.repository.InsertRoomType
import me.aburke.hotelbooking.ports.repository.RoomRepository
import me.aburke.hotelbooking.ports.repository.RoomTypeRecord
import org.postgresql.util.PSQLException
import java.sql.Connection
import java.time.Clock
import java.time.LocalDate
import java.util.UUID.randomUUID

class PostgresRoomRepository(
    private val clock: Clock,
    private val connection: Connection,
) : RoomRepository {

    private val hotelId: String =
        connection.readHotelQuery()
            .executeQueryWithRollback()
            .apply { next() }
            .getString("hotel_id")

    override fun insertRoomType(roomType: InsertRoomType, populateDates: List<LocalDate>): String {
        val roomTypeId = randomUUID().toString()
        val roomTypeDescriptionId = randomUUID().toString()

        val insertRoomTypeQuery = connection.insertRoomTypeQuery(
            hotelId = hotelId,
            roomTypeId = roomTypeId,
            stockLevel = roomType.stockLevel,
        )
        val insertRoomTypeDescriptionQuery = connection.insertRoomTypeDescriptionQuery(
            roomTypeDescriptionId = roomTypeDescriptionId,
            roomTypeId = roomTypeId,
            title = roomType.title,
            pricePerNight = roomType.pricePerNight,
            description = roomType.description,
            imageUrls = roomType.imageUrls,
        )
        val insertRoomStockQuery = connection.takeIf { populateDates.isNotEmpty() }
            ?.insertRoomStockQuery(
                roomTypeId = roomTypeId,
                stockLevel = roomType.stockLevel,
                dates = populateDates,
            )

        try {
            insertRoomTypeQuery.executeUpdate()
            insertRoomTypeDescriptionQuery.executeUpdate()
            insertRoomStockQuery?.executeBatch()
            connection.commit()
        } catch (e: PSQLException) {
            connection.rollback()
            throw e
        }

        return roomTypeId
    }

    override fun queryRoomsAndAvailability(
        availabilityRangeStart: LocalDate,
        availabilityRangeEnd: LocalDate,
    ): List<RoomTypeRecord> = connection.findRoomsDescriptionStockQuery(
        availabilityRangeStart,
        availabilityRangeEnd,
        clock.instant(),
    ).executeQueryWithRollback()
        .toRoomTypeRecords()
}
