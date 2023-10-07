package me.aburke.hotelbooking.repository.postgres.room.hold

import me.aburke.hotelbooking.ports.repository.CreateRoomHoldResult
import me.aburke.hotelbooking.repository.postgres.TestRooms
import me.aburke.hotelbooking.repository.postgres.createApp
import me.aburke.hotelbooking.repository.postgres.executeBatchWithRollback
import me.aburke.hotelbooking.repository.postgres.insertTestRooms
import me.aburke.hotelbooking.repository.postgres.setRoomStockLevel
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.koin.core.KoinApplication
import java.sql.Connection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Disabled
class PostgresRoomHoldRepositoryConcurrencyTest {

    private lateinit var app: KoinApplication
    private lateinit var connection: Connection

    @BeforeEach
    fun init() {
        app = createApp()
        connection = app.koin.get()

        connection.insertTestRooms(TestRooms.rooms)
        connection.setRoomStockLevel(TestRooms.UserWithHolds.roomHold.roomTypeId, 1)
        connection.insertUserIds(
            TestRooms.UserWithHolds.userId,
            TestRooms.UserWithExpiredHold.userId,
        )
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    @Suppress("ktlint:max-line-length")
    fun `should prevent hold from being created when stock is unavailable due to another hold being created concurrently`() {
        val preCommitLatch = CountDownLatch(1)
        val proceedToCommitLatch = CountDownLatch(1)

        val firstTxnRepository = createRepository {
            preCommitLatch.countDown()
            proceedToCommitLatch.await(30, TimeUnit.SECONDS).also {
                assertThat(it).isTrue()
            }
        }
        val secondTxnRepository = createRepository()

        val holdDates = (5L..9L).map {
            TestRooms.stockBaseDate.plusDays(it)
        }

        var firstTxnResult: CreateRoomHoldResult? = null
        val firstTxnThread = Thread {
            firstTxnResult = firstTxnRepository.createRoomHold(
                userId = TestRooms.UserWithHolds.userId,
                roomTypeId = TestRooms.UserWithHolds.roomHold.roomTypeId,
                roomHoldExpiry = TestRooms.UserWithHolds.roomHold.holdExpiry,
                holdStartDate = holdDates.first(),
                holdEndDate = holdDates.last(),
                holdIdToRemove = null,
            )
        }.apply { start() }

        preCommitLatch.await(30, TimeUnit.SECONDS).also {
            assertThat(it).isTrue()
        }

        var secondTxnResult: CreateRoomHoldResult? = null
        val secondTxnThread = Thread {
            secondTxnResult = secondTxnRepository.createRoomHold(
                userId = TestRooms.UserWithExpiredHold.userId,
                roomTypeId = TestRooms.UserWithHolds.roomHold.roomTypeId,
                roomHoldExpiry = TestRooms.UserWithHolds.roomHold.holdExpiry,
                holdStartDate = holdDates.first(),
                holdEndDate = holdDates.last(),
                holdIdToRemove = null,
            )
        }.apply { start() }
        Thread.sleep(200) // Give the second txn time to execute, hopefully run into Postgres row lock

        // Release the first txn
        // This should hopefully result in txn 1 committing first, which then unblocks txn 2 to continue
        // with visibility of the changes made by txn 1
        proceedToCommitLatch.countDown()
        firstTxnThread.join(30_000)
        secondTxnThread.join(30_000)

        val firstUserHolds = connection.readAllHoldsForUser(TestRooms.UserWithHolds.userId)
        val secondUserHolds = connection.readAllHoldsForUser(TestRooms.UserWithExpiredHold.userId)

        val createdRoomHoldId = (firstTxnResult as? CreateRoomHoldResult.RoomHoldCreated)?.roomHoldId
        assertSoftly { s ->
            s.assertThat(firstTxnResult).isInstanceOf(CreateRoomHoldResult.RoomHoldCreated::class.java)
            s.assertThat(secondTxnResult).isEqualTo(CreateRoomHoldResult.StockNotAvailable)
            s.assertThat(firstUserHolds).containsExactlyInAnyOrder(
                *holdDates.map {
                    RoomHoldRow(
                        roomHoldId = createdRoomHoldId ?: "",
                        userId = TestRooms.UserWithHolds.userId,
                        holdExpiry = TestRooms.UserWithHolds.additionalRoomHold.holdExpiry,
                        roomTypeId = TestRooms.UserWithHolds.additionalRoomHold.roomTypeId,
                        date = it,
                    )
                }.toTypedArray(),
            )
            s.assertThat(secondUserHolds).isEmpty()
        }
    }

    @Test
    @Suppress("ktlint:max-line-length")
    fun `should prevent hold from being created when stock is unavailable due to stock_level being reduced to 0 concurrently`() {
        fail("TODO")
    }

    @Test
    @Suppress("ktlint:max-line-length")
    fun `should not block a concurrent transaction which relates to different room stock`() {
        fail("TODO")
    }

    private fun createRepository(preCommitHook: (() -> Unit)? = null) = PostgresRoomHoldRepository(
        app.koin.get(),
        app.koin.get(),
        preCommitHook,
    )
}

private fun Connection.insertUserIds(vararg userIds: String) {
    prepareStatement(
        """
            insert into app_user(user_id, user_roles, name)
            values (?, '{}', '')
        """.trimIndent(),
    ).apply {
        userIds.forEach {
            setString(1, it)
            addBatch()
        }
    }.executeBatchWithRollback()
}
