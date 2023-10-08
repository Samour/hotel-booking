package me.aburke.hotelbooking.repository.postgres

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.aburke.hotelbooking.ports.repository.HotelRepository
import me.aburke.hotelbooking.ports.repository.RoomHoldRepository
import me.aburke.hotelbooking.ports.repository.RoomRepository
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.repository.postgres.hotel.PostgresHotelRepository
import me.aburke.hotelbooking.repository.postgres.room.PostgresRoomRepository
import me.aburke.hotelbooking.repository.postgres.room.hold.PostgresRoomHoldRepository
import me.aburke.hotelbooking.repository.postgres.user.PostgresUserRepository
import org.koin.dsl.module
import org.koin.dsl.onClose
import javax.sql.DataSource

val postgresModule = module {
    single<DataSource> {
        HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = getProperty("postgresql.uri")
                username = getProperty("postgresql.user")
                password = getProperty("postgresql.password")
                isAutoCommit = false
            },
        )
    } onClose { (it as? HikariDataSource)?.close() }

    single<UserRepository> { PostgresUserRepository(get()) }
    single<HotelRepository> { PostgresHotelRepository(get()) }
    single<RoomRepository> { PostgresRoomRepository(get(), get()) }
    single<RoomHoldRepository> {
        PostgresRoomHoldRepository(
            clock = get(),
            dataSource = get(),
        )
    }
}
