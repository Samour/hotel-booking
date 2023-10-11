package me.aburke.hotelbooking.customer

import me.aburke.hotelbooking.createTestContext
import me.aburke.hotelbooking.rest.client.api.CustomerUnstableApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.model.HoldRoomRequest
import me.aburke.hotelbooking.restTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.KoinApplication

class HoldRoomTest {

    private lateinit var app: KoinApplication

    @BeforeEach
    fun init() {
        app = createTestContext().app
    }

    @Test
    fun `endpoint should not be available`() = app.restTest { client, _ ->
        assertThrows<ApiException> {
            CustomerUnstableApi(client).holdRoom(
                "room-type-id",
                HoldRoomRequest(),
            )
        }.also {
            assertThat(it.code).isEqualTo(404)
        }
    }
}
