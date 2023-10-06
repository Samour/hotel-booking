package me.aburke.hotelbooking.ports.scenario.room

import me.aburke.hotelbooking.model.date.DateRange
import me.aburke.hotelbooking.ports.scenario.Scenario
import java.time.LocalDate

data class ListRoomsDetails(
    val currentUserId: String?,
    val availabilitySearchRange: DateRange,
) : Scenario.Details

data class RoomDescription(
    val title: String,
    val pricePerNight: Int,
    val description: String,
    val imageUrls: List<String>,
)

data class RoomAvailability(
    val date: LocalDate,
    val available: Boolean,
)

data class RoomTypeInfo(
    val roomTypeId: String,
    val description: RoomDescription,
    val roomAvailability: List<RoomAvailability>,
)

data class ListRoomsResult(
    val rooms: List<RoomTypeInfo>,
) : Scenario.Result

interface ListRoomsPort : Scenario<ListRoomsDetails, ListRoomsResult>
