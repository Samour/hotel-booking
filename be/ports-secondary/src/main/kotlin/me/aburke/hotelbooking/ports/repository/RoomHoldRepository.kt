package me.aburke.hotelbooking.ports.repository

import java.time.Instant
import java.time.LocalDate

data class RoomHold(
    val roomHoldId: String,
    val userId: String,
    val roomTypeId: String,
    val holdExpiry: Instant,
)

sealed interface CreateRoomHoldResult {
    data class RoomHoldCreated(
        val roomHoldId: String,
    ) : CreateRoomHoldResult

    data object StockNotAvailable : CreateRoomHoldResult
}

interface RoomHoldRepository {

    fun findHoldsForUser(userId: String): List<RoomHold>

    fun createRoomHold(
        userId: String,
        roomTypeId: String,
        roomHoldExpiry: Instant,
        holdStartDate: LocalDate,
        holdEndDate: LocalDate,
        holdIdToRemove: String?,
    ): CreateRoomHoldResult
}
