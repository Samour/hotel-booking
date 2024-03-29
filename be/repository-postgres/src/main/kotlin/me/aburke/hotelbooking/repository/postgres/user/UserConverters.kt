package me.aburke.hotelbooking.repository.postgres.user

import me.aburke.hotelbooking.ports.repository.NonAnonymousUserRecord
import me.aburke.hotelbooking.ports.repository.UserCredentialRecord
import java.sql.Connection
import java.sql.ResultSet

fun ResultSet.toNonAnonymousUserRecord() = NonAnonymousUserRecord(
    userId = getString("user_id"),
    userRoles = getArray("user_roles").toUserRoles(),
    name = getString("name"),
    credential = UserCredentialRecord(
        loginId = getString("login_id"),
        passwordHash = getString("password_hash"),
    ),
)

fun java.sql.Array.toUserRoles() = setOf(*(array as Array<String>))
    .toSet()

fun Connection.toSqlArray(roles: Set<String>) = createArrayOf(
    "varchar",
    roles.toTypedArray(),
)
