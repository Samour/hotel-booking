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
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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

    @Test
    fun `should return ConcurrentHoldRequest when there is a concurrent hold request for the user`() {
        val roomTypeId = (1..2).map { createRoom() }

        val blockFirstRequestLatch = CountDownLatch(1)
        val firstRequestReceivedLatch = CountDownLatch(1)
        stubs.lockRepository.postAcquireHook = { _ ->
            if (firstRequestReceivedLatch.count == 1L) {
                firstRequestReceivedLatch.countDown()
                blockFirstRequestLatch.await(30, TimeUnit.SECONDS)
            }
        }

        var roomHold1: HoldRoomResult? = null
        var expectedExpiry: Instant? = null
        val firstRequestThread = Thread {
            holdRoom(roomTypeId[0]).let {
                roomHold1 = it.first
                expectedExpiry = it.second
            }
        }.apply { start() }

        firstRequestReceivedLatch.await(30, TimeUnit.SECONDS)

        val (roomHold2) = holdRoom(roomTypeId[1])

        blockFirstRequestLatch.countDown()
        firstRequestThread.join(30_000)
        val roomHoldId = (roomHold1 as? HoldRoomResult.RoomHoldCreated)?.roomHoldId

        assertSoftly { s ->
            s.assertThat(roomHold1).isEqualTo(
                HoldRoomResult.RoomHoldCreated(
                    roomHoldId = roomHoldId ?: "",
                    holdExpiry = expectedExpiry ?: Instant.EPOCH,
                    removedRoomHoldId = null,
                ),
            )
            s.assertThat(roomHold2).isEqualTo(
                HoldRoomResult.ConcurrentHoldRequest,
            )
            s.assertThat(stubs.roomHoldRepository.holds).isEqualTo(
                mapOf(
                    USER_ID to listOf(
                        RoomHold(
                            roomHoldId = roomHoldId ?: "",
                            userId = USER_ID,
                            roomTypeId = roomTypeId[0],
                            holdExpiry = expectedExpiry ?: Instant.EPOCH,
                        ),
                    ),
                ),
            )
        }
    }

    @Test
    fun `should create the hold when there is a concurrent hold request for a different user`() {
        val roomTypeId = (1..2).map { createRoom() }

        val blockFirstRequestLatch = CountDownLatch(1)
        val firstRequestReceivedLatch = CountDownLatch(1)
        stubs.lockRepository.postAcquireHook = { _ ->
            if (firstRequestReceivedLatch.count == 1L) {
                firstRequestReceivedLatch.countDown()
                blockFirstRequestLatch.await(30, TimeUnit.SECONDS)
            }
        }

        var roomHold1: HoldRoomResult? = null
        var expectedExpiry1: Instant? = null
        val firstRequestThread = Thread {
            holdRoom(roomTypeId[0]).let {
                roomHold1 = it.first
                expectedExpiry1 = it.second
            }
        }.apply { start() }

        firstRequestReceivedLatch.await(30, TimeUnit.SECONDS)

        val (roomHold2, expectedExpiry2) = holdRoom(roomTypeId[1], ALT_USER_ID)

        blockFirstRequestLatch.countDown()
        firstRequestThread.join(30_000)
        val roomHoldId1 = (roomHold1 as? HoldRoomResult.RoomHoldCreated)?.roomHoldId
        val roomHoldId2 = (roomHold2 as? HoldRoomResult.RoomHoldCreated)?.roomHoldId

        assertSoftly { s ->
            s.assertThat(roomHold1).isEqualTo(
                HoldRoomResult.RoomHoldCreated(
                    roomHoldId = roomHoldId1 ?: "",
                    holdExpiry = expectedExpiry1 ?: Instant.EPOCH,
                    removedRoomHoldId = null,
                ),
            )
            s.assertThat(roomHold2).isEqualTo(
                HoldRoomResult.RoomHoldCreated(
                    roomHoldId = roomHoldId2 ?: "",
                    holdExpiry = expectedExpiry2 ?: Instant.EPOCH,
                    removedRoomHoldId = null,
                ),
            )
            s.assertThat(stubs.roomHoldRepository.holds).isEqualTo(
                mapOf(
                    USER_ID to listOf(
                        RoomHold(
                            roomHoldId = roomHoldId1 ?: "",
                            userId = USER_ID,
                            roomTypeId = roomTypeId[0],
                            holdExpiry = expectedExpiry1 ?: Instant.EPOCH,
                        ),
                    ),
                    ALT_USER_ID to listOf(
                        RoomHold(
                            roomHoldId = roomHoldId2 ?: "",
                            userId = ALT_USER_ID,
                            roomTypeId = roomTypeId[1],
                            holdExpiry = expectedExpiry2 ?: Instant.EPOCH,
                        ),
                    ),
                ),
            )
        }
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
