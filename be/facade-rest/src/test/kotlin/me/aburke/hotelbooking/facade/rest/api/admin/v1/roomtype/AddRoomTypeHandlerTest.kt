package me.aburke.hotelbooking.facade.rest.api.admin.v1.roomtype

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.assertThatJson
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class AddRoomTypeHandlerTest : AbstractAddRoomTypeTest() {

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should create new room type`(withImages: Boolean) = test(javalin) { _, client ->
        `RUN should create new room type`(
            withImages,
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/admin/v1/room-type") { rb ->
                    rb.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                        .header("Content-Type", "application/json")
                        .post(makeRequestBody(withImages))
                }

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.code).isEqualTo(201)
                    s.assertThat(response.header("Content-Type")).isEqualTo("application/json")
                    s.assertThatJson(response.body?.string()).isEqualTo(
                        """
                    {
                        "room_type_id": "$roomTypeId"
                    }
                """.trimIndent()
                    )
                }
            }
        )
    }

    @Test
    fun `should return 403 when user does not have MANAGE_ROOMS permission`() = test(javalin) { _, client ->
        `RUN should return 403 when user does not have MANAGE_ROOMS permission`(
            object : TestRequest<Response>() {
                override fun makeRequest(): Response = client.request("/api/admin/v1/room-type") { rb ->
                    rb.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                        .header("Content-Type", "application/json")
                        .post(makeRequestBody(true))
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
                                "instance": "/api/admin/v1/room-type",
                                "extended_details": []
                            }
                        """.trimIndent()
                    )
                }
            }
        )
    }

    private fun makeRequestBody(withImages: Boolean): RequestBody =
        if (withImages) {
            """
                {
                    "title": "$title",
                    "description": "$description",
                    "image_urls": [${imageUrls.joinToString(",") { "\"$it\"" }}],
                    "price_per_night": $pricePerNight,
                    "stock_level": $stockLevel
                }
            """.trimIndent().toRequestBody("application/json".toMediaType())
        } else {
            """
                {
                    "title": "$title",
                    "description": "$description",
                    "price_per_night": $pricePerNight,
                    "stock_level": $stockLevel
                }
            """.trimIndent().toRequestBody("application/json".toMediaType())
        }
}
