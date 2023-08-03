package me.aburke.hotelbooking.repository.postgres

import me.aburke.hotelbooking.model.user.UserRole
import java.sql.Connection
import java.sql.PreparedStatement

// Read queries

fun Connection.findUserByLoginIdQuery(loginId: String): PreparedStatement =
    prepareStatement(
        """
            select c.user_id, c.login_id, c.password_hash, u.user_roles, u.name
            from user_credential c
            join app_user u on u.user_id = c.user_id
            where login_id = ?
        """.trimIndent()
    ).also {
        it.setString(1, loginId)
    }

// Update queries

fun Connection.insertUserQuery(userId: String, userRoles: Set<UserRole>, name: String): PreparedStatement =
    prepareStatement(
        """
            insert into app_user(user_id, user_roles, name)
            values (?, ?, ?)
        """.trimIndent()
    ).also {
        it.setString(1, userId)
        it.setArray(2, toSqlArray(userRoles))
        it.setString(3, name)
    }

fun Connection.insertCredentialQuery(userId: String, loginId: String, passwordHash: String): PreparedStatement =
    prepareStatement(
        """
            insert into user_credential(user_id, login_id, password_hash)
            values (?, ?, ?)
        """.trimIndent()
    ).also {
        it.setString(1, userId)
        it.setString(2, loginId)
        it.setString(3, passwordHash)
    }

fun Connection.updateUserQuery(userId: String, roles: Set<UserRole>, name: String): PreparedStatement =
    prepareStatement(
        """
            update app_user
            set user_roles = ?, name = ?
            where user_id = ?
        """.trimIndent()
    ).also {
        it.setArray(1, toSqlArray(roles))
        it.setString(2, name)
        it.setString(3, userId)
    }
