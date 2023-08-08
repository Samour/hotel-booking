package me.aburke.hotelbooking.repository.postgres

import me.aburke.hotelbooking.ports.repository.RoomStockRecord
import me.aburke.hotelbooking.ports.repository.RoomTypeDescriptionRecord
import me.aburke.hotelbooking.ports.repository.RoomTypeRecord
import java.sql.ResultSet
import java.time.LocalDate

fun ResultSet.toRoomTypeRecords(): List<RoomTypeRecord> {
    val results = mutableListOf<RoomTypeRecord>()
    var aggregate: RoomTypeRecord? = null
    while (next()) {
        val roomTypeId = getString("room_type_id")
        aggregate?.takeIf { it.roomTypeId != roomTypeId }
            ?.let { results.add(it) }
        aggregate = aggregate?.takeIf { it.roomTypeId == roomTypeId }?.let {
            it.copy(
                stockLevels = it.stockLevels + listOf(toRoomStockRecord())
            )
        } ?: toRoomTypeRecord()
    }
    aggregate?.let { results.add(it) }

    return results
}

private fun ResultSet.toRoomTypeRecord() = RoomTypeRecord(
    roomTypeId = getString("room_type_id"),
    description = RoomTypeDescriptionRecord(
        title = getString("title"),
        description = getString("description"),
        imageUrls = (getArray("image_urls").array as Array<String>).toList(),
    ),
    stockLevels = listOf(toRoomStockRecord()),
)

private fun ResultSet.toRoomStockRecord() = RoomStockRecord(
    date = LocalDate.parse(getString("date")),
    stockLevel = getInt("visible_stock"),
)
