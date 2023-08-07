package me.aburke.hotelbooking.scenario.room

import me.aburke.hotelbooking.ports.repository.HotelRepository
import me.aburke.hotelbooking.ports.repository.InsertRoomType
import me.aburke.hotelbooking.ports.repository.RoomRepository
import me.aburke.hotelbooking.ports.scenario.room.AddRoomTypeDetails
import me.aburke.hotelbooking.ports.scenario.room.AddRoomTypePort
import me.aburke.hotelbooking.ports.scenario.room.AddRoomTypeResult
import me.aburke.hotelbooking.stock.DatesCalculator
import java.time.Clock
import java.time.temporal.ChronoUnit

class AddRoomTypeScenario(
    private val clock: Clock,
    private val datesCalculator: DatesCalculator,
    private val hotelRepository: HotelRepository,
    private val roomRepository: RoomRepository,
    private val populateRoomRange: Int,
    private val backPopulateDays: Int,
) : AddRoomTypePort {

    override fun run(details: AddRoomTypeDetails): AddRoomTypeResult =
        AddRoomTypeResult(
            roomRepository.insertRoomType(
                InsertRoomType(
                    title = details.title,
                    description = details.description,
                    imageUrls = details.imageUrls,
                    stockLevel = details.stockLevel,
                ),
                datesCalculator.calculateDateRange(
                    hotelRepository.loadTimeZone(),
                    clock.instant().minus(backPopulateDays.toLong(), ChronoUnit.DAYS),
                    populateRoomRange,
                ),
            )
        )
}
