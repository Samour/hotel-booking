package me.aburke.hotelbooking.ports.repository

import java.time.LocalDate

data class InsertRoomType(
    val title: String,
    val description: String,
    val imageUrls: List<String>,
    val stockLevel: Int,
)

interface RoomRepository {

    fun insertRoomType(roomType: InsertRoomType, populateDates: List<LocalDate>): String
}
