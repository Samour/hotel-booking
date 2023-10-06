package me.aburke.hotelbooking.repository.postgres

import me.aburke.hotelbooking.migrations.postgres.executeScript
import me.aburke.hotelbooking.ports.repository.InsertRoomType
import me.aburke.hotelbooking.ports.repository.RoomRepository
import me.aburke.hotelbooking.repository.postgres.queries.insertTestRooms
import org.assertj.core.api.Assertions.assertThat
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
private const val PRICE_PER_NIGHT = 150_00
private const val CURRENT_USER_ID = "current-user-id"

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

private val stockDatesToRemove = setOf(
    LocalDate.parse("2023-08-16"),
    LocalDate.parse("2023-08-20"),
    LocalDate.parse("2023-08-28"),
)
private val roomsWithNoStock = setOf(
    "room-type-id-3",
    "room-type-id-7",
)

private data class RoomRecord(
    val roomTypeId: String,
    val hotelId: String,
    val stockLevel: Int,
    val title: String,
    val description: String,
    val imageUrls: List<String>,
    val pricePerNight: Int,
)

private data class TestRoomStockRecord(
    val roomTypeId: String,
    val date: LocalDate,
    val stockLevel: Int,
)

class PostgresRoomRepositoryTest {

    private lateinit var app: KoinApplication
    private lateinit var connection: Connection

    private lateinit var underTest: RoomRepository

