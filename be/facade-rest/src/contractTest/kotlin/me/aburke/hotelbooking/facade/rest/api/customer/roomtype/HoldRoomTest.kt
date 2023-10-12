package me.aburke.hotelbooking.facade.rest.api.customer.roomtype

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.client
import me.aburke.hotelbooking.rest.client.api.CustomerUnstableApi
import me.aburke.hotelbooking.rest.client.invoker.ApiResponse
import me.aburke.hotelbooking.rest.client.model.HoldRoom201Response
import me.aburke.hotelbooking.rest.client.model.HoldRoomRequest
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.ZoneOffset

@Disabled("WIP")
class HoldRoomTest : AbstractHoldRoomTest() {

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `should place a hold on the specified room type`(withExistingHoldRemoved: Boolean) = test(javalin) { _, _ ->
        `RUN should place a hold on the specified room type`(
            object : TestRequest<ApiResponse<HoldRoom201Response>>() {
                override fun makeRequest(): ApiResponse<HoldRoom201Response> =
                    CustomerUnstableApi(javalin.client()).holdRoomWithHttpInfo(
                        roomTypeId,
                        HoldRoomRequest().also {
                            it.holdStartDate = holdStartDate
                            it.holdEndDate = holdEndDate
                        },
                    )

                override fun makeAssertions(s: SoftAssertions) {
                    s.assertThat(response.statusCode).isEqualTo(201)
                    s.assertThat(response.data).isEqualTo(
                        HoldRoom201Response().also {
                            it.roomHoldId = roomHoldId
                            it.holdExpiry = holdExpiry.atOffset(ZoneOffset.UTC)
                            it.removedRoomHoldId = if (withExistingHoldRemoved) removedRoomHoldId else null
                        },
                    )
                }
            },
            withExistingHoldRemoved,
        )
    }

    @Test
    fun `should return 409 if stock not available to hold`() {
        `RUN should return 409 if stock not available to hold`(
            object : TestRequest<ApiResponse<HoldRoom201Response>>() {
                override fun makeRequest(): ApiResponse<HoldRoom201Response> {
                    TODO("Not yet implemented")
                }

                override fun makeAssertions(s: SoftAssertions) {
                    TODO("Not yet implemented")
                }
            },
        )
    }

    @Test
    fun `should return 409 if concurrent request received from user`() {
        `RUN should return 409 if concurrent request received from user`(
            object : TestRequest<ApiResponse<HoldRoom201Response>>() {
                override fun makeRequest(): ApiResponse<HoldRoom201Response> {
                    TODO("Not yet implemented")
                }

                override fun makeAssertions(s: SoftAssertions) {
                    TODO("Not yet implemented")
                }
            },
        )
    }
}
