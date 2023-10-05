package me.aburke.hotelbooking.facade.rest.api.admin.v1.roomtype

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.client
import me.aburke.hotelbooking.facade.rest.parseResponse
import me.aburke.hotelbooking.rest.client.api.AdminApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.invoker.ApiResponse
import me.aburke.hotelbooking.rest.client.model.AddRoomType201Response
import me.aburke.hotelbooking.rest.client.model.AddRoomTypeRequest
import me.aburke.hotelbooking.rest.client.model.ProblemResponse
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class AddRoomTypeTest : AbstractAddRoomTypeTest() {

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should create new room type`(withImages: Boolean) = test(javalin) { _, _ ->
        `RUN should create new room type`(
            withImages,
            object : TestRequest<ApiResponse<AddRoomType201Response>>() {
                override fun makeRequest(): ApiResponse<AddRoomType201Response> =
                    AdminApi(javalin.client(sessionId)).addRoomTypeWithHttpInfo(
                        AddRoomTypeRequest().also {
                            it.title = title
                            it.description = description
                            it.pricePerNight = pricePerNight
                            it.stockLevel = stockLevel
                            if (withImages) {
                                it.imageUrls = imageUrls
                            }
                        },
                    )

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.statusCode).isEqualTo(201)
                    s.assertThat(response.data).isEqualTo(
                        AddRoomType201Response().also {
                            it.roomTypeId = roomTypeId
                        },
                    )
                }
            },
        )
    }

    @Test
    fun `should return 403 when user does not have MANAGE_ROOMS permission`() = test(javalin) { _, _ ->
        `RUN should return 403 when user does not have MANAGE_ROOMS permission`(
            object : TestRequest<ApiException>() {
                override fun makeRequest(): ApiException = assertThrows<ApiException> {
                    AdminApi(javalin.client(sessionId)).addRoomType(
                        AddRoomTypeRequest().also {
                            it.title = title
                            it.description = description
                            it.pricePerNight = pricePerNight
                            it.stockLevel = stockLevel
                            it.imageUrls = imageUrls
                        },
                    )
                }

                override fun makeAssertions(s: SoftAssertions) {
                    val responseBody = response.responseBody.parseResponse<ProblemResponse>()
                    s.assertThat(response.code).isEqualTo(403)
                    s.assertThat(response.responseHeaders["Content-Type"])
                        .containsExactly("application/problem+json;charset=utf-8")
                    s.assertThat(responseBody).isEqualTo(
                        ProblemResponse().apply {
                            title = "Forbidden"
                            code = "FORBIDDEN"
                            status = 403
                            detail = "Insufficient permissions to access resource"
                            instance = "/api/admin/v1/room-type"
                            extendedDetails = emptyList()
                        },
                    )
                }
            },
        )
    }
}
