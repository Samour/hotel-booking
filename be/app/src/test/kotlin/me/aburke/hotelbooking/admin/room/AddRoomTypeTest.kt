package me.aburke.hotelbooking.admin.room

import me.aburke.hotelbooking.TestContext
import me.aburke.hotelbooking.assertThatJson
import me.aburke.hotelbooking.authenticateAsAdmin
import me.aburke.hotelbooking.authenticateWith
import me.aburke.hotelbooking.client.RoomRecord
import me.aburke.hotelbooking.client.RoomStockRecord
import me.aburke.hotelbooking.client.loadAllRoomStocks
import me.aburke.hotelbooking.client.loadAllRooms
import me.aburke.hotelbooking.createTestContext
import me.aburke.hotelbooking.data.StockPopulation
import me.aburke.hotelbooking.data.hotelId
import me.aburke.hotelbooking.data.hotelTimeZone
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.rest.client.api.AdminUnstableApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.model.AddRoomTypeRequest
import me.aburke.hotelbooking.restTest
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.Connection
import javax.sql.DataSource

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

    private lateinit var testContext: TestContext
    private lateinit var connection: Connection

    @BeforeEach
    fun init() {
        testContext = createTestContext()
        connection = testContext.app.koin.get<DataSource>().connection
    }

    @AfterEach
    fun cleanUp() = connection.close()

    @Test
    fun `should add room type`() = testContext.app.restTest { client, _ ->
        client.authenticateWith(UserRole.MANAGE_ROOMS)

        val response = AdminUnstableApi(client).addRoomType(
            AddRoomTypeRequest().also {
                it.title = TITLE
                it.description = DESCRIPTION
                it.imageUrls = imageUrls
                it.stockLevel = STOCK_LEVEL
            },
        )

        val allRooms = connection.loadAllRooms()
        val allStock = connection.loadAllRoomStocks()

        val expectedStock = testContext.time.atZone(hotelTimeZone.toZoneId())
            .toLocalDate()
            .let { date ->
                (0 until StockPopulation.POPULATE_RANGE).map {
                    date.plusDays(it.toLong() - StockPopulation.BACK_POPULATE)
                }
            }.map {
                RoomStockRecord(
                    roomTypeId = response.roomTypeId,
                    date = it,
                    stockLevel = STOCK_LEVEL,
                )
            }

        assertSoftly { s ->
            s.assertThat(allRooms).containsExactly(
                RoomRecord(
                    roomTypeId = response.roomTypeId,
                    hotelId = hotelId,
                    stockLevel = STOCK_LEVEL,
                    title = TITLE,
                    description = DESCRIPTION,
                    imageUrls = imageUrls,
                ),
            )
            s.assertThat(allStock).containsExactlyInAnyOrder(*expectedStock.toTypedArray())
        }
    }

    @Test
    fun `should return 403 when user does not have MANAGE_ROOMS permission`() = testContext.app.restTest { client, _ ->
        client.authenticateAsAdmin()

        val response = assertThrows<ApiException> {
            AdminUnstableApi(client).addRoomType(
                AddRoomTypeRequest().also {
                    it.title = TITLE
                    it.description = DESCRIPTION
                    it.imageUrls = imageUrls
                    it.stockLevel = STOCK_LEVEL
                },
            )
        }

        val allRooms = connection.loadAllRooms()
        val allStock = connection.loadAllRoomStocks()

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(403)
            s.assertThatJson(response.responseBody).isEqualTo(
                """
                    {
                        "title": "Forbidden",
                        "code": "FORBIDDEN",
                        "status": 403,
                        "detail": "Insufficient permissions to access resource",
                        "instance": "/api/admin/v0/room-type",
                        "extended_details": []
                    }
                """.trimIndent(),
            )
            s.assertThat(allRooms).isEmpty()
            s.assertThat(allStock).isEmpty()
        }
    }
}
