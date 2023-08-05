package me.aburke.hotelbooking.stubs.repository

import me.aburke.hotelbooking.ports.repository.HotelRepository
import java.util.*

val hotelTimeZone = TimeZone.getTimeZone("Australia/Sydney")

class HotelRepositoryStub : HotelRepository {

    override fun getTimeZone(): TimeZone = hotelTimeZone
}
