package me.aburke.hotelbooking.stubs.repository

import me.aburke.hotelbooking.ports.repository.CreateRoomHoldResult
import me.aburke.hotelbooking.ports.repository.RoomHold
import me.aburke.hotelbooking.ports.repository.RoomHoldRepository
import java.time.Instant
import java.time.LocalDate
import java.util.UUID.randomUUID

class RoomHoldRepositoryStub(
    private val roomRepository: RoomRepositoryStub,
) : RoomHoldRepository {

    private val holds = mutableMapOf<String, MutableList<RoomHold>>()
    private val currentHoldsCount = mutableMapOf<Pair<String, LocalDate>, Int>()

    override fun findHoldsForUser(userId: String): List<RoomHold> =
        holds[userId] ?: emptyList()

    override fun createRoomHold(
        userId: String,
        roomTypeId: String,
        roomHoldExpiry: Instant,
        holdStartDate: LocalDate,
        holdEndDate: LocalDate,
        holdIdToRemove: String?,
    ): CreateRoomHoldResult {
        val roomStock = roomRepository.stock[roomTypeId] ?: emptyMap()

        val holdDates = mutableListOf<LocalDate>()
        var d = holdStartDate
        while (!d.isAfter(holdEndDate)) {
            val availableStock = roomStock.getOrDefault(d, 0) -
                currentHoldsCount.getOrDefault(roomTypeId to d, 0)
            if (availableStock < 1) {
                return CreateRoomHoldResult.StockNotAvailable
            }
            holdDates.add(d)
            d = d.plusDays(1)
        }

        holdDates.forEach {
            currentHoldsCount[roomTypeId to it] = currentHoldsCount.getOrDefault(roomTypeId to it, 0) + 1
        }
        return CreateRoomHoldResult.RoomHoldCreated(randomUUID().toString()).also {
            holds.getOrPut(userId, ::mutableListOf).add(
                RoomHold(
                    roomHoldId = it.roomHoldId,
                    userId = userId,
                    roomTypeId = roomTypeId,
                    holdExpiry = roomHoldExpiry,
                ),
            )
        }
    }
}
