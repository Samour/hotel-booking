package me.aburke.hotelbooking.client

import me.aburke.hotelbooking.ports.repository.UserCredentialRecord
import me.aburke.hotelbooking.ports.repository.UserRecord
import me.aburke.hotelbooking.repository.postgres.executeQueryWithRollback
import me.aburke.hotelbooking.repository.postgres.user.toUserRoles
import java.sql.Connection
import java.time.LocalDate

fun Connection.readAllUsers() = prepareStatement(
    """
        select u.user_id, u.user_roles, u.name, c.login_id, c.password_hash
        from app_user u
        left outer join user_credential c on c.user_id = u.user_id
    """.trimIndent(),
).executeQueryWithRollback().let {
    val records = mutableListOf<UserRecord>()
    while (it.next()) {
        val userId = it.getString("user_id")
        val userRoles = it.getArray("user_roles").toUserRoles()
        val name = it.getString("name")
        val loginId = it.getString("login_id")
        val passwordHash = it.getString("password_hash")

        records.add(
            UserRecord(
                userId = userId,
                userRoles = userRoles,
                name = name,
                credential = if (loginId != null && passwordHash != null) {
                    UserCredentialRecord(
                        loginId = loginId,
                        passwordHash = passwordHash,
                    )
                } else {
                    null
                },
            ),
        )
    }

    records
}

data class RoomRecord(
    val roomTypeId: String,
    val hotelId: String,
    val stockLevel: Int,
    val title: String,
    val description: String,
    val imageUrls: List<String>,
)

fun Connection.loadAllRooms(): List<RoomRecord> {
    val results = prepareStatement(
        """
                select r.room_type_id, r.hotel_id, r.stock_level, rd.title, rd.description, rd.image_urls
                from room_type r
                join room_type_description rd on rd.room_type_id = r.room_type_id
        """.trimIndent(),
    ).executeQuery()

    val rooms = mutableListOf<RoomRecord>()
    while (results.next()) {
        rooms.add(
            RoomRecord(
                roomTypeId = results.getString("room_type_id"),
                hotelId = results.getString("hotel_id"),
                stockLevel = results.getInt("stock_level"),
                title = results.getString("title"),
                description = results.getString("description"),
                imageUrls = listOf(*(results.getArray("image_urls").array as Array<String>)),
            ),
        )
    }

    return rooms
}

data class RoomStockRecord(
    val roomTypeId: String,
    val date: LocalDate,
    val stockLevel: Int,
)

fun Connection.loadAllRoomStocks(): List<RoomStockRecord> {
    val results = prepareStatement(
        """
                select room_type_id, date, stock_level
                from room_stock
        """.trimIndent(),
    ).executeQuery()

    val records = mutableListOf<RoomStockRecord>()
    while (results.next()) {
        records.add(
            RoomStockRecord(
                roomTypeId = results.getString("room_type_id"),
                date = LocalDate.parse(results.getString("date")),
                stockLevel = results.getInt("stock_level"),
            ),
        )
    }

    return records
}
