package me.aburke.hotelbooking.stubs.repository

import me.aburke.hotelbooking.ports.repository.InsertRoomType
import me.aburke.hotelbooking.ports.repository.RoomRepository
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
}
