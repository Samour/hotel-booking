package me.aburke.hotelbooking.repository.postgres.room.hold

import java.sql.Connection
import java.time.Instant

// Read queries

fun Connection.loadHoldsForUser(
    userId: String,
    currentTime: Instant,
) = prepareStatement(
    """
        select distinct on (h.room_hold_id) h.room_hold_id, h.user_id, h.hold_expiry, rs.room_type_id
        from room_hold h
        join room_stock_hold rsh on rsh.room_hold_id = h.room_hold_id
        join room_stock rs on rs.room_stock_id = rsh.room_stock_id
        where h.user_id = ? and h.hold_expiry > ?
    """.trimIndent(),
).apply {
    setString(1, userId)
    setString(2, "$currentTime")
}
