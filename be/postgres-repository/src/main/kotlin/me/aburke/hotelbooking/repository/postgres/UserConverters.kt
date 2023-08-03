package me.aburke.hotelbooking.repository.postgres

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.ports.repository.NonAnonymousUserRecord
import me.aburke.hotelbooking.ports.repository.UserCredentialRecord
import java.sql.Connection
import java.sql.ResultSet

fun ResultSet.toNonAnonymousUserRecord() = NonAnonymousUserRecord(
    userId = getString("user_id"),
    userRoles = setOf(*(getArray("user_roles").array as Array<String>))
        .map { UserRole.valueOf(it) }
        .toSet(),
    name = getString("name"),
    credential = UserCredentialRecord(
        loginId = getString("login_id"),
        passwordHash = getString("password_hash"),
    )
)

fun Connection.toSqlArray(roles: Set<UserRole>) = createArrayOf(
    "varchar",
    roles.map { it.name }.toTypedArray(),
)
