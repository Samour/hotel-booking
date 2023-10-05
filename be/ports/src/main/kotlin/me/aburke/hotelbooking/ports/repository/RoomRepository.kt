package me.aburke.hotelbooking.ports.repository

import java.time.LocalDate

data class InsertRoomType(
    val title: String,
    val pricePerNight: Int,
    val description: String,
    val imageUrls: List<String>,
    val stockLevel: Int,
)

data class RoomTypeDescriptionRecord(
    val title: String,
    val pricePerNight: Int,
    val description: String,
    val imageUrls: List<String>,
)

data class RoomStockRecord(
    val date: LocalDate,
    val stockLevel: Int,
)

data class RoomTypeRecord(
    val roomTypeId: String,
    val description: RoomTypeDescriptionRecord,
    val stockLevels: List<RoomStockRecord>,
)

interface RoomRepository {

    fun insertRoomType(roomType: InsertRoomType, populateDates: List<LocalDate>): String

    fun queryRoomsAndAvailability(
        availabilityRangeStart: LocalDate,
        availabilityRangeEnd: LocalDate,
    ): List<RoomTypeRecord>
}
