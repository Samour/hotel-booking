package me.aburke.hotelbooking.scenario.room

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import me.aburke.hotelbooking.lock.FastFailLock
import me.aburke.hotelbooking.ports.repository.CreateRoomHoldResult
import me.aburke.hotelbooking.ports.repository.RoomHold
import me.aburke.hotelbooking.ports.repository.RoomHoldRepository
import me.aburke.hotelbooking.ports.scenario.room.HoldRoomDetail
import me.aburke.hotelbooking.ports.scenario.room.HoldRoomResult
import me.aburke.hotelbooking.stubConflict
import me.aburke.hotelbooking.stubUnblocked
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private const val MAX_CONCURRENT_HOLDS = 3
private const val USER_ID = "user-id"
private const val ROOM_TYPE_ID = "room-type-id"
private const val ROOM_HOLD_ID = "room-hold-id"
private const val EXISTING_HOLD_ID1 = "existing-hold-id-1"
private const val EXISTING_HOLD_ID2 = "existing-hold-id-2"
private const val EXISTING_HOLD_ID3 = "existing-hold-id-3"
private const val ALTERNATE_ROOM_TYPE_ID1 = "alternate-room-type-id-1"
private const val ALTERNATE_ROOM_TYPE_ID2 = "alternate-room-type-id-2"
private const val ALTERNATE_ROOM_TYPE_ID3 = "alternate-room-type-id-3"

private val holdStartDate = LocalDate.parse("2023-05-10")
private val holdEndDate = LocalDate.parse("2023-05-15")

private val instant = Instant.now()
private val roomHoldDuration = Duration.parse("PT30M")
private val expectedExpiry = instant.plus(roomHoldDuration)
private val existingHoldExpiry1 = instant.plus(2, ChronoUnit.MINUTES)
private val existingHoldExpiry2 = instant.plus(3, ChronoUnit.MINUTES)
private val existingHoldExpiry3 = instant.plus(4, ChronoUnit.MINUTES)

@ExtendWith(MockKExtension::class)
class HoldRoomScenarioTest {

    @MockK
    lateinit var clock: Clock

    @MockK
    lateinit var fastFailLock: FastFailLock

    @MockK
    lateinit var roomHoldRepository: RoomHoldRepository

    private lateinit var underTest: HoldRoomScenario

    @BeforeEach
    fun init() {
        underTest = HoldRoomScenario(
            maxConcurrentHolds = MAX_CONCURRENT_HOLDS,
            roomHoldDuration = roomHoldDuration,
            clock = clock,
            fastFailLock = fastFailLock,
            roomHoldRepository = roomHoldRepository,
        )
    }

    @Test
    fun `should create hold on room for user`() {
        fastFailLock.stubUnblocked(USER_ID, HoldRoomResult.ConcurrentHoldRequest)
        every { clock.instant() } returns instant
        every {
            roomHoldRepository.findHoldsForUser(USER_ID)
        } returns emptyList()
        every {
            roomHoldRepository.createRoomHold(
                userId = USER_ID,
                roomTypeId = ROOM_TYPE_ID,
                roomHoldExpiry = expectedExpiry,
                holdStartDate = holdStartDate,
                holdEndDate = holdEndDate,
                holdIdToRemove = null,
            )
        } returns CreateRoomHoldResult.RoomHoldCreated(ROOM_HOLD_ID)

        val result = underTest.run(
            HoldRoomDetail(
                userId = USER_ID,
                roomTypeId = ROOM_TYPE_ID,
                holdStartDate = holdStartDate,
                holdEndDate = holdEndDate,
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                HoldRoomResult.RoomHoldCreated(
                    roomHoldId = ROOM_HOLD_ID,
                    holdExpiry = expectedExpiry,
                    removedRoomHoldId = null,
                ),
            )
            s.check {
                verify(exactly = 1) {
                    fastFailLock.execute(eq(USER_ID), eq(HoldRoomResult.ConcurrentHoldRequest), any())
                }
            }
            s.check {
                verify(exactly = 1) {
                    clock.instant()
                }
            }
            s.check {
                verify(exactly = 1) {
                    roomHoldRepository.findHoldsForUser(USER_ID)
                }
            }
            s.check {
                verify(exactly = 1) {
                    roomHoldRepository.createRoomHold(
                        userId = USER_ID,
                        roomTypeId = ROOM_TYPE_ID,
                        roomHoldExpiry = expectedExpiry,
                        holdStartDate = holdStartDate,
                        holdEndDate = holdEndDate,
                        holdIdToRemove = null,
                    )
                }
            }
            s.check {
                confirmVerified(
                    clock,
                    fastFailLock,
                    roomHoldRepository,
                )
            }
        }
    }

