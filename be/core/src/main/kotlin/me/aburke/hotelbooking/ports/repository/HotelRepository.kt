package me.aburke.hotelbooking.ports.repository

import java.util.TimeZone

interface HotelRepository {

    fun loadTimeZone(): TimeZone
}
