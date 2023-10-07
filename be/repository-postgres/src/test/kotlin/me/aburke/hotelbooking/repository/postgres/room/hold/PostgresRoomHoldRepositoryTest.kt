package me.aburke.hotelbooking.repository.postgres.room.hold

import me.aburke.hotelbooking.migrations.postgres.executeScript
import me.aburke.hotelbooking.ports.repository.CreateRoomHoldResult
import me.aburke.hotelbooking.ports.repository.RoomHoldRepository
import me.aburke.hotelbooking.repository.postgres.TestRooms
import me.aburke.hotelbooking.repository.postgres.createApp
import me.aburke.hotelbooking.repository.postgres.executeQueryWithRollback
import me.aburke.hotelbooking.repository.postgres.insertTestRooms
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.koin.core.KoinApplication
import java.sql.Connection
import java.time.Instant
import java.time.LocalDate

class PostgresRoomHoldRepositoryTest {

    private lateinit var app: KoinApplication
    private lateinit var connection: Connection

    private lateinit var underTest: RoomHoldRepository

    @BeforeEach
    fun init() {
        app = createApp()
        connection = app.koin.get()
        underTest = app.koin.get()

        connection.insertTestRooms(TestRooms.rooms)
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should return holds for user`() {
        connection.executeScript("test/room/insert_room_holds.sql")
        connection.executeScript("test/room/insert_additional_room_holds.sql")

        val result = underTest.findHoldsForUser(TestRooms.UserWithHolds.userId)

        assertThat(result).containsExactlyInAnyOrder(
            TestRooms.UserWithHolds.roomHold,
            TestRooms.UserWithHolds.additionalRoomHold,
        )
    }

    @Test
    fun `should return empty list when user does not have any holds`() {
        connection.executeScript("test/room/insert_room_holds.sql")
        connection.executeScript("test/room/insert_additional_room_holds.sql")

        val result = underTest.findHoldsForUser("other-user-id")

        assertThat(result).isEmpty()
    }

    @Test
    fun `should not return expired holds`() {
        connection.executeScript("test/room/insert_room_holds.sql")

        val result = underTest.findHoldsForUser(TestRooms.UserWithExpiredHold.userId)

        assertThat(result).containsExactlyInAnyOrder(
            TestRooms.UserWithExpiredHold.visibleRoomHold,
        )
    }

    @Test
    fun `should create room_hold and room_stock_hold rows for room hold`() {
        connection.executeScript("test/room/insert_room_holds.sql")

        val holdDates = (5L..9L).map {
            TestRooms.stockBaseDate.plusDays(it)
        }

        val result = underTest.createRoomHold(
            userId = TestRooms.UserWithHolds.userId,
            roomTypeId = TestRooms.UserWithHolds.additionalRoomHold.roomTypeId,
            roomHoldExpiry = TestRooms.UserWithHolds.additionalRoomHold.holdExpiry,
            holdStartDate = holdDates.first(),
            holdEndDate = holdDates.last(),
            holdIdToRemove = null,
        )

        val createdRoomHoldId = (result as? CreateRoomHoldResult.RoomHoldCreated)?.roomHoldId ?: ""
        val holdRows = connection.readAllHoldsForUser(TestRooms.UserWithHolds.userId)

        assertSoftly { s ->
            s.assertThat(result).isInstanceOf(CreateRoomHoldResult.RoomHoldCreated::class.java)
            s.assertThat(holdRows).containsExactlyInAnyOrder(
                *TestRooms.UserWithHolds.roomHoldDates.map {
                    RoomHoldRow(
                        roomHoldId = TestRooms.UserWithHolds.roomHold.roomHoldId,
                        userId = TestRooms.UserWithHolds.userId,
                        holdExpiry = TestRooms.UserWithHolds.roomHold.holdExpiry,
                        roomTypeId = TestRooms.UserWithHolds.roomHold.roomTypeId,
                        date = it,
                    )
                }.plus(
                    holdDates.map {
                        RoomHoldRow(
                            roomHoldId = createdRoomHoldId,
                            userId = TestRooms.UserWithHolds.userId,
                            holdExpiry = TestRooms.UserWithHolds.additionalRoomHold.holdExpiry,
                            roomTypeId = TestRooms.UserWithHolds.additionalRoomHold.roomTypeId,
                            date = it,
                        )
                    },
                ).toTypedArray(),
            )
        }
    }

    @Disabled
    @Test
    fun `should delete existing hold`() {
        fail("TODO")
    }

    @Disabled
    @Test
    fun `should roll back transaction if the room stock is 0`() {
        fail("TODO")
    }

    @Disabled
    @Test
    fun `should roll back transaction if the room type ID does not exist`() {
        fail("TODO")
    }

    @Disabled
    @Test
    fun `should roll back transaction if the number of pre-existing holds is equal to the room stock`() {
        fail("TODO")
    }
}

private data class RoomHoldRow(
    val roomHoldId: String,
    val userId: String,
    val holdExpiry: Instant,
    val roomTypeId: String,
    val date: LocalDate,
)

private fun Connection.readAllHoldsForUser(userId: String): List<RoomHoldRow> = prepareStatement(
    """
        select h.room_hold_id, h.user_id, h.hold_expiry, rs.room_type_id, rs.date
        from room_hold h
        join room_stock_hold rsh on rsh.room_hold_id = h.room_hold_id
        join room_stock rs on rs.room_stock_id = rsh.room_stock_id
        where h.user_id = ?
        
    """.trimIndent(),
).apply {
    setString(1, userId)
}.executeQueryWithRollback()
    .run {
        val rows = mutableListOf<RoomHoldRow>()
        while (next()) {
            rows.add(
                RoomHoldRow(
                    roomHoldId = getString("room_hold_id"),
                    userId = getString("user_id"),
                    holdExpiry = Instant.parse(getString("hold_expiry")),
                    roomTypeId = getString("room_type_id"),
                    date = LocalDate.parse(getString("date")),
                ),
            )
        }

        rows
    }
