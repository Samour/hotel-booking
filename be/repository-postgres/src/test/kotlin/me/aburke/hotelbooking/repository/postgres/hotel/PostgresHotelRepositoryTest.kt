package me.aburke.hotelbooking.repository.postgres.hotel

import me.aburke.hotelbooking.ports.repository.HotelRepository
import me.aburke.hotelbooking.repository.postgres.appForTest
import me.aburke.hotelbooking.repository.postgres.hotelTimeZone
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.sql.Connection
import javax.sql.DataSource

class PostgresHotelRepositoryTest {

    private lateinit var app: KoinApplication
    private lateinit var connection: Connection

    private lateinit var underTest: HotelRepository

    @BeforeEach
    fun init() {
        app = appForTest()
        connection = app.koin.get<DataSource>().connection
        underTest = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = connection.close()

    @Test
    fun `should return hotel timezone`() {
        val result = underTest.loadTimeZone()

        assertThat(result).isEqualTo(hotelTimeZone)
    }
}
