package me.aburke.hotelbooking.repository.postgres.room.hold

import me.aburke.hotelbooking.ports.repository.CreateRoomHoldResult
import me.aburke.hotelbooking.ports.repository.RoomHold
import me.aburke.hotelbooking.ports.repository.RoomHoldRepository
import me.aburke.hotelbooking.repository.postgres.executeQueryWithRollback
import org.postgresql.util.PSQLException
import java.sql.Connection
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID.randomUUID

class PostgresRoomHoldRepository(
    private val clock: Clock,
    private val connection: Connection,
    private val preCommitHook: (() -> Unit)? = null,
) : RoomHoldRepository {

    override fun findHoldsForUser(userId: String): List<RoomHold> =
        connection.loadHoldsForUser(userId, clock.instant())
            .executeQueryWithRollback()
            .toRoomHolds()

    override fun createRoomHold(
        userId: String,
        roomTypeId: String,
        roomHoldExpiry: Instant,
        holdStartDate: LocalDate,
        holdEndDate: LocalDate,
        holdIdToRemove: String?,
    ): CreateRoomHoldResult {
        val roomHoldId = randomUUID().toString()
        val expectedStockHoldRows = holdStartDate.until(holdEndDate).days + 1

        val roomHoldQuery = connection.insertRoomHold(
            roomHoldId = roomHoldId,
            userId = userId,
            roomHoldExpiry = roomHoldExpiry,
        )
        val roomStockHoldQuery = connection.createRoomStockHolds(
            roomHoldId = roomHoldId,
            roomTypeId = roomTypeId,
            holdStartDate = holdStartDate,
            holdEndDate = holdEndDate,
            now = clock.instant(),
        )
        val deleteHoldQuery = holdIdToRemove?.let { connection.deleteRoomHold(it) }

        try {
            deleteHoldQuery?.executeUpdate()
            roomHoldQuery.executeUpdate()
            if (roomStockHoldQuery.executeUpdate() < expectedStockHoldRows) {
                connection.rollback()
                return CreateRoomHoldResult.StockNotAvailable
            }
            preCommitHook?.let { it() }
            connection.commit()
        } catch (e: PSQLException) {
            connection.rollback()
            throw e
        }

        return CreateRoomHoldResult.RoomHoldCreated(roomHoldId)
    }
}
