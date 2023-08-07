package me.aburke.hotelbooking.scenario.room

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import me.aburke.hotelbooking.ports.repository.HotelRepository
import me.aburke.hotelbooking.ports.repository.InsertRoomType
import me.aburke.hotelbooking.ports.repository.RoomRepository
import me.aburke.hotelbooking.ports.scenario.room.AddRoomTypeDetails
import me.aburke.hotelbooking.ports.scenario.room.AddRoomTypeResult
import me.aburke.hotelbooking.stock.DatesCalculator
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

private const val RANGE_SIZE = 30
private const val BACK_POPULATE_DAYS = 3
private const val TITLE = "title"
private const val DESCRIPTION = "description"
private const val STOCK_LEVEL = 70
private const val ROOM_TYPE_ID = "room-type-id"

private val timezone = TimeZone.getTimeZone("Australia/Sydney")
private val instant = Instant.now()
private val imageUrls = listOf(
    "image-url-1",
    "image-url-2",
    "image-url-3",
)
private val dates = listOf(
    LocalDate.parse("2023-08-05"),
    LocalDate.parse("2023-08-06"),
    LocalDate.parse("2023-08-07"),
)

@ExtendWith(MockKExtension::class)
class AddRoomTypeScenarioTest {

    @MockK
    lateinit var clock: Clock

    @MockK
    lateinit var datesCalculator: DatesCalculator

    @MockK
    lateinit var hotelRepository: HotelRepository

    @MockK
    lateinit var roomRepository: RoomRepository

    private lateinit var underTest: AddRoomTypeScenario

    @BeforeEach
    fun init() {
        underTest = AddRoomTypeScenario(
            clock,
            datesCalculator,
            hotelRepository,
            roomRepository,
            RANGE_SIZE,
            BACK_POPULATE_DAYS,
        )
    }

    @Test
    fun `should insert room type into DB`() {
        every { hotelRepository.loadTimeZone() } returns timezone
        every { clock.instant() } returns instant
        every {
            datesCalculator.calculateDateRange(
                timezone,
                instant.minus(BACK_POPULATE_DAYS.toLong(), ChronoUnit.DAYS),
                RANGE_SIZE
            )
        } returns dates
        every {
            roomRepository.insertRoomType(
                InsertRoomType(
                    title = TITLE,
                    description = DESCRIPTION,
                    imageUrls = imageUrls,
                    stockLevel = STOCK_LEVEL,
                ),
                dates,
            )
        } returns ROOM_TYPE_ID

        val result = underTest.run(
            AddRoomTypeDetails(
                title = TITLE,
                description = DESCRIPTION,
                imageUrls = imageUrls,
                stockLevel = STOCK_LEVEL,
            )
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(AddRoomTypeResult(ROOM_TYPE_ID))
            s.check {
                verify(exactly = 1) {
                    hotelRepository.loadTimeZone()
                }
            }
            s.check {
                verify(exactly = 1) {
                    clock.instant()
                }
            }
            s.check {
                verify(exactly = 1) {
                    datesCalculator.calculateDateRange(
                        timezone,
                        instant.minus(BACK_POPULATE_DAYS.toLong(), ChronoUnit.DAYS),
                        RANGE_SIZE
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    roomRepository.insertRoomType(
                        InsertRoomType(
                            title = TITLE,
                            description = DESCRIPTION,
                            imageUrls = imageUrls,
                            stockLevel = STOCK_LEVEL,
                        ),
                        dates,
                    )
                }
            }
            s.check {
                confirmVerified(
                    clock,
                    datesCalculator,
                    hotelRepository,
                    roomRepository,
                )
            }
        }
    }
}
