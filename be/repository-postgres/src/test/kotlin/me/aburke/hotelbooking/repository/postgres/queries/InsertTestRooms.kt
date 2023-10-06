package me.aburke.hotelbooking.repository.postgres.queries

import me.aburke.hotelbooking.ports.repository.RoomTypeRecord
import me.aburke.hotelbooking.repository.postgres.hotelId
import org.postgresql.util.PSQLException
import java.sql.Connection

fun Connection.insertTestRooms(rooms: List<RoomTypeRecord>) {
    val insertRoomTypeQuery = prepareStatement(
        """
            insert into room_type(room_type_id, hotel_id, stock_level)
            values (?, ?, ?);
        """.trimIndent(),
    ).apply {
        rooms.forEach {
            setString(1, it.roomTypeId)
            setString(2, hotelId)
            setInt(3, 5)
            addBatch()
        }
    }

    val insertRoomTypeDescription = prepareStatement(
        """
            insert into room_type_description(room_type_description_id, room_type_id, title, price_per_night,
                                              description, image_urls)
            values (?, ?, ?, ?, ?, ?);
        """.trimIndent(),
    ).apply {
        rooms.forEach {
            setString(1, "${it.roomTypeId}-description")
            setString(2, it.roomTypeId)
            setString(3, it.description.title)
            setInt(4, it.description.pricePerNight)
            setString(5, it.description.description)
            setArray(6, createArrayOf("varchar", it.description.imageUrls.toTypedArray()))
            addBatch()
        }
    }

    val insertRoomStock = prepareStatement(
        """
            insert into room_stock(room_stock_id, room_type_id, date, stock_level)
            values (?, ?, ?, ?);
        """.trimIndent(),
    ).apply {
        rooms.forEach { room ->
            room.stockLevels.forEachIndexed { i, stock ->
                setString(1, "${room.roomTypeId}-stock-id-${i + 1}")
                setString(2, room.roomTypeId)
                setString(3, "${stock.date}")
                setInt(4, stock.stockLevel)
                addBatch()
            }
        }
    }

    try {
        insertRoomTypeQuery.executeBatch().also {
            assert(it.sum() == rooms.size)
        }
        insertRoomTypeDescription.executeBatch().also {
            assert(it.sum() == rooms.size)
        }
        insertRoomStock.executeBatch().also {
            assert(it.sum() == rooms.sumOf { sl -> sl.stockLevels.size })
        }
        commit()
    } catch (e: PSQLException) {
        rollback()
        throw e
    } catch (e: AssertionError) {
        rollback()
        throw e
    }
}
