package me.aburke.hotelbooking.customer

import me.aburke.hotelbooking.assertThatJson
import me.aburke.hotelbooking.authenticateWith
import me.aburke.hotelbooking.createApp
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.rest.client.api.AdminApi
import me.aburke.hotelbooking.rest.client.api.CustomerApi
import me.aburke.hotelbooking.rest.client.invoker.ApiClient
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.model.AddRoomTypeRequest
import me.aburke.hotelbooking.rest.client.model.DateAvailability
import me.aburke.hotelbooking.rest.client.model.RoomDescription
import me.aburke.hotelbooking.rest.client.model.RoomTypeWithAvailability
import me.aburke.hotelbooking.restTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.koin.core.KoinApplication
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class FetchRoomAvailabilityTest {

    private lateinit var searchRangeStart: LocalDate
    private lateinit var searchRangeEnd: LocalDate

    private lateinit var app: KoinApplication
    private lateinit var instant: Instant

    @BeforeEach
    fun init() {
        createApp().also {
            app = it.first
            instant = it.second
        }

        searchRangeStart = LocalDate.ofInstant(instant, ZoneOffset.UTC)
        searchRangeEnd = searchRangeStart.plusDays(9)
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should return rooms with availability data for unauthenticated user`() = app.restTest { client, cookieJar ->
        client.authenticateWith(UserRole.MANAGE_ROOMS)
        val rooms = client.createRooms()

        cookieJar.clearAllCookies()

        val response = CustomerApi(client).fetchRoomsAvailability(
            searchRangeStart,
            searchRangeEnd,
        )

        assertThat(response.roomTypes).containsExactlyInAnyOrder(
            *rooms.map { it.toRoomTypeWithAvailability() }.toTypedArray(),
        )
    }

    @Disabled
    @Test
    fun `should return rooms that are held by another user as unavailable`() {
        fail("TODO")
    }

    @Test
    fun `should return 403 if authenticated session does not have CUSTOMER permission`() = app.restTest { client, _ ->
        client.authenticateWith(UserRole.MANAGE_ROOMS)
        client.createRooms()

        val response = assertThrows<ApiException> {
            CustomerApi(client).fetchRoomsAvailability(
                searchRangeStart,
                searchRangeEnd,
            )
        }

        SoftAssertions.assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(403)
            s.assertThatJson(response.responseBody).isEqualTo(
                """
                    {
                        "title": "Forbidden",
                        "code": "FORBIDDEN",
                        "status": 403,
                        "detail": "Insufficient permissions to access resource",
                        "instance": "/api/customer/v1/room-type/availability",
                        "extended_details": []
                    }
                """.trimIndent(),
            )
        }
    }

    private fun ApiClient.createRooms(): List<Pair<String, AddRoomTypeRequest>> = (1..3).map { i ->
        val request = AddRoomTypeRequest().apply {
            title = "room-title-$i"
            description = "room-description-$i"
            imageUrls = (1..5).map { j -> "https://test.local/room-$i-image-url-$j" }
            pricePerNight = (10 + i) * 10_00
            stockLevel = 5
        }

        AdminApi(this).addRoomType(request).roomTypeId to request
    }

    private fun Pair<String, AddRoomTypeRequest>.toRoomTypeWithAvailability() = RoomTypeWithAvailability().apply {
        roomTypeId = first
        description = RoomDescription().also {
            it.title = second.title
            it.pricePerNight = second.pricePerNight
            it.description = second.description
            it.imageUrls = second.imageUrls!!
        }
        roomAvailability = (0..9).map { i ->
            DateAvailability().also {
                it.date = searchRangeStart.plusDays(i.toLong())
                it.available = true
            }
        }
    }
}
