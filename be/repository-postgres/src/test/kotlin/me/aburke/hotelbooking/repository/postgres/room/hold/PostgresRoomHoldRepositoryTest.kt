package me.aburke.hotelbooking.repository.postgres.room.hold

import me.aburke.hotelbooking.migrations.postgres.executeScript
import me.aburke.hotelbooking.ports.repository.CreateRoomHoldResult
import me.aburke.hotelbooking.ports.repository.RoomHoldRepository
import me.aburke.hotelbooking.repository.postgres.TestRooms
import me.aburke.hotelbooking.repository.postgres.appForTest
import me.aburke.hotelbooking.repository.postgres.executeUpdateWithRollback
import me.aburke.hotelbooking.repository.postgres.insertTestRooms
import me.aburke.hotelbooking.repository.postgres.setRoomStockLevel
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.sql.Connection
import javax.sql.DataSource

class PostgresRoomHoldRepositoryTest {

    private lateinit var app: KoinApplication
    private lateinit var connection: Connection

    private lateinit var underTest: RoomHoldRepository

    @BeforeEach
    fun init() {
        app = appForTest()
        connection = app.koin.get<DataSource>().connection
        underTest = app.koin.get()

        connection.insertTestRooms(TestRooms.rooms)
    }

    @AfterEach
    fun cleanUp() = connection.close()

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
        connection.setRoomStockLevel(TestRooms.UserWithHolds.additionalRoomHold.roomTypeId, 1)

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

    @Test
    fun `should delete existing hold`() {
        connection.executeScript("test/room/insert_room_holds.sql")
        connection.setRoomStockLevel(TestRooms.UserWithHolds.additionalRoomHold.roomTypeId, 1)

        val holdDates = (5L..9L).map {
            TestRooms.stockBaseDate.plusDays(it)
        }

        val result = underTest.createRoomHold(
            userId = TestRooms.UserWithHolds.userId,
            roomTypeId = TestRooms.UserWithHolds.additionalRoomHold.roomTypeId,
            roomHoldExpiry = TestRooms.UserWithHolds.additionalRoomHold.holdExpiry,
            holdStartDate = holdDates.first(),
            holdEndDate = holdDates.last(),
            holdIdToRemove = TestRooms.UserWithHolds.roomHold.roomHoldId,
        )

        val createdRoomHoldId = (result as? CreateRoomHoldResult.RoomHoldCreated)?.roomHoldId ?: ""
        val holdRows = connection.readAllHoldsForUser(TestRooms.UserWithHolds.userId)

        assertSoftly { s ->
            s.assertThat(result).isInstanceOf(CreateRoomHoldResult.RoomHoldCreated::class.java)
            s.assertThat(holdRows).containsExactlyInAnyOrder(
                *holdDates.map {
                    RoomHoldRow(
                        roomHoldId = createdRoomHoldId,
                        userId = TestRooms.UserWithHolds.userId,
                        holdExpiry = TestRooms.UserWithHolds.additionalRoomHold.holdExpiry,
                        roomTypeId = TestRooms.UserWithHolds.additionalRoomHold.roomTypeId,
                        date = it,
                    )
                }.toTypedArray(),
            )
        }
    }

