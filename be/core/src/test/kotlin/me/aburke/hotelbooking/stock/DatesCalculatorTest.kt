package me.aburke.hotelbooking.stock

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.util.*

private const val RANGE_SIZE = 30

private val auTimeZone = TimeZone.getTimeZone("Australia/Sydney")
private val usTimeZone = TimeZone.getTimeZone("America/New_York")
private val timestamp = Instant.parse("2023-08-05T02:00:00Z")

private val auFirstDate = LocalDate.parse("2023-08-05")
private val usFirstDate = LocalDate.parse("2023-08-04")

class DatesCalculatorTest {

    private val underTest = DatesCalculator()

    @Test
    fun `should return date range based on AU time zone`() {
        val result = underTest.calculateDateRange(auTimeZone, timestamp, RANGE_SIZE)

        assertThat(result).isEqualTo(
            (0L..29).map {
                auFirstDate.plusDays(it)
            }
        )
    }

    @Test
    fun `should return date range based on US time zone`() {
        val result = underTest.calculateDateRange(usTimeZone, timestamp, RANGE_SIZE)

        assertThat(result).isEqualTo(
            (0L..29).map {
                usFirstDate.plusDays(it)
            }
        )
    }
}
