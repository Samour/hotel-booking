package me.aburke.hotelbooking.repository.postgres.room.hold

import me.aburke.hotelbooking.ports.repository.CreateRoomHoldResult
import me.aburke.hotelbooking.ports.repository.RoomHold
import me.aburke.hotelbooking.ports.repository.RoomHoldRepository
import me.aburke.hotelbooking.repository.postgres.executeQueryWithRollback
import java.sql.Connection
import java.time.Instant
import java.time.LocalDate

class PostgresRoomHoldRepository(private val connection: Connection) : RoomHoldRepository {

    override fun findHoldsForUser(userId: String): List<RoomHold> =
        connection.loadHoldsForUser(userId)
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
        TODO("Not yet implemented")
    }
}
