package me.aburke.hotelbooking.customer

import me.aburke.hotelbooking.createApp
import me.aburke.hotelbooking.rest.client.api.CustomerApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.restTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.KoinApplication
import java.time.LocalDate

class FetchRoomAvailabilityTest {

    private lateinit var app: KoinApplication

    @BeforeEach
    fun init() {
        app = createApp().first
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should not yet be available`() = app.restTest { client, _ ->
        assertThrows<ApiException> {
            CustomerApi(client).fetchRoomsAvailability(
                LocalDate.parse("2023-10-07"),
                LocalDate.parse("2023-10-17"),
            )
        }.also {
            assertThat(it.code).isEqualTo(404)
        }
    }
}
