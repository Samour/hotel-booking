package me.aburke.hotelbooking.repository.postgres

import java.sql.Connection
import java.time.LocalDate
import java.util.UUID.randomUUID

fun Connection.insertRoomTypeQuery(hotelId: String, roomTypeId: String, stockLevel: Int) =
    prepareStatement(
        """
            insert into room_type(room_type_id, hotel_id, stock_level)
            values (?, ?, ?)
        """.trimIndent()
    ).apply {
        setString(1, roomTypeId)
        setString(2, hotelId)
        setInt(3, stockLevel)
    }

fun Connection.insertRoomTypeDescriptionQuery(
    roomTypeDescriptionId: String,
    roomTypeId: String,
    title: String,
    description: String,
    imageUrls: List<String>
) = prepareStatement(
    """
        insert into room_type_description(room_type_description_id, room_type_id, title, description, image_urls)
        values (?, ?, ?, ?, ?)
    """.trimIndent()
).apply {
    setString(1, roomTypeDescriptionId)
    setString(2, roomTypeId)
    setString(3, title)
    setString(4, description)
    setArray(5, connection.createArrayOf("varchar", imageUrls.toTypedArray()))
}

fun Connection.insertRoomStockQuery(roomTypeId: String, stockLevel: Int, dates: List<LocalDate>) =
    prepareStatement(
        """
            insert into room_stock(room_stock_id, room_type_id, date, stock_level)
            values (?, ?, ?, ?)
        """.trimIndent()
    ).apply {
        dates.forEach {
            setString(1, randomUUID().toString())
            setString(2, roomTypeId)
            setString(3, it.toString())
            setInt(4, stockLevel)
            addBatch()
        }
    }
