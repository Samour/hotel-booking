package me.aburke.hotelbooking.repository.postgres.room.hold

import me.aburke.hotelbooking.migrations.postgres.executeScript
import me.aburke.hotelbooking.ports.repository.RoomHoldRepository
import me.aburke.hotelbooking.repository.postgres.TestRooms
import me.aburke.hotelbooking.repository.postgres.createApp
import me.aburke.hotelbooking.repository.postgres.insertTestRooms
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.koin.core.KoinApplication
import java.sql.Connection

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

    @Disabled
    @Test
    fun `should create room_hold and room_stock_hold rows for room hold`() {
        fail("TODO")
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
    fun `should roll back transaction if the number of pre-existing holds is equal to the room stock`() {
        fail("TODO")
    }
}
