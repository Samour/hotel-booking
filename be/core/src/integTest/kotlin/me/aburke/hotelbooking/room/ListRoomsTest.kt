package me.aburke.hotelbooking.room

import me.aburke.hotelbooking.RandomString
import me.aburke.hotelbooking.model.user.DateRange
import me.aburke.hotelbooking.ports.repository.InsertRoomType
import me.aburke.hotelbooking.ports.scenario.room.ListRoomsDetails
import me.aburke.hotelbooking.ports.scenario.room.ListRoomsPort
import me.aburke.hotelbooking.ports.scenario.room.ListRoomsResult
import me.aburke.hotelbooking.ports.scenario.room.RoomAvailability
import me.aburke.hotelbooking.ports.scenario.room.RoomDescription
import me.aburke.hotelbooking.ports.scenario.room.RoomTypeInfo
import me.aburke.hotelbooking.stock.DatesCalculator
import me.aburke.hotelbooking.stubs.Stubs
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.time.LocalDate

private val availabilityRangeStart = LocalDate.parse("2023-08-09")
private val availabilityRangeEnd = LocalDate.parse("2023-08-18")

private val roomRecords = (1..10).map {
    RoomTypeInfo(
        roomTypeId = "room-type-$it",
        description = RoomDescription(
            title = RandomString.make(),
            description = RandomString.make(),
            imageUrls = (1..5).map { _ ->
                RandomString.make()
            },
        ),
        roomAvailability = (1..10).map { i ->
            RoomAvailability(
                date = LocalDate.parse("2023-08-08").plusDays(i.toLong()),
                available = true, // TODO Need to add some variance here
            )
        },
    )
}

class ListRoomsTest {

    private val stubs = Stubs()

    private lateinit var app: KoinApplication
    private lateinit var underTest: ListRoomsPort

    @BeforeEach
    fun init() {
        app = stubs.make()
        underTest = app.koin.get()

        val dateRange = app.koin.get<DatesCalculator>().calculateDatesInRange(availabilityRangeStart, availabilityRangeEnd)
        stubs.roomRepository.rooms.putAll(
            roomRecords.map {
                it.roomTypeId to InsertRoomType(
                    title = it.description.title,
                    description = it.description.description,
                    imageUrls = it.description.imageUrls,
                    stockLevel = 5,
                )
            }
        )
        stubs.roomRepository.stock.addAll(
            roomRecords.flatMap { room ->
                dateRange.map { room.roomTypeId to it }
            }
        )
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should return room availability from repository`() {
        val result = underTest.run(
            ListRoomsDetails(
                availabilitySearchRange = DateRange(
                    rangeStart = availabilityRangeStart,
                    rangeEnd = availabilityRangeEnd,
                )
            )
        )

        assertThat(result).isEqualTo(ListRoomsResult(roomRecords))
    }

    @Test
    fun `should exclude rooms which do not have fully known availability`() {
        stubs.roomRepository.stock.removeAll(
            setOf(
                roomRecords[2].roomTypeId to LocalDate.parse("2023-08-12"),
                roomRecords[2].roomTypeId to LocalDate.parse("2023-08-13"),
                roomRecords[5].roomTypeId to LocalDate.parse("2023-08-12"),
            )
        )

        val result = underTest.run(
            ListRoomsDetails(
                availabilitySearchRange = DateRange(
                    rangeStart = availabilityRangeStart,
                    rangeEnd = availabilityRangeEnd,
                )
            )
        )

        assertThat(result).isEqualTo(
            ListRoomsResult(
                roomRecords.filter {
                    it.roomTypeId != roomRecords[2].roomTypeId
                        && it.roomTypeId != roomRecords[5].roomTypeId
                }
            )
        )
    }
}
