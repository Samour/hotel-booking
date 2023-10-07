package me.aburke.hotelbooking.repository.postgres.room.hold

import java.sql.Connection
import java.time.Instant
import java.time.LocalDate

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

// Write queries

fun Connection.insertRoomHold(
    roomHoldId: String,
    userId: String,
    roomHoldExpiry: Instant,
) = prepareStatement(
    """
    insert into room_hold(room_hold_id, user_id, hold_expiry)
    values (?, ?, ?)
    """.trimIndent(),
).apply {
    setString(1, roomHoldId)
    setString(2, userId)
    setString(3, "$roomHoldExpiry")
}

fun Connection.createRoomStockHolds(
    roomHoldId: String,
    roomTypeId: String,
    holdStartDate: LocalDate,
    holdEndDate: LocalDate,
) = prepareStatement(
    """
        insert into room_stock_hold(room_stock_hold_id, room_hold_id, room_stock_id)
        select gen_random_uuid(), ?, rs.room_stock_id
            from room_stock rs
            where rs.room_type_id = ? and rs.date <= ? and rs.date >= ?
    """.trimIndent(),
).apply {
    setString(1, roomHoldId)
    setString(2, roomTypeId)
    setString(3, "$holdEndDate")
    setString(4, "$holdStartDate")
}
