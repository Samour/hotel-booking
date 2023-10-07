package me.aburke.hotelbooking.repository.postgres

import me.aburke.hotelbooking.ports.repository.HotelRepository
import me.aburke.hotelbooking.ports.repository.RoomRepository
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.repository.postgres.hotel.PostgresHotelRepository
import me.aburke.hotelbooking.repository.postgres.room.PostgresRoomRepository
import me.aburke.hotelbooking.repository.postgres.user.PostgresUserRepository
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.sql.Connection
import java.sql.DriverManager

val postgresModule = module {
    single<Connection> {
        DriverManager.getConnection(
            getProperty("postgresql.uri"),
            getProperty("postgresql.user"),
            getProperty("postgresql.password"),
        ).apply {
            autoCommit = false
        }
    } onClose {
        it?.close()
    }

    single<UserRepository> { PostgresUserRepository(get()) }
    single<HotelRepository> { PostgresHotelRepository(get()) }
    single<RoomRepository> { PostgresRoomRepository(get(), get()) }
}
