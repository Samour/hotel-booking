package me.aburke.hotelbooking.room

import me.aburke.hotelbooking.ports.repository.InsertRoomType
import me.aburke.hotelbooking.scenario.room.AddRoomTypeDetails
import me.aburke.hotelbooking.scenario.room.AddRoomTypeScenario
import me.aburke.hotelbooking.stubs.Stubs
import me.aburke.hotelbooking.stubs.repository.hotelTimeZone
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication

private const val TITLE = "title"
private const val DESCRIPTION = "description"
private const val STOCK_LEVEL = 70

private val imageUrls = listOf(
    "image-url-1",
    "image-url-2",
    "image-url-3",
)

class AddRoomTypeTest {

    private val stubs = Stubs()

    private lateinit var app: KoinApplication
    private lateinit var underTest: AddRoomTypeScenario

    @BeforeEach
    fun init() {
        app = stubs.make()
        underTest = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should insert room type`() {
        val result = underTest.run(
            AddRoomTypeDetails(
                title = TITLE,
                description = DESCRIPTION,
                imageUrls = imageUrls,
                stockLevel = STOCK_LEVEL,
            )
        )

        val firstDate = stubs.time.atZone(hotelTimeZone.toZoneId())
            .toLocalDate()
            .minusDays(5)

        assertSoftly { s ->
            s.assertThat(stubs.roomRepository.rooms).isEqualTo(
                mapOf(
                    result.roomTypeId to InsertRoomType(
                        title = TITLE,
                        description = DESCRIPTION,
                        imageUrls = imageUrls,
                        stockLevel = STOCK_LEVEL,
                    )
                )
            )
            s.assertThat(stubs.roomRepository.stock).isEqualTo(
                (0..44).map {
                    result.roomTypeId to firstDate.plusDays(it.toLong())
                }.toSet()
            )
        }
    }
}
