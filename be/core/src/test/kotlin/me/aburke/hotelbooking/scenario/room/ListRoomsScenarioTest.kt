package me.aburke.hotelbooking.scenario.room

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import me.aburke.hotelbooking.RandomString
import me.aburke.hotelbooking.model.user.DateRange
import me.aburke.hotelbooking.ports.repository.RoomRepository
import me.aburke.hotelbooking.ports.repository.RoomStockRecord
import me.aburke.hotelbooking.ports.repository.RoomTypeDescriptionRecord
import me.aburke.hotelbooking.ports.repository.RoomTypeRecord
import me.aburke.hotelbooking.ports.scenario.room.*
import me.aburke.hotelbooking.stock.DatesCalculator
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate

private val availabilityRangeStart = LocalDate.parse("2023-08-09")
private val availabilityRangeEnd = LocalDate.parse("2023-08-18")

private val roomRecords = (1..10).map {
    RoomTypeRecord(
        roomTypeId = "room-type-$it",
        description = RoomTypeDescriptionRecord(
            title = RandomString.make(),
            pricePerNight = 125_00,
            description = RandomString.make(),
            imageUrls = (1..5).map { _ ->
                RandomString.make()
            },
        ),
        stockLevels = (1..10).map { i ->
            RoomStockRecord(
                date = LocalDate.parse("2023-08-08").plusDays(i.toLong()),
                stockLevel = i % 3,
            )
        },
    )
}
private val expandedDates = roomRecords.first().stockLevels.map { it.date }

@ExtendWith(MockKExtension::class)
class ListRoomsScenarioTest {

    @MockK
    lateinit var datesCalculator: DatesCalculator

    @MockK
    lateinit var roomRepository: RoomRepository

    @InjectMockKs
    lateinit var underTest: ListRoomsScenario

    @Test
    fun `should return room availability from repository`() {
        every {
            datesCalculator.calculateDatesInRange(
                availabilityRangeStart,
                availabilityRangeEnd,
            )
        } returns expandedDates
        every {
            roomRepository.queryRoomsAndAvailability(
                availabilityRangeStart,
                availabilityRangeEnd,
            )
        } returns roomRecords

        val result = underTest.run(
            ListRoomsDetails(
                availabilitySearchRange = DateRange(
                    rangeStart = availabilityRangeStart,
                    rangeEnd = availabilityRangeEnd,
                )
            )
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                ListRoomsResult(
                    roomRecords.map {
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
            )
            s.check {
                verify(exactly = 1) {
                    datesCalculator.calculateDatesInRange(
                        availabilityRangeStart,
                        availabilityRangeEnd,
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    roomRepository.queryRoomsAndAvailability(
                        availabilityRangeStart,
                        availabilityRangeEnd,
                    )
                }
            }
            s.check {
                confirmVerified(
                    datesCalculator,
                    roomRepository,
                )
            }
        }
    }

    @Test
    fun `should exclude rooms which do not have fully known availability`() {
        every {
            datesCalculator.calculateDatesInRange(
                availabilityRangeStart,
                availabilityRangeEnd,
            )
        } returns expandedDates
        every {
            roomRepository.queryRoomsAndAvailability(
                availabilityRangeStart,
                availabilityRangeEnd,
            )
        } returns roomRecords.mapIndexed { i, it ->
            it.takeIf { i % 3 == 0 }?.let { _ ->
                it.copy(
                    stockLevels = it.stockLevels.filterIndexed { j, _ ->
                        j % 3 == 0
                    }
                )
            } ?: it
        }

        val result = underTest.run(
            ListRoomsDetails(
                availabilitySearchRange = DateRange(
                    rangeStart = availabilityRangeStart,
                    rangeEnd = availabilityRangeEnd,
                )
            )
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                ListRoomsResult(
                    roomRecords
                        .filterIndexed { i, _ -> i % 3 != 0 }
                        .map {
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
            )
            s.check {
                verify(exactly = 1) {
                    datesCalculator.calculateDatesInRange(
                        availabilityRangeStart,
                        availabilityRangeEnd,
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    roomRepository.queryRoomsAndAvailability(
                        availabilityRangeStart,
                        availabilityRangeEnd,
                    )
                }
            }
            s.check {
                confirmVerified(
                    datesCalculator,
                    roomRepository,
                )
            }
        }
    }
}