    @Test
    fun `should replace an existing hold on the same room`() {
        fastFailLock.stubUnblocked(USER_ID, HoldRoomResult.ConcurrentHoldRequest)
        every { clock.instant() } returns instant
        every {
            roomHoldRepository.findHoldsForUser(USER_ID)
        } returns listOf(
            RoomHold(
                roomHoldId = EXISTING_HOLD_ID1,
                userId = USER_ID,
                roomTypeId = ROOM_TYPE_ID,
                holdExpiry = existingHoldExpiry1,
            ),
        )
        every {
            roomHoldRepository.createRoomHold(
                userId = USER_ID,
                roomTypeId = ROOM_TYPE_ID,
                roomHoldExpiry = expectedExpiry,
                holdStartDate = holdStartDate,
                holdEndDate = holdEndDate,
                holdIdToRemove = EXISTING_HOLD_ID1,
            )
        } returns CreateRoomHoldResult.RoomHoldCreated(ROOM_HOLD_ID)

        val result = underTest.run(
            HoldRoomDetail(
                userId = USER_ID,
                roomTypeId = ROOM_TYPE_ID,
                holdStartDate = holdStartDate,
                holdEndDate = holdEndDate,
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                HoldRoomResult.RoomHoldCreated(
                    roomHoldId = ROOM_HOLD_ID,
                    holdExpiry = expectedExpiry,
                    removedRoomHoldId = EXISTING_HOLD_ID1,
                ),
            )
            s.check {
                verify(exactly = 1) {
                    fastFailLock.execute(eq(USER_ID), eq(HoldRoomResult.ConcurrentHoldRequest), any())
                }
            }
            s.check {
                verify(exactly = 1) {
                    clock.instant()
                }
            }
            s.check {
                verify(exactly = 1) {
                    roomHoldRepository.findHoldsForUser(USER_ID)
                }
            }
            s.check {
                verify(exactly = 1) {
                    roomHoldRepository.createRoomHold(
                        userId = USER_ID,
                        roomTypeId = ROOM_TYPE_ID,
                        roomHoldExpiry = expectedExpiry,
                        holdStartDate = holdStartDate,
                        holdEndDate = holdEndDate,
                        holdIdToRemove = EXISTING_HOLD_ID1,
                    )
                }
            }
            s.check {
                confirmVerified(
                    clock,
                    fastFailLock,
                    roomHoldRepository,
                )
            }
        }
    }

    @Test
    fun `should allow multiple holds to be created provided they are on different rooms`() {
        fastFailLock.stubUnblocked(USER_ID, HoldRoomResult.ConcurrentHoldRequest)
        every { clock.instant() } returns instant
        every {
            roomHoldRepository.findHoldsForUser(USER_ID)
        } returns listOf(
            RoomHold(
                roomHoldId = EXISTING_HOLD_ID1,
                userId = USER_ID,
                roomTypeId = ALTERNATE_ROOM_TYPE_ID1,
                holdExpiry = existingHoldExpiry1,
            ),
        )
        every {
            roomHoldRepository.createRoomHold(
                userId = USER_ID,
                roomTypeId = ROOM_TYPE_ID,
                roomHoldExpiry = expectedExpiry,
                holdStartDate = holdStartDate,
                holdEndDate = holdEndDate,
                holdIdToRemove = null,
            )
        } returns CreateRoomHoldResult.RoomHoldCreated(ROOM_HOLD_ID)

        val result = underTest.run(
            HoldRoomDetail(
                userId = USER_ID,
                roomTypeId = ROOM_TYPE_ID,
                holdStartDate = holdStartDate,
                holdEndDate = holdEndDate,
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                HoldRoomResult.RoomHoldCreated(
                    roomHoldId = ROOM_HOLD_ID,
                    holdExpiry = expectedExpiry,
                    removedRoomHoldId = null,
                ),
            )
            s.check {
                verify(exactly = 1) {
                    fastFailLock.execute(eq(USER_ID), eq(HoldRoomResult.ConcurrentHoldRequest), any())
                }
            }
            s.check {
                verify(exactly = 1) {
                    clock.instant()
                }
            }
            s.check {
                verify(exactly = 1) {
                    roomHoldRepository.findHoldsForUser(USER_ID)
                }
            }
            s.check {
                verify(exactly = 1) {
                    roomHoldRepository.createRoomHold(
                        userId = USER_ID,
                        roomTypeId = ROOM_TYPE_ID,
                        roomHoldExpiry = expectedExpiry,
                        holdStartDate = holdStartDate,
                        holdEndDate = holdEndDate,
                        holdIdToRemove = null,
                    )
                }
            }
            s.check {
                confirmVerified(
                    clock,
                    fastFailLock,
                    roomHoldRepository,
                )
            }
        }
    }

