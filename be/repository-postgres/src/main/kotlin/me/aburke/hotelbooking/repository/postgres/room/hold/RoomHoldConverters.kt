package me.aburke.hotelbooking.repository.postgres.room.hold

import me.aburke.hotelbooking.ports.repository.RoomHold
import java.sql.ResultSet
import java.time.Instant

fun ResultSet.toRoomHolds(): List<RoomHold> {
    val results = mutableListOf<RoomHold>()

    while (next()) {
        results.add(
            RoomHold(
                roomHoldId = getString("room_hold_id"),
                userId = getString("user_id"),
                roomTypeId = getString("room_type_id"),
                holdExpiry = Instant.parse(getString("hold_expiry")),
            ),
        )
    }

    return results
}