    @Test
    fun `should roll back transaction if the room stock is 0`() {
        connection.executeScript("test/room/insert_room_holds.sql")
        connection.setRoomStockLevel(TestRooms.UserWithHolds.additionalRoomHold.roomTypeId, 0)

        val holdDates = (5L..9L).map {
            TestRooms.stockBaseDate.plusDays(it)
        }

        val result = underTest.createRoomHold(
            userId = TestRooms.UserWithHolds.userId,
            roomTypeId = TestRooms.UserWithHolds.additionalRoomHold.roomTypeId,
            roomHoldExpiry = TestRooms.UserWithHolds.additionalRoomHold.holdExpiry,
            holdStartDate = holdDates.first(),
            holdEndDate = holdDates.last(),
            holdIdToRemove = TestRooms.UserWithHolds.roomHold.roomHoldId,
        )

        val holdRows = connection.readAllHoldsForUser(TestRooms.UserWithHolds.userId)

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(CreateRoomHoldResult.StockNotAvailable)
            s.assertThat(holdRows).containsExactlyInAnyOrder(
                *TestRooms.UserWithHolds.roomHoldDates.map {
                    RoomHoldRow(
                        roomHoldId = TestRooms.UserWithHolds.roomHold.roomHoldId,
                        userId = TestRooms.UserWithHolds.userId,
                        holdExpiry = TestRooms.UserWithHolds.roomHold.holdExpiry,
                        roomTypeId = TestRooms.UserWithHolds.roomHold.roomTypeId,
                        date = it,
                    )
                }.toTypedArray(),
            )
        }
    }

    @Test
    fun `should roll back transaction if the number of pre-existing holds is equal to the room stock`() {
        connection.executeScript("test/room/insert_room_holds.sql")
        connection.setRoomStockLevel(TestRooms.RoomHeldByAnotherUser.roomTypeId, 1)

        val result = underTest.createRoomHold(
            userId = TestRooms.UserWithHolds.userId,
            roomTypeId = TestRooms.RoomHeldByAnotherUser.roomTypeId,
            roomHoldExpiry = TestRooms.UserWithHolds.additionalRoomHold.holdExpiry,
            holdStartDate = TestRooms.RoomHeldByAnotherUser.heldDates.first().minusDays(2),
            holdEndDate = TestRooms.RoomHeldByAnotherUser.heldDates.last(),
            holdIdToRemove = TestRooms.UserWithHolds.roomHold.roomHoldId,
        )

        val holdRows = connection.readAllHoldsForUser(TestRooms.UserWithHolds.userId)

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(CreateRoomHoldResult.StockNotAvailable)
            s.assertThat(holdRows).containsExactlyInAnyOrder(
                *TestRooms.UserWithHolds.roomHoldDates.map {
                    RoomHoldRow(
                        roomHoldId = TestRooms.UserWithHolds.roomHold.roomHoldId,
                        userId = TestRooms.UserWithHolds.userId,
                        holdExpiry = TestRooms.UserWithHolds.roomHold.holdExpiry,
                        roomTypeId = TestRooms.UserWithHolds.roomHold.roomTypeId,
                        date = it,
                    )
                }.toTypedArray(),
            )
        }
    }

    @Suppress("ktlint:max-line-length")
    @Test
    fun `should create hold when no stock is available due to the hold that is being deleted as part of this transaction`() {
        connection.executeScript("test/room/insert_room_holds.sql")
        connection.setRoomStockLevel(TestRooms.UserWithHolds.roomHold.roomTypeId, 1)
        connection.deleteConflictingHolds(TestRooms.UserWithHolds.roomHold.roomTypeId, TestRooms.UserWithHolds.userId)

        val holdDates = TestRooms.UserWithHolds.roomHoldDates.plus(
            listOf(
                TestRooms.UserWithHolds.roomHoldDates.last().plusDays(1),
                TestRooms.UserWithHolds.roomHoldDates.last().plusDays(2),
            ),
        )

        val result = underTest.createRoomHold(
            userId = TestRooms.UserWithHolds.userId,
            roomTypeId = TestRooms.UserWithHolds.roomHold.roomTypeId,
            roomHoldExpiry = TestRooms.UserWithHolds.roomHold.holdExpiry,
            holdStartDate = holdDates.first(),
            holdEndDate = holdDates.last(),
            holdIdToRemove = TestRooms.UserWithHolds.roomHold.roomHoldId,
        )

        val createdRoomHoldId = (result as? CreateRoomHoldResult.RoomHoldCreated)?.roomHoldId ?: ""
        val holdRows = connection.readAllHoldsForUser(TestRooms.UserWithHolds.userId)

        assertSoftly { s ->
            s.assertThat(result).isInstanceOf(CreateRoomHoldResult.RoomHoldCreated::class.java)
            s.assertThat(holdRows).containsExactlyInAnyOrder(
                *holdDates.map {
                    RoomHoldRow(
                        roomHoldId = createdRoomHoldId,
                        userId = TestRooms.UserWithHolds.userId,
                        holdExpiry = TestRooms.UserWithHolds.roomHold.holdExpiry,
                        roomTypeId = TestRooms.UserWithHolds.roomHold.roomTypeId,
                        date = it,
                    )
                }.toTypedArray(),
            )
        }
    }

    @Test
    fun `should create hold when stock is available due to a hold that's past its expiry time`() {
        connection.executeScript("test/room/insert_room_holds.sql")
        connection.setRoomStockLevel(TestRooms.UserWithExpiredHold.expiredRoomTypeId, 1)

        val holdDates = listOf(
            TestRooms.UserWithExpiredHold.expiredHoldDates.last(),
            TestRooms.UserWithExpiredHold.expiredHoldDates.last().plusDays(1),
            TestRooms.UserWithExpiredHold.expiredHoldDates.last().plusDays(2),
        )

        val result = underTest.createRoomHold(
            userId = TestRooms.UserWithHolds.userId,
            roomTypeId = TestRooms.UserWithExpiredHold.expiredRoomTypeId,
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
                            roomTypeId = TestRooms.UserWithExpiredHold.expiredRoomTypeId,
                            date = it,
                        )
                    },
                ).toTypedArray(),
            )
        }
    }

    @Test
    fun `should roll back transaction if the room type ID does not exist`() {
        connection.executeScript("test/room/insert_room_holds.sql")

        val holdDates = (5L..9L).map {
            TestRooms.stockBaseDate.plusDays(it)
        }

        val result = underTest.createRoomHold(
            userId = TestRooms.UserWithHolds.userId,
            roomTypeId = "not-a-valid-room-type-id",
            roomHoldExpiry = TestRooms.UserWithHolds.additionalRoomHold.holdExpiry,
            holdStartDate = holdDates.first(),
            holdEndDate = holdDates.last(),
            holdIdToRemove = TestRooms.UserWithHolds.roomHold.roomHoldId,
        )

        val holdRows = connection.readAllHoldsForUser(TestRooms.UserWithHolds.userId)

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(CreateRoomHoldResult.StockNotAvailable)
            s.assertThat(holdRows).containsExactlyInAnyOrder(
                *TestRooms.UserWithHolds.roomHoldDates.map {
                    RoomHoldRow(
                        roomHoldId = TestRooms.UserWithHolds.roomHold.roomHoldId,
                        userId = TestRooms.UserWithHolds.userId,
                        holdExpiry = TestRooms.UserWithHolds.roomHold.holdExpiry,
                        roomTypeId = TestRooms.UserWithHolds.roomHold.roomTypeId,
                        date = it,
                    )
                }.toTypedArray(),
            )
        }
    }
}

private fun Connection.deleteConflictingHolds(roomTypeId: String, userId: String) = prepareStatement(
    """
        delete from room_hold
        where user_id != ? and room_hold_id in (
            select distinct rsh.room_hold_id
            from room_stock_hold rsh
            join room_stock rs on rs.room_stock_id = rsh.room_stock_id
            where rs.room_type_id = ?
        )
    """.trimIndent(),
).apply {
    setString(1, userId)
    setString(2, roomTypeId)
}.executeUpdateWithRollback()
