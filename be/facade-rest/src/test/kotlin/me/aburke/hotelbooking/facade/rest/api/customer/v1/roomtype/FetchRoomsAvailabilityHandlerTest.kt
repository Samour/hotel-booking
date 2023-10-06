package me.aburke.hotelbooking.facade.rest.api.customer.v1.roomtype

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.assertThatJson
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import me.aburke.hotelbooking.ports.scenario.room.RoomAvailability
import me.aburke.hotelbooking.ports.scenario.room.RoomTypeInfo
import okhttp3.Response
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

class FetchRoomsAvailabilityHandlerTest : AbstractFetchRoomsAvailabilityHandlerTest() {

    @Test
    fun `should fetch room availabilities for unauthenticated user`() = test(javalin) { _, client ->
        `RUN should fetch room availabilities for unauthenticated user`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.get(
                    "/api/customer/v1/room-type/availability?" +
                        "availability_range_start=$searchRangeStart&availability_range_end=$searchRangeEnd",
                )

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.code).isEqualTo(200)
                    s.assertThat(response.header("Content-Type")).isEqualTo("application/json")
                    s.assertThatJson(response.body?.string()).isEqualTo(expectedJsonResponse())
                }
            },
        )
    }

    @Test
    fun `should fetch room availabilities for authenticated user`() = test(javalin) { _, client ->
        `RUN should fetch room availabilities for authenticated user`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request(
                    "/api/customer/v1/room-type/availability?" +
                        "availability_range_start=$searchRangeStart&availability_range_end=$searchRangeEnd",
                ) { rb ->
                    rb.header("Cookie", "$AUTH_COOKIE_KEY=${session.sessionId}")
                        .get()
                }

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.code).isEqualTo(200)
                    s.assertThat(response.header("Content-Type")).isEqualTo("application/json")
                    s.assertThatJson(response.body?.string()).isEqualTo(expectedJsonResponse())
                }
            },
        )
    }

    @Test
    fun `should return 400 if date is in invalid format`() = test(javalin) { _, client ->
        `RUN should return 400 if date is in invalid format or missing`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.get(
                    "/api/customer/v1/room-type/availability?" +
                        "availability_range_start=2023&availability_range_end=2024",
                )

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.code).isEqualTo(400)
                    s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
                    s.assertThatJson(response.body?.string()).isEqualTo(
                        """
                            {
                                "title": "Invalid Parameters",
                                "code": "BAD_REQUEST",
                                "status": 400,
                                "detail": "URL parameters are badly formed",
                                "instance": "/api/customer/v1/room-type/availability",
                                "extended_details": [
                                    {
                                        "code": "INVALID_FORMAT_DATE",
                                        "detail": "availability_range_start"
                                    },
                                    {
                                        "code": "INVALID_FORMAT_DATE",
                                        "detail": "availability_range_end"
                                    }
                                ]
                            }
                        """.trimIndent(),
                    )
                }
            },
        )
    }

    @Test
    fun `should return 400 if date is not provided`() = test(javalin) { _, client ->
        `RUN should return 400 if date is in invalid format or missing`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.get(
                    "/api/customer/v1/room-type/availability?" +
                        "availability_range_start=$searchRangeStart",
                )

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.code).isEqualTo(400)
                    s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
                    s.assertThatJson(response.body?.string()).isEqualTo(
                        """
                            {
                                "title": "Invalid Parameters",
                                "code": "BAD_REQUEST",
                                "status": 400,
                                "detail": "URL parameters are badly formed",
                                "instance": "/api/customer/v1/room-type/availability",
                                "extended_details": [
                                    {
                                        "code": "INVALID_FORMAT_DATE",
                                        "detail": "availability_range_end"
                                    }
                                ]
                            }
                        """.trimIndent(),
                    )
                }
            },
        )
    }

    @Test
    fun `should return 401 if invalid session ID provided`() = test(javalin) { _, client ->
        `RUN should return 401 if invalid session ID provided`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request(
                    "/api/customer/v1/room-type/availability?" +
                        "availability_range_start=$searchRangeStart&availability_range_end=$searchRangeEnd",
                ) { rb ->
                    rb.header("Cookie", "$AUTH_COOKIE_KEY=${session.sessionId}")
                        .get()
                }

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.code).isEqualTo(401)
                    s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
                    s.assertThatJson(response.body?.string()).isEqualTo(
                        """
                            {
                                "title": "Not Authorized",
                                "code": "UNAUTHORIZED",
                                "status": 401,
                                "detail": "Credentials not provided",
                                "instance": "/api/customer/v1/room-type/availability",
                                "extended_details": []
                            }
                        """.trimIndent(),
                    )
                }
            },
        )
    }

    @Test
    fun `should return 403 if authenticated session does not have CUSTOMER permission`() = test(javalin) { _, client ->
        `RUN should return 403 if authenticated session does not have CUSTOMER permission`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request(
                    "/api/customer/v1/room-type/availability?" +
                        "availability_range_start=$searchRangeStart&availability_range_end=$searchRangeEnd",
                ) { rb ->
                    rb.header("Cookie", "$AUTH_COOKIE_KEY=${session.sessionId}")
                        .get()
                }

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.code).isEqualTo(403)
                    s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
                    s.assertThatJson(response.body?.string()).isEqualTo(
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
            },
        )
    }

    private fun expectedJsonResponse() = """
        {
            "room_types": [${roomTypeInfo.joinToString { it.toJson() }}]
        }
    """.trimIndent()
}

private fun RoomTypeInfo.toJson() = """
    {
        "room_type_id": "$roomTypeId",
        "description": {
            "title": "${description.title}",
            "price_per_night": ${description.pricePerNight},
            "description": "${description.description}",
            "image_urls": [${description.imageUrls.joinToString { "\"$it\"" }}]
        },
        "room_availability": [${roomAvailability.joinToString { it.toJson() }}]
    }
""".trimIndent()

private fun RoomAvailability.toJson() = """
    {
        "date": "$date",
        "available": $available
    }
""".trimIndent()
