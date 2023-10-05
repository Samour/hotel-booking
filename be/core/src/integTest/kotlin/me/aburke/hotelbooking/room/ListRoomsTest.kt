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
            pricePerNight = 150_00,
            description = RandomString.make(),
            imageUrls = (1..5).map { _ ->
                RandomString.make()
            },
        ),
        roomAvailability = (1..10).map { i ->
            RoomAvailability(
                date = LocalDate.parse("2023-08-08").plusDays(i.toLong()),
                available = it % 3 != 0,
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

        stubs.roomRepository.rooms.putAll(
            roomRecords.map {
                it.roomTypeId to InsertRoomType(
                    title = it.description.title,
                    description = it.description.description,
                    imageUrls = it.description.imageUrls,
                    pricePerNight = 150_00,
                    stockLevel = 5,
                )
            }
        )
        stubs.roomRepository.stock.putAll(
            roomRecords.associate { room ->
                room.roomTypeId to room.roomAvailability.associate { a ->
                    a.date to ((5).takeIf { a.available } ?: 0)
                }.toMutableMap()
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
        stubs.roomRepository.stock[roomRecords[2].roomTypeId]?.remove(LocalDate.parse("2023-08-12"))
        stubs.roomRepository.stock[roomRecords[2].roomTypeId]?.remove(LocalDate.parse("2023-08-13"))
        stubs.roomRepository.stock[roomRecords[5].roomTypeId]?.remove(LocalDate.parse("2023-08-12"))

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
