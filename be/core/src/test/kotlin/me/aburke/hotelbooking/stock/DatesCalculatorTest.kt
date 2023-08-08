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

private val dateRange = (1..20).map {
    auFirstDate.plusDays(it.toLong())
}

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

    @Test
    fun `should return list of dates within range, inclusive`() {
        val result = underTest.calculateDatesInRange(dateRange.first(), dateRange.last())

        assertThat(result).isEqualTo(dateRange)
    }

    @Test
    fun `should flip start & end when they are out of order`() {
        val result = underTest.calculateDatesInRange(dateRange.last(), dateRange.first())

        assertThat(result).isEqualTo(dateRange)
    }

    @Test
    fun `should return single date when start = end`() {
        val result = underTest.calculateDatesInRange(dateRange.first(), dateRange.first())

        assertThat(result).containsExactly(dateRange.first())
    }
}
