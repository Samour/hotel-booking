package me.aburke.hotelbooking.scenario.room

import me.aburke.hotelbooking.lock.FastFailLock
import me.aburke.hotelbooking.ports.repository.CreateRoomHoldResult
import me.aburke.hotelbooking.ports.repository.RoomHoldRepository
import me.aburke.hotelbooking.ports.scenario.room.HoldRoomDetail
import me.aburke.hotelbooking.ports.scenario.room.HoldRoomPort
import me.aburke.hotelbooking.ports.scenario.room.HoldRoomResult
import java.time.Clock
import java.time.Duration

class HoldRoomScenario(
    private val maxConcurrentHolds: Int,
    private val roomHoldDuration: Duration,
    private val clock: Clock,
    private val fastFailLock: FastFailLock,
    private val roomHoldRepository: RoomHoldRepository,
) : HoldRoomPort {

    override fun run(details: HoldRoomDetail): HoldRoomResult = fastFailLock.execute(
        details.userId,
        HoldRoomResult.ConcurrentHoldRequest,
    ) {
        val holdToRemove = pickHoldToRemove(details.userId, details.roomTypeId)

        val roomHoldExpiry = clock.instant().plus(roomHoldDuration)
        val roomHoldResult = roomHoldRepository.createRoomHold(
            userId = details.userId,
            roomTypeId = details.roomTypeId,
            roomHoldExpiry = roomHoldExpiry,
            holdStartDate = details.holdStartDate,
            holdEndDate = details.holdEndDate,
            holdIdToRemove = holdToRemove,
        )

        when (roomHoldResult) {
            is CreateRoomHoldResult.RoomHoldCreated -> HoldRoomResult.RoomHoldCreated(
                roomHoldId = roomHoldResult.roomHoldId,
                holdExpiry = roomHoldExpiry,
                removedRoomHoldId = holdToRemove,
            )

            is CreateRoomHoldResult.StockNotAvailable -> HoldRoomResult.StockNotAvailable
        }
    }

    private fun pickHoldToRemove(userId: String, roomTypeId: String): String? {
        val existingHolds = roomHoldRepository.findHoldsForUser(userId)

        val holdOnSameRoom = existingHolds.firstOrNull { it.roomTypeId == roomTypeId }
            ?.roomHoldId
        if (holdOnSameRoom != null) {
            return holdOnSameRoom
        }

        return if (existingHolds.size >= maxConcurrentHolds) {
            existingHolds.minBy { it.holdExpiry }.roomHoldId
        } else {
            null
        }
    }
}
