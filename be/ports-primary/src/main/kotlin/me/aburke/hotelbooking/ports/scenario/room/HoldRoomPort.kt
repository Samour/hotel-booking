package me.aburke.hotelbooking.ports.scenario.room

import me.aburke.hotelbooking.ports.scenario.Scenario
import java.time.Instant
import java.time.LocalDate

data class HoldRoomDetail(
    val userId: String,
    val roomTypeId: String,
    val holdStartDate: LocalDate,
    val holdEndDate: LocalDate,
) : Scenario.Details

sealed interface HoldRoomResult : Scenario.Result {

    data class RoomHoldCreated(
        val roomHoldId: String,
        val holdExpiry: Instant,
        val removedRoomHoldId: String?,
    ) : HoldRoomResult

    data object StockNotAvailable : HoldRoomResult

    data object ConcurrentHoldRequest : HoldRoomResult
}

interface HoldRoomPort : Scenario<HoldRoomDetail, HoldRoomResult>
