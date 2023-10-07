package me.aburke.hotelbooking.repository.postgres.room.hold

import me.aburke.hotelbooking.repository.postgres.executeQueryWithRollback
import java.sql.Connection
import java.time.Instant
import java.time.LocalDate

data class RoomHoldRow(
    val roomHoldId: String,
    val userId: String,
    val holdExpiry: Instant,
    val roomTypeId: String,
    val date: LocalDate,
)

fun Connection.readAllHoldsForUser(userId: String): List<RoomHoldRow> = prepareStatement(
    """
        select h.room_hold_id, h.user_id, h.hold_expiry, rs.room_type_id, rs.date
        from room_hold h
        join room_stock_hold rsh on rsh.room_hold_id = h.room_hold_id
        join room_stock rs on rs.room_stock_id = rsh.room_stock_id
        where h.user_id = ?
        
    """.trimIndent(),
).apply {
    setString(1, userId)
}.executeQueryWithRollback()
    .run {
        val rows = mutableListOf<RoomHoldRow>()
        while (next()) {
            rows.add(
                RoomHoldRow(
                    roomHoldId = getString("room_hold_id"),
                    userId = getString("user_id"),
                    holdExpiry = Instant.parse(getString("hold_expiry")),
                    roomTypeId = getString("room_type_id"),
                    date = LocalDate.parse(getString("date")),
                ),
            )
        }

        rows
    }
