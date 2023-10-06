package me.aburke.hotelbooking.repository.postgres

import org.postgresql.util.PSQLException
import java.sql.PreparedStatement

fun PreparedStatement.executeQueryWithRollback() = try {
    executeQuery()!!
} catch (e: PSQLException) {
    connection.rollback()
    throw e
}
