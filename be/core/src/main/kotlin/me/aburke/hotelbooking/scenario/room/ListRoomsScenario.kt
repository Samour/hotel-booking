package me.aburke.hotelbooking.scenario.room

import me.aburke.hotelbooking.ports.repository.RoomRepository
import me.aburke.hotelbooking.ports.scenario.room.*
import me.aburke.hotelbooking.stock.DatesCalculator

class ListRoomsScenario(
    private val datesCalculator: DatesCalculator,
    private val roomRepository: RoomRepository,
) : ListRoomsPort {

    override fun run(details: ListRoomsDetails): ListRoomsResult {
        val dates = datesCalculator.calculateDatesInRange(
            details.availabilitySearchRange.rangeStart,
            details.availabilitySearchRange.rangeEnd,
        ).toSet()

        return ListRoomsResult(
            roomRepository.queryRoomsAndAvailability(
                details.availabilitySearchRange.rangeStart,
                details.availabilitySearchRange.rangeEnd,
            ).filter { r ->
                r.stockLevels.map { it.date }.toSet() == dates
            }.map {
                RoomTypeInfo(
                    roomTypeId = it.roomTypeId,
                    description = RoomDescription(
                        title = it.description.title,
                        pricePerNight = it.description.pricePerNight,
                        description = it.description.description,
                        imageUrls = it.description.imageUrls,
                    ),
                    roomAvailability = it.stockLevels.map { sl ->
                        RoomAvailability(
                            date = sl.date,
                            available = sl.stockLevel > 0,
                        )
                    },
                )
            }
        )
    }
}
