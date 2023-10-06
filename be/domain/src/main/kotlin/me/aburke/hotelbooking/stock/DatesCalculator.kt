package me.aburke.hotelbooking.stock

import java.time.Instant
import java.time.LocalDate
import java.util.TimeZone

class DatesCalculator {

    fun calculateDateRange(timeZone: TimeZone, anchor: Instant, rangeSize: Int): List<LocalDate> {
        val firstDate = anchor.atZone(timeZone.toZoneId())
            .toLocalDate()

        return (0L until rangeSize).map {
            firstDate.plusDays(it)
        }
    }

    fun calculateDatesInRange(rangeStart: LocalDate, rangeEnd: LocalDate): List<LocalDate> {
        val (actualStart, actualEnd) = if (rangeStart <= rangeEnd) {
            rangeStart to rangeEnd
        } else {
            rangeEnd to rangeStart
        }

        val dates = mutableListOf(actualStart)
        var offset = 1
        while (dates.last() < actualEnd) {
            dates.add(actualStart.plusDays(offset.toLong()))
            offset++
        }

        return dates
    }
}
