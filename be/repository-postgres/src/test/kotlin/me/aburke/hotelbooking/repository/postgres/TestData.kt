package me.aburke.hotelbooking.repository.postgres

import me.aburke.hotelbooking.ports.repository.RoomHold
import me.aburke.hotelbooking.ports.repository.RoomStockRecord
import me.aburke.hotelbooking.ports.repository.RoomTypeDescriptionRecord
import me.aburke.hotelbooking.ports.repository.RoomTypeRecord
import java.time.Instant
import java.time.LocalDate
import java.util.*

val clockTime = Instant.parse("2023-08-09T09:00:00Z")

val hotelId = "test-hotel"
val hotelTimeZone = TimeZone.getTimeZone("Australia/Sydney")

object TestRooms {

    val stockBaseDate = LocalDate.parse("2023-08-08")

    val rooms = (1..15).map {
        RoomTypeRecord(
            roomTypeId = "room-type-id-$it",
            description = RoomTypeDescriptionRecord(
                title = "room-title-$it",
                pricePerNight = 150_00,
                description = "room-description-$it",
                imageUrls = (1..3).map { j ->
                    "room-type-$it-image-$j"
                },
            ),
            stockLevels = (1..30).map { j ->
                RoomStockRecord(
                    date = stockBaseDate.plusDays(j.toLong()),
                    stockLevel = j % 5,
                )
            },
        )
    }

    val holds = mapOf<String, Map<LocalDate, Int>>(
        "room-type-id-1" to mapOf(
            LocalDate.parse("2023-08-15") to 1,
            LocalDate.parse("2023-08-16") to 1,
            LocalDate.parse("2023-08-17") to 2,
            LocalDate.parse("2023-08-19") to 1,
            LocalDate.parse("2023-08-20") to 1,
        ),
        "room-type-id-2" to mapOf(
            LocalDate.parse("2023-08-20") to 1,
            LocalDate.parse("2023-08-21") to 1,
            LocalDate.parse("2023-08-22") to 1,
        ),
    )

    object UserWithHolds {
        val userId = "test-hold-user-1"

        val visibleHolds = mapOf<String, Map<LocalDate, Int>>(
            "room-type-id-1" to mapOf(
                LocalDate.parse("2023-08-17") to 1,
                LocalDate.parse("2023-08-19") to 1,
                LocalDate.parse("2023-08-20") to 1,
            ),
            "room-type-id-2" to mapOf(
                LocalDate.parse("2023-08-20") to 1,
                LocalDate.parse("2023-08-21") to 1,
                LocalDate.parse("2023-08-22") to 1,
            ),
        )

        val roomHold = RoomHold(
            roomHoldId = "test-hold-id-1",
            userId = userId,
            roomTypeId = "room-type-id-1",
            holdExpiry = Instant.parse("2023-08-09T15:00:00Z"),
        )
        val roomHoldDates = (7L..9L).map { stockBaseDate.plusDays(it) }

        val additionalRoomHold = RoomHold(
            roomHoldId = "test-hold-id-5",
            userId = userId,
            roomTypeId = "room-type-id-5",
            holdExpiry = Instant.parse("2023-08-09T15:03:00Z"),
        )
    }

    object UserWithExpiredHold {
        val userId = "test-hold-user-3"
        val visibleRoomHold = RoomHold(
            roomHoldId = "test-hold-id-3",
            userId = userId,
            roomTypeId = "room-type-id-2",
            holdExpiry = Instant.parse("2023-08-09T15:02:00Z"),
        )
    }

    object RoomHeldByAnotherUser {
        val roomTypeId = "room-type-id-2"
        val heldDates = listOf(
            LocalDate.parse("2023-08-20"),
            LocalDate.parse("2023-08-21"),
            LocalDate.parse("2023-08-22"),
        )
    }
}

fun Map<String, Map<LocalDate, Int>>.getHoldCount(roomTypeId: String, date: LocalDate): Int =
    this[roomTypeId]?.get(date) ?: 0
