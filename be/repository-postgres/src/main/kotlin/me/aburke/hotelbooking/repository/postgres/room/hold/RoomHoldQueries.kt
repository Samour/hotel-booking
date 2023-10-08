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

fun Connection.lockRoomStockForHolds(
    roomTypeId: String,
    holdStartDate: LocalDate,
    holdEndDate: LocalDate,
) = prepareStatement(
    """
        select room_stock_id
        from room_stock
        where room_type_id = ? and date <= ? and date >= ?
        for update
    """.trimIndent(),
).apply {
    setString(1, roomTypeId)
    setString(2, "$holdEndDate")
    setString(3, "$holdStartDate")
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
    now: Instant,
) = prepareStatement(
    """
        insert into room_stock_hold(room_stock_hold_id, room_hold_id, room_stock_id)
        select gen_random_uuid(), ?, rs.room_stock_id
            from room_stock rs
            left outer join (
                select rsh.room_stock_id, count(rsh.room_stock_hold_id) as held_stock_count
                from room_stock_hold rsh
                join room_hold rh on rh.room_hold_id = rsh.room_hold_id
                where rh.hold_expiry > ?
                group by rsh.room_stock_id
            ) held_stock on held_stock.room_stock_id = rs.room_stock_id
            where rs.room_type_id = ? and rs.date <= ? and rs.date >= ?
                and rs.stock_level - coalesce(held_stock.held_stock_count, 0) > 0
    """.trimIndent(),
).apply {
    setString(1, roomHoldId)
    setString(2, "$now")
    setString(3, roomTypeId)
    setString(4, "$holdEndDate")
    setString(5, "$holdStartDate")
}

fun Connection.deleteRoomHold(
    roomHoldId: String,
) = prepareStatement(
    """
        delete from room_hold where room_hold_id = ?
    """.trimIndent(),
).apply {
    setString(1, roomHoldId)
}
