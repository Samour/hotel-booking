package me.aburke.hotelbooking.facade.rest.api.customer.roomtype

import io.javalin.http.Context
import io.javalin.http.HttpStatus
import me.aburke.hotelbooking.facade.rest.authentication.userSessionOptional
import me.aburke.hotelbooking.facade.rest.responses.ProblemAdditionalDetail
import me.aburke.hotelbooking.facade.rest.responses.ProblemResponse
import me.aburke.hotelbooking.facade.rest.responses.problemJson
import me.aburke.hotelbooking.model.date.DateRange
import me.aburke.hotelbooking.ports.scenario.room.ListRoomsDetails
import me.aburke.hotelbooking.ports.scenario.room.ListRoomsPort
import me.aburke.hotelbooking.ports.scenario.room.ListRoomsResult
import me.aburke.hotelbooking.ports.scenario.room.RoomAvailability
import me.aburke.hotelbooking.ports.scenario.room.RoomTypeInfo
import java.time.DateTimeException
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

    fun handle(ctx: Context, availabilityRangeStart: String?, availabilityRangeEnd: String?) {
        val rangeStart = availabilityRangeStart?.localDateOrNull()
        val rangeEnd = availabilityRangeEnd?.localDateOrNull()

        if (rangeStart == null || rangeEnd == null) {
            ctx.invalidParametersResponse(rangeStart, rangeEnd)
            return
        }

        val result = listRoomsPort.run(
            ListRoomsDetails(
                currentUserId = ctx.userSessionOptional()?.userId,
                availabilitySearchRange = DateRange(
                    rangeStart = rangeStart,
                    rangeEnd = rangeEnd,
                ),
            ),
        )

        ctx.json(result.toResponse())
    }
}

private fun String.localDateOrNull(): LocalDate? = try {
    LocalDate.parse(this)
} catch (e: DateTimeException) {
    null
}

private fun Context.invalidParametersResponse(availabilityRangeStart: LocalDate?, availabilityRangeEnd: LocalDate?) {
    problemJson(
        ProblemResponse(
            title = "Invalid Parameters",
            code = HttpStatus.BAD_REQUEST.name,
            status = HttpStatus.BAD_REQUEST.code,
            detail = "URL parameters are badly formed",
            instance = path(),
            extendedDetails = listOfNotNull(
                ProblemAdditionalDetail(
                    code = "INVALID_FORMAT_DATE",
                    detail = "availability_range_start",
                ).takeIf { availabilityRangeStart == null },
                ProblemAdditionalDetail(
                    code = "INVALID_FORMAT_DATE",
                    detail = "availability_range_end",
                ).takeIf { availabilityRangeEnd == null },
            ),
        ),
    )
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
