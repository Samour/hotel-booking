package me.aburke.hotelbooking.repository.postgres

import java.sql.Connection
import java.time.Instant
import java.time.LocalDate
import java.util.UUID.randomUUID

// Read Queries

// TODO Add a condition that excludes holds for the current user
//  (ie the user can see rooms that they have a hold on)
fun Connection.findRoomsDescriptionStockQuery(rangeStart: LocalDate, rangeEnd: LocalDate, now: Instant) =
    prepareStatement(
        """
            select r.room_type_id,
               rd.title,
               rd.price_per_night,
               rd.description,
               rd.image_urls,
               rs.date,
               rs.stock_level - COALESCE(holds.held_stock, 0) as visible_stock
            from room_stock rs
            join room_type r on r.room_type_id = rs.room_type_id
            join room_type_description rd on rd.room_type_id = r.room_type_id
            left outer join (
                select rsh.room_stock_id, count(rsh.room_stock_hold_id) as held_stock
                from room_stock_hold rsh
                join room_hold rh on rh.room_hold_id = rsh.room_hold_id
                where rh.hold_expiry > ?
                group by rsh.room_stock_id
            ) holds on holds.room_stock_id = rs.room_stock_id
            where rs.date >= ?
                and rs.date <= ?
            order by r.room_type_id, rs.date
        """.trimIndent(),
    ).apply {
        setString(1, now.toString())
        setString(2, rangeStart.toString())
        setString(3, rangeEnd.toString())
    }

// Write queries

fun Connection.insertRoomTypeQuery(hotelId: String, roomTypeId: String, stockLevel: Int) =
    prepareStatement(
        """
            insert into room_type(room_type_id, hotel_id, stock_level)
            values (?, ?, ?)
        """.trimIndent(),
    ).apply {
        setString(1, roomTypeId)
        setString(2, hotelId)
        setInt(3, stockLevel)
    }

fun Connection.insertRoomTypeDescriptionQuery(
    roomTypeDescriptionId: String,
    roomTypeId: String,
    title: String,
    pricePerNight: Int,
    description: String,
    imageUrls: List<String>,
) = prepareStatement(
    """
        insert into room_type_description(room_type_description_id, room_type_id, title, price_per_night, description, 
            image_urls)
        values (?, ?, ?, ?, ?, ?)
    """.trimIndent(),
).apply {
    setString(1, roomTypeDescriptionId)
    setString(2, roomTypeId)
    setString(3, title)
    setInt(4, pricePerNight)
    setString(5, description)
    setArray(6, connection.createArrayOf("varchar", imageUrls.toTypedArray()))
}

fun Connection.insertRoomStockQuery(roomTypeId: String, stockLevel: Int, dates: List<LocalDate>) =
    prepareStatement(
        """
            insert into room_stock(room_stock_id, room_type_id, date, stock_level)
            values (?, ?, ?, ?)
        """.trimIndent(),
    ).apply {
        dates.forEach {
            setString(1, randomUUID().toString())
            setString(2, roomTypeId)
            setString(3, it.toString())
            setInt(4, stockLevel)
            addBatch()
        }
    }
