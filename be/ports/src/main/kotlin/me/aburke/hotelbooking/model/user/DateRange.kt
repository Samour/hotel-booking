package me.aburke.hotelbooking.model.user

import java.time.LocalDate

data class DateRange(
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
)
