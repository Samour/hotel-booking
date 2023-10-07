package me.aburke.hotelbooking.customer

import me.aburke.hotelbooking.TestContext
import me.aburke.hotelbooking.assertThatJson
import me.aburke.hotelbooking.authenticateWith
import me.aburke.hotelbooking.createApp
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.rest.client.api.AdminUnstableApi
import me.aburke.hotelbooking.rest.client.api.CustomerUnstableApi
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
import java.time.LocalDate
import java.time.ZoneOffset

class FetchRoomAvailabilityTest {

    private lateinit var searchRangeStart: LocalDate
    private lateinit var searchRangeEnd: LocalDate

    private lateinit var testContext: TestContext

    @BeforeEach
    fun init() {
        testContext = createApp()

        searchRangeStart = LocalDate.ofInstant(testContext.time, ZoneOffset.UTC)
        searchRangeEnd = searchRangeStart.plusDays(9)
    }

    @AfterEach
    fun cleanUp() = testContext.app.close()

    @Test
    fun `should return rooms with availability data for unauthenticated user`() =
        testContext.app.restTest { client, cookieJar ->
            client.authenticateWith(UserRole.MANAGE_ROOMS)
            val rooms = client.createRooms()

            cookieJar.clearAllCookies()

            val response = CustomerUnstableApi(client).fetchRoomsAvailability(
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
    fun `should return 403 if authenticated session does not have CUSTOMER permission`() =
        testContext.app.restTest { client, _ ->
            client.authenticateWith(UserRole.MANAGE_ROOMS)
            client.createRooms()

            val response = assertThrows<ApiException> {
                CustomerUnstableApi(client).fetchRoomsAvailability(
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
                        "instance": "/api/customer/v0/room-type/availability",
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

        AdminUnstableApi(this).addRoomType(request).roomTypeId to request
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
