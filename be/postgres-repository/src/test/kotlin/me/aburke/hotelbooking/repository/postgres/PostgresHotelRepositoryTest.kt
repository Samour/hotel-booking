package me.aburke.hotelbooking.repository.postgres

import me.aburke.hotelbooking.migrations.postgres.executeScript
import me.aburke.hotelbooking.ports.repository.HotelRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.sql.Connection

class PostgresHotelRepositoryTest {

    private lateinit var app: KoinApplication
    private lateinit var connection: Connection

    private lateinit var underTest: HotelRepository

    @BeforeEach
    fun init() {
        app = createApp()
        connection = app.koin.get()
        connection.executeScript("populate_hotel.sql")
        underTest = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should return hotel timezone`() {
        val result = underTest.loadTimeZone()

        assertThat(result).isEqualTo(hotelTimeZone)
    }
}
