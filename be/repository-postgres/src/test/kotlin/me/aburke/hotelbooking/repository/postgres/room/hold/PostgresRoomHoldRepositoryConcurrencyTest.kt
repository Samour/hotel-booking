package me.aburke.hotelbooking.repository.postgres.room.hold

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@Disabled
class PostgresRoomHoldRepositoryConcurrencyTest {

    @Test
    @Suppress("ktlint:max-line-length")
    fun `should prevent hold from being created when stock is unavailable due to another hold being created concurrently`() {
        fail("TODO")
    }

    @Test
    @Suppress("ktlint:max-line-length")
    fun `should prevent hold from being created when stock is unavailable due to stock_level being reduced to 0 concurrently`() {
        fail("TODO")
    }
}
