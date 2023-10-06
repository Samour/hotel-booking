package me.aburke.hotelbooking.facade.rest.api.customer.v1.roomtype

import io.javalin.http.Context
import me.aburke.hotelbooking.facade.rest.authentication.userSessionOptional
import me.aburke.hotelbooking.model.date.DateRange
import me.aburke.hotelbooking.ports.scenario.room.ListRoomsDetails
import me.aburke.hotelbooking.ports.scenario.room.ListRoomsPort
import me.aburke.hotelbooking.ports.scenario.room.ListRoomsResult
import me.aburke.hotelbooking.ports.scenario.room.RoomAvailability
import me.aburke.hotelbooking.ports.scenario.room.RoomTypeInfo
import java.time.LocalDate

data class FetchRoomsAvailabilityResponse(
    val roomTypes: List<RoomTypeResponse>,
)

data class RoomTypeResponse(
    val roomTypeId: String,
    val description: RoomDescriptionResponse,
    val roomAvailability: List<RoomAvailabilityResponse>,
)

data class RoomDescriptionResponse(
    val title: String,
    val pricePerNight: Int,
    val description: String,
    val imageUrls: List<String>,
)

data class RoomAvailabilityResponse(
    val date: String,
    val available: Boolean,
)

class FetchRoomsAvailabilityHandler(private val listRoomsPort: ListRoomsPort) {

    fun handle(ctx: Context, availabilityRangeStart: String, availabilityRangeEnd: String) {
        val result = listRoomsPort.run(
            ListRoomsDetails(
                currentUserId = ctx.userSessionOptional()?.userId,
                availabilitySearchRange = DateRange(
                    rangeStart = LocalDate.parse(availabilityRangeStart),
                    rangeEnd = LocalDate.parse(availabilityRangeEnd),
                ),
            ),
        )

        ctx.json(result.toResponse())
    }
}

private fun ListRoomsResult.toResponse() = FetchRoomsAvailabilityResponse(
    roomTypes = rooms.map { it.toResponse() },
)

private fun RoomTypeInfo.toResponse() = RoomTypeResponse(
    roomTypeId = roomTypeId,
    description = RoomDescriptionResponse(
        title = description.title,
        pricePerNight = description.pricePerNight,
        description = description.description,
        imageUrls = description.imageUrls,
    ),
    roomAvailability = roomAvailability.map { it.toResponse() },
)

private fun RoomAvailability.toResponse() = RoomAvailabilityResponse(
    date = "$date",
    available = available,
)