    @BeforeEach
    fun init() {
        app = createApp()
        connection = app.koin.get()
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
                pricePerNight = PRICE_PER_NIGHT,
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
                    imageUrls = imageUrls,
                    pricePerNight = PRICE_PER_NIGHT,
                ),
            )
            s.assertThat(allStock).containsExactlyInAnyOrder(
                *stockDates.map {
                    TestRoomStockRecord(
                        roomTypeId = result,
                        date = it,
                        stockLevel = STOCK_LEVEL,
                    )
                }.toTypedArray(),
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
                pricePerNight = PRICE_PER_NIGHT,
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
                    imageUrls = emptyList(),
                    pricePerNight = PRICE_PER_NIGHT,
                ),
            )
            s.assertThat(allStock).containsExactlyInAnyOrder(
                *stockDates.map {
                    TestRoomStockRecord(
                        roomTypeId = result,
                        date = it,
                        stockLevel = STOCK_LEVEL,
                    )
                }.toTypedArray(),
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
                pricePerNight = PRICE_PER_NIGHT,
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
                    imageUrls = imageUrls,
                    pricePerNight = PRICE_PER_NIGHT,
                ),
            )
            s.assertThat(allStock).isEmpty()
        }
    }

    @Test
    fun `should return rooms, descriptions & stock levels based on date range`() {
        connection.insertTestRooms(TestRooms.rooms)

        val queryStartDate = LocalDate.parse("2023-08-13")
        val queryEndDate = LocalDate.parse("2023-09-01")

        val result = underTest.queryRoomsAndAvailability(
            CURRENT_USER_ID,
            queryStartDate,
            queryEndDate,
        )

        assertThat(result).containsExactlyInAnyOrder(
            *TestRooms.rooms.map { room ->
                room.copy(
                    stockLevels = room.stockLevels.filter {
                        it.date >= queryStartDate && it.date <= queryEndDate
                    },
                )
            }.toTypedArray(),
        )
    }

    @Test
    fun `should return empty list when no rooms exist`() {
        val queryStartDate = LocalDate.parse("2023-08-13")
        val queryEndDate = LocalDate.parse("2023-09-01")

        val result = underTest.queryRoomsAndAvailability(
            CURRENT_USER_ID,
            queryStartDate,
            queryEndDate,
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `should return room & stock data when there are gaps in stock records`() {
        connection.insertTestRooms(TestRooms.rooms)
        deleteSomeRoomStock()

        val queryStartDate = LocalDate.parse("2023-08-13")
        val queryEndDate = LocalDate.parse("2023-09-01")

        val result = underTest.queryRoomsAndAvailability(
            CURRENT_USER_ID,
            queryStartDate,
            queryEndDate,
        )

        assertThat(result).containsExactlyInAnyOrder(
            *TestRooms.rooms.map { room ->
                room.copy(
                    stockLevels = room.stockLevels.filter {
                        it.date >= queryStartDate && it.date <= queryEndDate &&
                            !stockDatesToRemove.contains(it.date)
                    },
                )
            }.toTypedArray(),
        )
    }

    @Test
    fun `should omit rooms which have no stock data`() {
        connection.insertTestRooms(TestRooms.rooms)
        deleteStockForRooms()

        val queryStartDate = LocalDate.parse("2023-08-13")
        val queryEndDate = LocalDate.parse("2023-09-01")

        val result = underTest.queryRoomsAndAvailability(
            CURRENT_USER_ID,
            queryStartDate,
            queryEndDate,
        )

        assertThat(result).containsExactlyInAnyOrder(
            *TestRooms.rooms
                .filter { !roomsWithNoStock.contains(it.roomTypeId) }
                .map { room ->
                    room.copy(
                        stockLevels = room.stockLevels.filter {
                            it.date >= queryStartDate && it.date <= queryEndDate
                        },
                    )
                }.toTypedArray(),
        )
    }

    @Test
    fun `should return rooms, descriptions & stock levels based on date range & hold data`() {
        connection.insertTestRooms(TestRooms.rooms)
        connection.executeScript("test/room/insert_room_holds.sql")

        val queryStartDate = LocalDate.parse("2023-08-13")
        val queryEndDate = LocalDate.parse("2023-09-01")

        val result = underTest.queryRoomsAndAvailability(
            CURRENT_USER_ID,
            queryStartDate,
            queryEndDate,
        )

        assertThat(result).containsExactlyInAnyOrder(
            *TestRooms.rooms.map { room ->
                room.copy(
                    stockLevels = room.stockLevels.filter {
                        it.date >= queryStartDate && it.date <= queryEndDate
                    }.map {
                        it.copy(
                            stockLevel = it.stockLevel - TestRooms.getHoldCount(room.roomTypeId, it.date),
                        )
                    },
                )
            }.toTypedArray(),
        )
    }

    private fun loadAllRooms(): List<RoomRecord> {
        val results = connection.prepareStatement(
            """
                select r.room_type_id, r.hotel_id, r.stock_level, rd.title, rd.description, rd.image_urls,
                    rd.price_per_night
                from room_type r
                join room_type_description rd on rd.room_type_id = r.room_type_id
            """.trimIndent(),
        ).executeQuery()

        val rooms = mutableListOf<RoomRecord>()
        while (results.next()) {
            rooms.add(
                RoomRecord(
                    roomTypeId = results.getString("room_type_id"),
                    hotelId = results.getString("hotel_id"),
                    stockLevel = results.getInt("stock_level"),
                    title = results.getString("title"),
                    description = results.getString("description"),
                    imageUrls = listOf(*(results.getArray("image_urls").array as Array<String>)),
                    pricePerNight = results.getInt("price_per_night"),
                ),
            )
        }

        return rooms
    }

    private fun loadAllRoomStocks(): List<TestRoomStockRecord> {
        val results = connection.prepareStatement(
            """
                select room_type_id, date, stock_level
                from room_stock
            """.trimIndent(),
        ).executeQuery()

        val records = mutableListOf<TestRoomStockRecord>()
        while (results.next()) {
            records.add(
                TestRoomStockRecord(
                    roomTypeId = results.getString("room_type_id"),
                    date = LocalDate.parse(results.getString("date")),
                    stockLevel = results.getInt("stock_level"),
                ),
            )
        }

        return records
    }

    private fun deleteSomeRoomStock() {
        val query = connection.prepareStatement(
            """
                delete from room_stock where date in (?, ?, ?)
            """.trimIndent(),
        )
        val dates = stockDatesToRemove.map { it.toString() }
        query.setString(1, dates[0])
        query.setString(2, dates[1])
        query.setString(3, dates[2])
        query.executeUpdate()
    }

    private fun deleteStockForRooms() {
        val query = connection.prepareStatement(
            """
                delete from room_stock where room_type_id in (?, ?)
            """.trimIndent(),
        )
        val roomIds = roomsWithNoStock.toList()
        query.setString(1, roomIds[0])
        query.setString(2, roomIds[1])
        query.executeUpdate()
    }
}
