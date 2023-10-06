package me.aburke.hotelbooking.repository.postgres

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@Disabled
class RoomHoldRepositoryTest {

    @Test
    fun `should return holds for user`() {
        fail("TODO")
    }

    @Test
    fun `should return empty list when user does not have any holds`() {
        fail("TODO")
    }

    @Test
    fun `should not return expired holds`() {
        fail("TODO")
    }

    @Test
    fun `should create room_hold and room_stock_hold rows for room hold`() {
        fail("TODO")
    }

    @Test
    fun `should delete existing hold`() {
        fail("TODO")
    }

    @Test
    fun `should roll back transaction if the room stock is 0`() {
        fail("TODO")
    }

    @Test
    fun `should roll back transaction if the number of pre-existing holds is equal to the room stock`() {
        fail("TODO")
    }
}
