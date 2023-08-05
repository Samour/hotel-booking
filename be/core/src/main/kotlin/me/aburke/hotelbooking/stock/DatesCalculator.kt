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
}
