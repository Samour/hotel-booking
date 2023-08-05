package me.aburke.hotelbooking.ports.repository

import java.util.TimeZone

interface HotelRepository {

    fun getTimeZone(): TimeZone
}
