package me.aburke.hotelbooking.migrations.postgres

import java.sql.DriverManager
import java.util.*

fun main() {
    val properties = Properties()
    properties.load(object {}::class.java.getResourceAsStream("/migrations.properties"))

    val connection = DriverManager.getConnection(
        properties.getProperty("postgresql.uri"),
        properties.getProperty("postgresql.user"),
        properties.getProperty("postgresql.password"),
    )

    connection.executeScript("bootstrap_db.sql")
    connection.close()
}
