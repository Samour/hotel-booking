package me.aburke.hotelbooking.stubs.repository

import me.aburke.hotelbooking.ports.repository.InsertRoomType
import me.aburke.hotelbooking.ports.repository.RoomRepository
import me.aburke.hotelbooking.ports.repository.RoomStockRecord
import me.aburke.hotelbooking.ports.repository.RoomTypeDescriptionRecord
import me.aburke.hotelbooking.ports.repository.RoomTypeRecord
import me.aburke.hotelbooking.stock.DatesCalculator
import java.time.LocalDate
import java.util.UUID

class RoomRepositoryStub : RoomRepository {

    val rooms = mutableMapOf<String, InsertRoomType>()
    val stock = mutableSetOf<Pair<String, LocalDate>>()

    override fun insertRoomType(roomType: InsertRoomType, populateDates: List<LocalDate>): String {
        val roomId = UUID.randomUUID().toString()
        rooms[roomId] = roomType
        populateDates.forEach { stock.add(roomId to it) }

        return roomId
    }

    override fun queryRoomsAndAvailability(
        availabilityRangeStart: LocalDate,
        availabilityRangeEnd: LocalDate
    ): List<RoomTypeRecord> {
        if (availabilityRangeEnd < availabilityRangeStart) {
            return emptyList()
        }

        val dates = DatesCalculator().calculateDatesInRange(availabilityRangeStart, availabilityRangeEnd)

        return rooms.entries.map { (id, room) ->
            RoomTypeRecord(
                roomTypeId = id,
                description = RoomTypeDescriptionRecord(
                    title = room.title,
                    description = room.description,
                    imageUrls = room.imageUrls,
                ),
                stockLevels = dates.filter { stock.contains(id to it) }
                    .map {
                        RoomStockRecord(
                            date = it,
                            stockLevel = room.stockLevel,
                        )
                    },
            )
        }
    }
}
