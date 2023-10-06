package me.aburke.hotelbooking.facade.rest.api.customer.v1.roomtype

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.client
import me.aburke.hotelbooking.facade.rest.parseResponse
import me.aburke.hotelbooking.ports.scenario.room.RoomAvailability
import me.aburke.hotelbooking.ports.scenario.room.RoomDescription
import me.aburke.hotelbooking.ports.scenario.room.RoomTypeInfo
import me.aburke.hotelbooking.rest.client.api.CustomerApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.invoker.ApiResponse
import me.aburke.hotelbooking.rest.client.model.DateAvailability
import me.aburke.hotelbooking.rest.client.model.FetchRoomsAvailability200Response
import me.aburke.hotelbooking.rest.client.model.ProblemResponse
import me.aburke.hotelbooking.rest.client.model.ProblemResponseExtendedDetailsInner
import me.aburke.hotelbooking.rest.client.model.RoomTypeWithAvailability
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import me.aburke.hotelbooking.rest.client.model.RoomDescription as ClientRoomDescription

class FetchRoomsAvailabilityTest : AbstractFetchRoomsAvailabilityHandlerTest() {

    @Test
    fun `should fetch room availabilities for unauthenticated user`() = test(javalin) { _, _ ->
        `RUN should fetch room availabilities for unauthenticated user`(
            object : TestRequest<ApiResponse<FetchRoomsAvailability200Response>>() {
                override fun makeRequest(): ApiResponse<FetchRoomsAvailability200Response> =
                    CustomerApi(javalin.client()).fetchRoomsAvailabilityWithHttpInfo(searchRangeStart, searchRangeEnd)

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.statusCode).isEqualTo(200)
                    s.assertThat(response.data).isEqualTo(
                        FetchRoomsAvailability200Response().apply {
                            roomTypes = roomTypeInfo.map { it.toResponse() }
                        },
                    )
                }
            },
        )
    }

    @Test
    fun `should fetch room availabilities for authenticated user`() = test(javalin) { _, _ ->
        `RUN should fetch room availabilities for authenticated user`(
            object : TestRequest<ApiResponse<FetchRoomsAvailability200Response>>() {
                override fun makeRequest(): ApiResponse<FetchRoomsAvailability200Response> =
                    CustomerApi(javalin.client(session.sessionId)).fetchRoomsAvailabilityWithHttpInfo(
                        searchRangeStart,
                        searchRangeEnd,
                    )

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.statusCode).isEqualTo(200)
                    s.assertThat(response.data).isEqualTo(
                        FetchRoomsAvailability200Response().apply {
                            roomTypes = roomTypeInfo.map { it.toResponse() }
                        },
                    )
                }
            },
        )
    }

    @Test
    fun `should return 400 if date is not provided`() = test(javalin) { _, _ ->
        `RUN should return 400 if date is in invalid format or missing`(
            object : TestRequest<ApiException>() {
                override fun makeRequest(): ApiException = assertThrows {
                    CustomerApi(javalin.client()).fetchRoomsAvailabilityWithHttpInfo(
                        searchRangeStart,
                        null,
                    )
                }

                override fun makeAssertions(s: SoftAssertions) {
                    val responseBody = response.responseBody.parseResponse<ProblemResponse>()
                    s.assertThat(response.code).isEqualTo(400)
                    s.assertThat(response.responseHeaders["Content-Type"])
                        .containsExactly("application/problem+json;charset=utf-8")
                    s.assertThat(responseBody).isEqualTo(
                        ProblemResponse().apply {
                            title = "Invalid Parameters"
                            code = "BAD_REQUEST"
                            status = 400
                            detail = "URL parameters are badly formed"
                            instance = "/api/customer/v1/room-type/availability"
                            extendedDetails = listOf(
                                ProblemResponseExtendedDetailsInner().also {
                                    it.code = "INVALID_FORMAT_DATE"
                                    it.detail = "availability_range_end"
                                },
                            )
                        },
                    )
                }
            },
        )
    }

    @Test
    fun `should return 401 if invalid session ID provided`() {
        fail("TODO")
    }

    @Test
    fun `should return 403 if authenticated session does not have CUSTOMER permission`() {
        fail("TODO")
    }
}

private fun RoomTypeInfo.toResponse() = RoomTypeWithAvailability().also {
    it.roomTypeId = roomTypeId
    it.description = description.toResponse()
    it.roomAvailability = roomAvailability.map { a -> a.toResponse() }
}

private fun RoomDescription.toResponse() = ClientRoomDescription().also {
    it.title = title
    it.pricePerNight = pricePerNight
    it.description = description
    it.imageUrls = imageUrls
}

private fun RoomAvailability.toResponse() = DateAvailability().also {
    it.date = date
    it.available = available
}
