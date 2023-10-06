package me.aburke.hotelbooking.scenario.room

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@Disabled
class HoldRoomScenarioTest {

    // TODO acquire distributed lock on user ID to ensure that they can't exceed their hold quota by creating race
    // conditions via concurrently calling hold endpoint

    @Test
    fun `should create hold on room for user`() {
        fail("TODO")
    }

    @Test
    fun `should replace an existing hold on the same room`() {
        fail("TODO")
    }

    @Test
    fun `should allow multiple holds to be created provided they are on different rooms`() {
        fail("TODO")
    }

    /**
     * Extended description:
     *
     * Should limit the total number of holds for a user by deleting the hold with the earliest expiry
     * when the hold count threshold is reached
     */
    @Test
    fun `should limit the total number of holds for a user`() {
        fail("TODO")
    }

    @Test
    fun `should return StockNotAvailable when there is not enough available stock to create the hold`() {
        fail("TODO")
    }
}
