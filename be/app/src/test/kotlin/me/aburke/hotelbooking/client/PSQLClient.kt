package me.aburke.hotelbooking.client

import me.aburke.hotelbooking.ports.repository.UserCredentialRecord
import me.aburke.hotelbooking.ports.repository.UserRecord
import me.aburke.hotelbooking.repository.postgres.executeQueryWithRollback
import me.aburke.hotelbooking.repository.postgres.toUserRoles
import java.sql.Connection

fun Connection.readAllUsers() = prepareStatement(
    """
        select u.user_id, u.user_roles, u.name, c.login_id, c.password_hash
        from app_user u
        left outer join user_credential c on c.user_id = u.user_id
    """.trimIndent()
).executeQueryWithRollback().let {
    val records = mutableListOf<UserRecord>()
    while (it.next()) {
        val userId = it.getString("user_id")
        val userRoles = it.getArray("user_roles").toUserRoles()
        val name = it.getString("name")
        val loginId = it.getString("login_id")
        val passwordHash = it.getString("password_hash")

        records.add(
            UserRecord(
                userId = userId,
                userRoles = userRoles,
                name = name,
                credential = if (loginId != null && passwordHash != null) {
                    UserCredentialRecord(
                        loginId = loginId,
                        passwordHash = passwordHash,
                    )
                } else {
                    null
                },
            )
        )
    }

    records
}
