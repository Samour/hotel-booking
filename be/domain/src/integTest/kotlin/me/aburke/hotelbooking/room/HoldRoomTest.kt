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
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

private const val USER_ID = "user-id"
private const val ALT_USER_ID = "alt-user-id"

class HoldRoomTest {

    private val stubs = Stubs()

    private lateinit var holdStartDate: LocalDate
    private lateinit var holdEndDate: LocalDate

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
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should create hold on room for user`() {
        val roomTypeId = createRoom()

        val (result, expectedHoldExpiry) = holdRoom(roomTypeId)
        val roomHoldId = (result as? HoldRoomResult.RoomHoldCreated)?.roomHoldId

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                HoldRoomResult.RoomHoldCreated(
                    roomHoldId = roomHoldId ?: "",
                    holdExpiry = expectedHoldExpiry,
                    removedRoomHoldId = null,
                ),
            )
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

        val (holdResults, expectedExpiryTimes) = (1..2).map { holdRoom(roomTypeId) }
            .let { pairs ->
                pairs.map { it.first } to pairs.map { it.second }
            }
        val roomHoldIds = holdResults.map {
            (it as? HoldRoomResult.RoomHoldCreated)?.roomHoldId
        }

        assertSoftly { s ->
            s.assertThat(holdResults[0]).isEqualTo(
                HoldRoomResult.RoomHoldCreated(
                    roomHoldId = roomHoldIds[0] ?: "",
                    holdExpiry = expectedExpiryTimes[0],
                    removedRoomHoldId = null,
                ),
            )
            s.assertThat(holdResults[1]).isEqualTo(
                HoldRoomResult.RoomHoldCreated(
                    roomHoldId = roomHoldIds[1] ?: "",
                    holdExpiry = expectedExpiryTimes[1],
                    removedRoomHoldId = roomHoldIds[0],
                ),
            )
            s.assertThat(stubs.roomHoldRepository.holds).isEqualTo(
                mapOf(
                    USER_ID to listOf(
                        RoomHold(
                            roomHoldId = roomHoldIds[1] ?: "",
                            userId = USER_ID,
                            roomTypeId = roomTypeId,
                            holdExpiry = expectedExpiryTimes[1],
                        ),
                    ),
                ),
            )
        }
    }

    @Test
    fun `should allow multiple holds to be created provided they are on different rooms`() {
        val roomTypeIds = (1..2).map { createRoom() }

        val (holdResults, expectedExpiryTimes) = roomTypeIds.map { holdRoom(it) }
            .let { pairs ->
                pairs.map { it.first } to pairs.map { it.second }
            }
        val roomHoldIds = holdResults.map {
            (it as? HoldRoomResult.RoomHoldCreated)?.roomHoldId
        }

        assertSoftly { s ->
            (0..1).forEach {
                s.assertThat(holdResults[it]).isEqualTo(
                    HoldRoomResult.RoomHoldCreated(
                        roomHoldId = roomHoldIds[it] ?: "",
                        holdExpiry = expectedExpiryTimes[it],
                        removedRoomHoldId = null,
                    ),
                )
            }
            s.assertThat(stubs.roomHoldRepository.holds).isEqualTo(
                mapOf(
                    USER_ID to (0..1).map {
                        RoomHold(
                            roomHoldId = roomHoldIds[it] ?: "",
                            userId = USER_ID,
                            roomTypeId = roomTypeIds[it],
                            holdExpiry = expectedExpiryTimes[it],
                        )
                    },
                ),
            )
        }
    }

    @Test
    fun `should limit the total number of holds for a user`() {
        val roomTypeIds = (1..4).map { createRoom() }

        val (holdResults, expectedExpiryTimes) = roomTypeIds.map { holdRoom(it) }
            .let { pairs ->
                pairs.map { it.first } to pairs.map { it.second }
            }
        val roomHoldIds = holdResults.map {
            (it as? HoldRoomResult.RoomHoldCreated)?.roomHoldId
        }

        assertSoftly { s ->
            (0..2).forEach {
                s.assertThat(holdResults[it]).isEqualTo(
                    HoldRoomResult.RoomHoldCreated(
                        roomHoldId = roomHoldIds[it] ?: "",
                        holdExpiry = expectedExpiryTimes[it],
                        removedRoomHoldId = null,
                    ),
                )
            }
            s.assertThat(holdResults[3]).isEqualTo(
                HoldRoomResult.RoomHoldCreated(
                    roomHoldId = roomHoldIds[3] ?: "",
                    holdExpiry = expectedExpiryTimes[3],
                    removedRoomHoldId = roomHoldIds[0],
                ),
            )
            s.assertThat(stubs.roomHoldRepository.holds).isEqualTo(
                mapOf(
                    USER_ID to (1..3).map {
                        RoomHold(
                            roomHoldId = roomHoldIds[it] ?: "",
                            userId = USER_ID,
                            roomTypeId = roomTypeIds[it],
                            holdExpiry = expectedExpiryTimes[it],
                        )
                    },
                ),
            )
        }
    }

    @Test
    fun `should return StockNotAvailable when there is not enough available stock to create the hold`() {
        val roomTypeId = createRoom()

        val (holdResults, expectedExpiryTimes) = listOf(ALT_USER_ID, USER_ID)
            .map { holdRoom(roomTypeId, it) }
            .let { pairs ->
                pairs.map { it.first } to pairs.map { it.second }
            }
        val roomHoldIds = holdResults.map {
            (it as? HoldRoomResult.RoomHoldCreated)?.roomHoldId
        }

        assertSoftly { s ->
            s.assertThat(holdResults[0]).isEqualTo(
                HoldRoomResult.RoomHoldCreated(
                    roomHoldId = roomHoldIds[0] ?: "",
                    holdExpiry = expectedExpiryTimes[0],
                    removedRoomHoldId = null,
                ),
            )
            s.assertThat(holdResults[1]).isEqualTo(
                HoldRoomResult.StockNotAvailable,
            )
            s.assertThat(stubs.roomHoldRepository.holds).isEqualTo(
                mapOf(
                    ALT_USER_ID to listOf(
                        RoomHold(
                            roomHoldId = roomHoldIds[0] ?: "",
                            userId = ALT_USER_ID,
                            roomTypeId = roomTypeId,
                            holdExpiry = expectedExpiryTimes[0],
                        ),
                    ),
                ),
            )
        }
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

    private fun expectedHoldExpiry() = stubs.time.plus(30, ChronoUnit.MINUTES)

    private fun holdRoom(roomTypeId: String, userId: String = USER_ID) = underTest.run(
        HoldRoomDetail(
            userId = userId,
            roomTypeId = roomTypeId,
            holdStartDate = holdStartDate,
            holdEndDate = holdEndDate,
        ),
    ).let {
        it to expectedHoldExpiry()
    }
}
