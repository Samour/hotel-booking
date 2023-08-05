package me.aburke.hotelbooking.repository.postgres

import me.aburke.hotelbooking.migrations.postgres.executeScript
import me.aburke.hotelbooking.ports.repository.InsertRoomType
import me.aburke.hotelbooking.ports.repository.RoomRepository
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.sql.Connection
import java.time.LocalDate

private const val TITLE = "title"
private const val DESCRIPTION = "description"
private const val STOCK_LEVEL = 25

private val imageUrls = listOf(
    "image-url-1",
    "image-url-2",
    "image-url-3",
    "image-url-4",
)
private val stockDates = listOf(
    LocalDate.parse("2023-08-05"),
    LocalDate.parse("2023-08-06"),
    LocalDate.parse("2023-08-07"),
    LocalDate.parse("2023-08-08"),
    LocalDate.parse("2023-08-09"),
)

private data class RoomRecord(
    val roomTypeId: String,
    val hotelId: String,
    val stockLevel: Int,
    val title: String,
    val description: String,
    val imageUrls: Set<String>,
)

private data class RoomStockRecord(
    val roomTypeId: String,
    val date: LocalDate,
    val morningStock: Int,
    val afternoonStock: Int,
)

class PostgresRoomRepositoryTest {

    private lateinit var app: KoinApplication
    private lateinit var connection: Connection

    private lateinit var underTest: RoomRepository

    @BeforeEach
    fun init() {
        app = createApp()
        connection = app.koin.get()
        app.koin.get<Connection>().executeScript("populate_hotel.sql")
        underTest = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should insert room type and stock records`() {
        val result = underTest.insertRoomType(
            InsertRoomType(
                title = TITLE,
                description = DESCRIPTION,
                imageUrls = imageUrls,
                stockLevel = STOCK_LEVEL,
            ),
            stockDates,
        )

        val allRooms = loadAllRooms()
        val allStock = loadAllRoomStocks()

        assertSoftly { s ->
            s.assertThat(allRooms).containsExactly(
                RoomRecord(
                    roomTypeId = result,
                    hotelId = hotelId,
                    stockLevel = STOCK_LEVEL,
                    title = TITLE,
                    description = DESCRIPTION,
                    imageUrls = imageUrls.toSet(),
                )
            )
            s.assertThat(allStock).containsExactlyInAnyOrder(
                *stockDates.map {
                    RoomStockRecord(
                        roomTypeId = result,
                        date = it,
                        morningStock = STOCK_LEVEL,
                        afternoonStock = STOCK_LEVEL,
                    )
                }.toTypedArray()
            )
        }
    }

    @Test
    fun `should insert room type when no image URLs provided`() {
        val result = underTest.insertRoomType(
            InsertRoomType(
                title = TITLE,
                description = DESCRIPTION,
                imageUrls = emptyList(),
                stockLevel = STOCK_LEVEL,
            ),
            stockDates,
        )

        val allRooms = loadAllRooms()
        val allStock = loadAllRoomStocks()

        assertSoftly { s ->
            s.assertThat(allRooms).containsExactly(
                RoomRecord(
                    roomTypeId = result,
                    hotelId = hotelId,
                    stockLevel = STOCK_LEVEL,
                    title = TITLE,
                    description = DESCRIPTION,
                    imageUrls = emptySet(),
                )
            )
            s.assertThat(allStock).containsExactlyInAnyOrder(
                *stockDates.map {
                    RoomStockRecord(
                        roomTypeId = result,
                        date = it,
                        morningStock = STOCK_LEVEL,
                        afternoonStock = STOCK_LEVEL,
                    )
                }.toTypedArray()
            )
        }
    }

    @Test
    fun `should insert only room type when stock dates not provided`() {
        val result = underTest.insertRoomType(
            InsertRoomType(
                title = TITLE,
                description = DESCRIPTION,
                imageUrls = imageUrls,
                stockLevel = STOCK_LEVEL,
            ),
            emptyList(),
        )

        val allRooms = loadAllRooms()
        val allStock = loadAllRoomStocks()

        assertSoftly { s ->
            s.assertThat(allRooms).containsExactly(
                RoomRecord(
                    roomTypeId = result,
                    hotelId = hotelId,
                    stockLevel = STOCK_LEVEL,
                    title = TITLE,
                    description = DESCRIPTION,
                    imageUrls = imageUrls.toSet(),
                )
            )
            s.assertThat(allStock).isEmpty()
        }
    }

    private fun loadAllRooms(): List<RoomRecord> {
        val results = connection.prepareStatement(
            """
                select r.room_type_id, r.hotel_id, r.stock_level, rd.title, rd.description, ri.image_url
                from room_type r
                join room_type_description rd on rd.room_type_id = r.room_type_id
                left outer join room_type_image ri on ri.room_type_description_id = rd.room_type_description_id
            """.trimIndent()
        ).executeQuery()

        val rooms = mutableMapOf<String, RoomRecord>()
        while (results.next()) {
            val roomTypeId = results.getString("room_type_id")
            val imageUrls = setOfNotNull(results.getString("image_url"))
            rooms[roomTypeId] = rooms[roomTypeId]?.let {
                it.copy(imageUrls = it.imageUrls + imageUrls)
            } ?: RoomRecord(
                roomTypeId = roomTypeId,
                hotelId = results.getString("hotel_id"),
                stockLevel = results.getInt("stock_level"),
                title = results.getString("title"),
                description = results.getString("description"),
                imageUrls = imageUrls,
            )
        }

        return rooms.values.toList()
    }

    private fun loadAllRoomStocks(): List<RoomStockRecord> {
        val results = connection.prepareStatement(
            """
                select room_type_id, date, morning_stock, afternoon_stock
                from room_stock
            """.trimIndent()
        ).executeQuery()

        val records = mutableListOf<RoomStockRecord>()
        while (results.next()) {
            records.add(
                RoomStockRecord(
                    roomTypeId = results.getString("room_type_id"),
                    date = LocalDate.parse(results.getString("date")),
                    morningStock = results.getInt("morning_stock"),
                    afternoonStock = results.getInt("afternoon_stock"),
                )
            )
        }

        return records
    }
}
