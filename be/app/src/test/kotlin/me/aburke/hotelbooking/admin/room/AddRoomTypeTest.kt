package me.aburke.hotelbooking.admin.room

import me.aburke.hotelbooking.*
import me.aburke.hotelbooking.client.*
import me.aburke.hotelbooking.data.StockPopulation
import me.aburke.hotelbooking.data.hotelId
import me.aburke.hotelbooking.data.hotelTimeZone
import me.aburke.hotelbooking.facade.rest.api.admin.v1.roomtype.AddRoomTypeRequest
import me.aburke.hotelbooking.facade.rest.api.admin.v1.roomtype.AddRoomTypeResponse
import me.aburke.hotelbooking.model.user.UserRole
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.sql.Connection
import java.time.Instant

private const val TITLE = "title"
private const val DESCRIPTION = "description"
private const val STOCK_LEVEL = 45

private val imageUrls = listOf(
    "image-url-1",
    "image-url-2",
    "image-url-3",
    "image-url-4",
    "image-url-5",
)

class AddRoomTypeTest {

    private lateinit var instant: Instant

    private lateinit var app: KoinApplication
    private lateinit var connection: Connection

    @BeforeEach
    fun init() {
        createApp().let {
            app = it.first
            instant = it.second
        }
        connection = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should add room type`() = app.restTest { client ->
        client.authenticateWith(UserRole.MANAGE_ROOMS)

        val response = client.addRoomType(
            AddRoomTypeRequest(
                title = TITLE,
                description = DESCRIPTION,
                imageUrls = imageUrls,
                stockLevel = STOCK_LEVEL,
            )
        )
        val responseBody = response.parseBody<AddRoomTypeResponse>()

        val allRooms = connection.loadAllRooms()
        val allStock = connection.loadAllRoomStocks()

        val expectedStock = instant.atZone(hotelTimeZone.toZoneId())
            .toLocalDate()
            .let { date ->
                (0 until StockPopulation.POPULATE_RANGE).map {
                    date.plusDays(it.toLong() - StockPopulation.BACK_POPULATE)
                }
            }.map {
                RoomStockRecord(
                    roomTypeId = responseBody?.roomTypeId ?: "",
                    date = it,
                    stockLevel = STOCK_LEVEL,
                )
            }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(201)
            s.assertThat(allRooms).containsExactly(
                RoomRecord(
                    roomTypeId = responseBody?.roomTypeId ?: "",
                    hotelId = hotelId,
                    stockLevel = STOCK_LEVEL,
                    title = TITLE,
                    description = DESCRIPTION,
                    imageUrls = imageUrls,
                )
            )
            s.assertThat(allStock).containsExactlyInAnyOrder(*expectedStock.toTypedArray())
        }
    }

    @Test
    fun `should return 403 when user does not have MANAGE_ROOMS permission`() = app.restTest { client ->
        client.authenticateAsAdmin()

        val response = client.addRoomType(
            AddRoomTypeRequest(
                title = TITLE,
                description = DESCRIPTION,
                imageUrls = imageUrls,
                stockLevel = STOCK_LEVEL,
            )
        )

        val allRooms = connection.loadAllRooms()
        val allStock = connection.loadAllRoomStocks()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(403)
            s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "title": "Forbidden",
                        "code": "FORBIDDEN",
                        "status": 403,
                        "detail": "Insufficient permissions to access resource",
                        "instance": "/api/admin/v1/room-type",
                        "extended_details": []
                    }
                """.trimIndent()
            )
            s.assertThat(allRooms).isEmpty()
            s.assertThat(allStock).isEmpty()
        }
    }
}