    /**
     * Extended description:
     *
     * Should limit the total number of holds for a user by deleting the hold with the earliest expiry
     * when the hold count threshold is reached
     */
    @Test
    fun `should limit the total number of holds for a user`() {
        fastFailLock.stubUnblocked(USER_ID, HoldRoomResult.ConcurrentHoldRequest)
        every { clock.instant() } returns instant
        every {
            roomHoldRepository.findHoldsForUser(USER_ID)
        } returns listOf(
            RoomHold(
                roomHoldId = EXISTING_HOLD_ID1,
                userId = USER_ID,
                roomTypeId = ALTERNATE_ROOM_TYPE_ID1,
                holdExpiry = existingHoldExpiry1,
            ),
            RoomHold(
                roomHoldId = EXISTING_HOLD_ID2,
                userId = USER_ID,
                roomTypeId = ALTERNATE_ROOM_TYPE_ID2,
                holdExpiry = existingHoldExpiry2,
            ),
            RoomHold(
                roomHoldId = EXISTING_HOLD_ID3,
                userId = USER_ID,
                roomTypeId = ALTERNATE_ROOM_TYPE_ID3,
                holdExpiry = existingHoldExpiry3,
            ),
        ).shuffled()
        every {
            roomHoldRepository.createRoomHold(
                userId = USER_ID,
                roomTypeId = ROOM_TYPE_ID,
                roomHoldExpiry = expectedExpiry,
                holdStartDate = holdStartDate,
                holdEndDate = holdEndDate,
                holdIdToRemove = EXISTING_HOLD_ID1,
            )
        } returns CreateRoomHoldResult.RoomHoldCreated(ROOM_HOLD_ID)

        val result = underTest.run(
            HoldRoomDetail(
                userId = USER_ID,
                roomTypeId = ROOM_TYPE_ID,
                holdStartDate = holdStartDate,
                holdEndDate = holdEndDate,
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                HoldRoomResult.RoomHoldCreated(
                    roomHoldId = ROOM_HOLD_ID,
                    holdExpiry = expectedExpiry,
                    removedRoomHoldId = EXISTING_HOLD_ID1,
                ),
            )
            s.check {
                verify(exactly = 1) {
                    fastFailLock.execute(eq(USER_ID), eq(HoldRoomResult.ConcurrentHoldRequest), any())
                }
            }
            s.check {
                verify(exactly = 1) {
                    clock.instant()
                }
            }
            s.check {
                verify(exactly = 1) {
                    roomHoldRepository.findHoldsForUser(USER_ID)
                }
            }
            s.check {
                verify(exactly = 1) {
                    roomHoldRepository.createRoomHold(
                        userId = USER_ID,
                        roomTypeId = ROOM_TYPE_ID,
                        roomHoldExpiry = expectedExpiry,
                        holdStartDate = holdStartDate,
                        holdEndDate = holdEndDate,
                        holdIdToRemove = EXISTING_HOLD_ID1,
                    )
                }
            }
            s.check {
                confirmVerified(
                    clock,
                    fastFailLock,
                    roomHoldRepository,
                )
            }
        }
    }

    @Test
    fun `should return StockNotAvailable when there is not enough available stock to create the hold`() {
        fastFailLock.stubUnblocked(USER_ID, HoldRoomResult.ConcurrentHoldRequest)
        every { clock.instant() } returns instant
        every {
            roomHoldRepository.findHoldsForUser(USER_ID)
        } returns emptyList()
        every {
            roomHoldRepository.createRoomHold(
                userId = USER_ID,
                roomTypeId = ROOM_TYPE_ID,
                roomHoldExpiry = expectedExpiry,
                holdStartDate = holdStartDate,
                holdEndDate = holdEndDate,
                holdIdToRemove = null,
            )
        } returns CreateRoomHoldResult.StockNotAvailable

        val result = underTest.run(
            HoldRoomDetail(
                userId = USER_ID,
                roomTypeId = ROOM_TYPE_ID,
                holdStartDate = holdStartDate,
                holdEndDate = holdEndDate,
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(HoldRoomResult.StockNotAvailable)
            s.check {
                verify(exactly = 1) {
                    fastFailLock.execute(eq(USER_ID), eq(HoldRoomResult.ConcurrentHoldRequest), any())
                }
            }
            s.check {
                verify(exactly = 1) {
                    clock.instant()
                }
            }
            s.check {
                verify(exactly = 1) {
                    roomHoldRepository.findHoldsForUser(USER_ID)
                }
            }
            s.check {
                verify(exactly = 1) {
                    roomHoldRepository.createRoomHold(
                        userId = USER_ID,
                        roomTypeId = ROOM_TYPE_ID,
                        roomHoldExpiry = expectedExpiry,
                        holdStartDate = holdStartDate,
                        holdEndDate = holdEndDate,
                        holdIdToRemove = null,
                    )
                }
            }
            s.check {
                confirmVerified(
                    clock,
                    fastFailLock,
                    roomHoldRepository,
                )
            }
        }
    }

    @Test
    fun `should return ConcurrentHoldRequest when lock cannot be acquired`() {
        fastFailLock.stubConflict(USER_ID, HoldRoomResult.ConcurrentHoldRequest)

        val result = underTest.run(
            HoldRoomDetail(
                userId = USER_ID,
                roomTypeId = ROOM_TYPE_ID,
                holdStartDate = holdStartDate,
                holdEndDate = holdEndDate,
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(HoldRoomResult.ConcurrentHoldRequest)
            s.check {
                verify(exactly = 1) {
                    fastFailLock.execute(eq(USER_ID), eq(HoldRoomResult.ConcurrentHoldRequest), any())
                }
            }
            s.check {
                confirmVerified(
                    clock,
                    fastFailLock,
                    roomHoldRepository,
                )
            }
        }
    }
}
