package me.aburke.hotelbooking.stubs.repository

import me.aburke.hotelbooking.ports.repository.InsertRoomType
import me.aburke.hotelbooking.ports.repository.RoomRepository
import me.aburke.hotelbooking.ports.repository.RoomStockRecord
import me.aburke.hotelbooking.ports.repository.RoomTypeDescriptionRecord
import me.aburke.hotelbooking.ports.repository.RoomTypeRecord
import java.time.LocalDate
import java.util.UUID

class RoomRepositoryStub : RoomRepository {

    val rooms = mutableMapOf<String, InsertRoomType>()
    val stock = mutableMapOf<String, MutableMap<LocalDate, Int>>()

    override fun insertRoomType(roomType: InsertRoomType, populateDates: List<LocalDate>): String {
        val roomId = UUID.randomUUID().toString()
        rooms[roomId] = roomType
        stock[roomId] = populateDates.associateWith { roomType.stockLevel }.toMutableMap()

        return roomId
    }

    override fun queryRoomsAndAvailability(
        availabilityRangeStart: LocalDate,
        availabilityRangeEnd: LocalDate
    ): List<RoomTypeRecord> {
        if (availabilityRangeEnd < availabilityRangeStart) {
            return emptyList()
        }

        return rooms.entries.map { (id, room) ->
            RoomTypeRecord(
                roomTypeId = id,
                description = RoomTypeDescriptionRecord(
                    title = room.title,
                    pricePerNight = room.pricePerNight,
                    description = room.description,
                    imageUrls = room.imageUrls,
                ),
                stockLevels = stock[id]?.entries?.filter { (d, _) -> d in availabilityRangeStart..availabilityRangeEnd }
                    ?.map { (d, s) ->
                        RoomStockRecord(
                            date = d,
                            stockLevel = s,
                        )
                    } ?: emptyList()
            )
        }
    }
}
