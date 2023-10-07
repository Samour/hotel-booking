package me.aburke.hotelbooking.room

import me.aburke.hotelbooking.ports.repository.RoomHold
import me.aburke.hotelbooking.ports.scenario.room.AddRoomTypeDetails
import me.aburke.hotelbooking.ports.scenario.room.AddRoomTypePort
import me.aburke.hotelbooking.ports.scenario.room.HoldRoomDetail
import me.aburke.hotelbooking.ports.scenario.room.HoldRoomPort
import me.aburke.hotelbooking.ports.scenario.room.HoldRoomResult
import me.aburke.hotelbooking.stubs.Stubs
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.koin.core.KoinApplication
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

private const val USER_ID = "user-id"

class HoldRoomTest {

    private val stubs = Stubs()

    private lateinit var holdStartDate: LocalDate
    private lateinit var holdEndDate: LocalDate
    private lateinit var expectedHoldExpiry: Instant

    private lateinit var app: KoinApplication
    private lateinit var addRoomTypePort: AddRoomTypePort

    private lateinit var underTest: HoldRoomPort

    @BeforeEach
    fun init() {
        app = stubs.make()
        addRoomTypePort = app.koin.get()
        underTest = app.koin.get()

        holdStartDate = stubs.time.atZone(ZoneOffset.UTC).toLocalDate()
        holdEndDate = holdStartDate.plusDays(5)
        expectedHoldExpiry = stubs.time.plus(30, ChronoUnit.MINUTES)
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should create hold on room for user`() {
        val roomTypeId = createRoom()

        val result = underTest.run(
            HoldRoomDetail(
                userId = USER_ID,
                roomTypeId = roomTypeId,
                holdStartDate = holdStartDate,
                holdEndDate = holdEndDate,
            ),
        )

        val roomHoldId = (result as? HoldRoomResult.RoomHoldCreated)?.roomHoldId

        assertSoftly { s ->
            s.assertThat(result).isInstanceOf(HoldRoomResult.RoomHoldCreated::class.java)
            s.assertThat(stubs.roomHoldRepository.holds).isEqualTo(
                mapOf(
                    USER_ID to listOf(
                        RoomHold(
                            roomHoldId = roomHoldId ?: "",
                            userId = USER_ID,
                            roomTypeId = roomTypeId,
                            holdExpiry = expectedHoldExpiry,
                        ),
                    ),
                ),
            )
        }
    }

    @Test
    fun `should replace an existing hold on the same room`() {
        val roomTypeId = createRoom()

        val firstHoldResult = underTest.run(
            HoldRoomDetail(
                userId = USER_ID,
                roomTypeId = roomTypeId,
                holdStartDate = holdStartDate,
                holdEndDate = holdEndDate,
            ),
        )

        val firstRoomHoldId = (firstHoldResult as? HoldRoomResult.RoomHoldCreated)?.roomHoldId

        assertSoftly { s ->
            s.assertThat(firstHoldResult).isInstanceOf(HoldRoomResult.RoomHoldCreated::class.java)
            s.assertThat(stubs.roomHoldRepository.holds).isEqualTo(
                mapOf(
                    USER_ID to listOf(
                        RoomHold(
                            roomHoldId = firstRoomHoldId ?: "",
                            userId = USER_ID,
                            roomTypeId = roomTypeId,
                            holdExpiry = expectedHoldExpiry,
                        ),
                    ),
                ),
            )
        }

        val secondHoldResult = underTest.run(
            HoldRoomDetail(
                userId = USER_ID,
                roomTypeId = roomTypeId,
                holdStartDate = holdStartDate.plusDays(2),
                holdEndDate = holdEndDate,
            ),
        )

        val secondRoomHoldId = (secondHoldResult as? HoldRoomResult.RoomHoldCreated)?.roomHoldId

        assertSoftly { s ->
            s.assertThat(firstHoldResult).isInstanceOf(HoldRoomResult.RoomHoldCreated::class.java)
            s.assertThat(stubs.roomHoldRepository.holds).isEqualTo(
                mapOf(
                    USER_ID to listOf(
                        RoomHold(
                            roomHoldId = secondRoomHoldId ?: "",
                            userId = USER_ID,
                            roomTypeId = roomTypeId,
                            holdExpiry = expectedHoldExpiry,
                        ),
                    ),
                ),
            )
        }
    }

    @Test
    fun `should allow multiple holds to be created provided they are on different rooms`() {
        val roomTypeId1 = createRoom()
        val roomTypeId2 = createRoom()

        val firstHoldResult = underTest.run(
            HoldRoomDetail(
                userId = USER_ID,
                roomTypeId = roomTypeId1,
                holdStartDate = holdStartDate,
                holdEndDate = holdEndDate,
            ),
        )
        val secondHoldResult = underTest.run(
            HoldRoomDetail(
                userId = USER_ID,
                roomTypeId = roomTypeId2,
                holdStartDate = holdStartDate.plusDays(2),
                holdEndDate = holdEndDate,
            ),
        )

        val firstRoomHoldId = (firstHoldResult as? HoldRoomResult.RoomHoldCreated)?.roomHoldId
        val secondRoomHoldId = (secondHoldResult as? HoldRoomResult.RoomHoldCreated)?.roomHoldId

        assertSoftly { s ->
            s.assertThat(firstHoldResult).isInstanceOf(HoldRoomResult.RoomHoldCreated::class.java)
            s.assertThat(secondHoldResult).isInstanceOf(HoldRoomResult.RoomHoldCreated::class.java)
            s.assertThat(stubs.roomHoldRepository.holds).isEqualTo(
                mapOf(
                    USER_ID to listOf(
                        RoomHold(
                            roomHoldId = firstRoomHoldId ?: "",
                            userId = USER_ID,
                            roomTypeId = roomTypeId1,
                            holdExpiry = expectedHoldExpiry,
                        ),
                        RoomHold(
                            roomHoldId = secondRoomHoldId ?: "",
                            userId = USER_ID,
                            roomTypeId = roomTypeId2,
                            holdExpiry = expectedHoldExpiry,
                        ),
                    ),
                ),
            )
        }
    }

    @Disabled
    @Test
    fun `should limit the total number of holds for a user`() {
        fail("TODO")
    }

    @Disabled
    @Test
    fun `should return StockNotAvailable when there is not enough available stock to create the hold`() {
        fail("TODO")
    }

    @Disabled
    @Test
    fun `should return ConcurrentHoldRequest when there is a concurrent hold request for the user`() {
        fail("TODO")
    }

    @Disabled
    @Test
    fun `should create the hold when there is a concurrent hold request for a different user`() {
        fail("TODO")
    }

    private fun createRoom(): String = addRoomTypePort.run(
        AddRoomTypeDetails(
            title = "title",
            description = "description",
            imageUrls = emptyList(),
            pricePerNight = 1,
            stockLevel = 1,
        ),
    ).roomTypeId
}
