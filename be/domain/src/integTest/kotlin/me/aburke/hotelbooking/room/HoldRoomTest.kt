package me.aburke.hotelbooking.room

import me.aburke.hotelbooking.ports.scenario.room.HoldRoomPort
import me.aburke.hotelbooking.stubs.Stubs
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.koin.core.KoinApplication

@Disabled
class HoldRoomTest {

    private val stubs = Stubs()

    private lateinit var app: KoinApplication
    private lateinit var underTest: HoldRoomPort

    @BeforeEach
    fun init() {
        app = stubs.make()
        underTest = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = app.close()

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

    @Test
    fun `should limit the total number of holds for a user`() {
        fail("TODO")
    }

    @Test
    fun `should return StockNotAvailable when there is not enough available stock to create the hold`() {
        fail("TODO")
    }

    @Test
    fun `should return ConcurrentHoldRequest when there is a concurrent hold request for the user`() {
        fail("TODO")
    }

    @Test
    fun `should create the hold when there is a concurrent hold request for a different user`() {
        fail("TODO")
    }
}
