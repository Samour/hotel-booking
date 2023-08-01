package me.aburke.hotelbooking.repository.postgres

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.ports.repository.InsertUserResult
import me.aburke.hotelbooking.ports.repository.PromoteAnonymousUserResult
import me.aburke.hotelbooking.ports.repository.UserRepository
import org.postgresql.util.PSQLException
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.UUID.randomUUID

private const val CONSTRAINT_UNIQUE_LOGIN_ID = "idx__user_credential__login_id"
private const val CONSTRAINT_DUPLICATE_USER_CREDENTIAL = "user_credential_pkey"

class PostgresUserRepository(
    private val connection: Connection,
) : UserRepository {

    override fun createAnonymousUser(): String {
        val userId = randomUUID().toString()

        val query = insertUserQuery(userId, setOf(UserRole.CUSTOMER), "")

        try {
            query.executeUpdate()
            connection.commit()
        } catch (e: PSQLException) {
            connection.rollback()
            throw e
        }

        return userId
    }

    override fun insertUser(userRecord: InsertUserRecord): InsertUserResult {
        val userId = randomUUID().toString()

        val userQuery = insertUserQuery(userId, userRecord.roles, userRecord.name)

        val credentialQuery = insertCredentialQuery(userId, userRecord.loginId, userRecord.passwordHash)

        try {
            userQuery.executeUpdate()
            credentialQuery.executeUpdate()
            connection.commit()
        } catch (e: PSQLException) {
            connection.rollback()
            when (e.serverErrorMessage?.constraint) {
                CONSTRAINT_UNIQUE_LOGIN_ID -> return InsertUserResult.LoginIdUniquenessViolation
                else -> throw e
            }
        }

        return InsertUserResult.UserInserted(userId)
    }

    override fun createCredentialsForAnonymousUser(
        userId: String,
        credentials: InsertUserRecord
    ): PromoteAnonymousUserResult {
        val userQuery = connection.prepareStatement(
            """
                update app_user
                set user_roles = ?, name = ?
                where user_id = ?
            """.trimIndent()
        )
        userQuery.setArray(1, credentials.roles.toSqlArray())
        userQuery.setString(2, credentials.name)
        userQuery.setString(3, userId)

        val credentialQuery = insertCredentialQuery(userId, credentials.loginId, credentials.passwordHash)

        try {
            userQuery.executeUpdate().takeIf { it == 0 }?.let {
                connection.rollback()
                return PromoteAnonymousUserResult.AnonymousUserDoesNotExist
            }
            credentialQuery.executeUpdate()
            connection.commit()
        } catch (e: PSQLException) {
            connection.rollback()
            return when (e.serverErrorMessage?.constraint) {
                CONSTRAINT_DUPLICATE_USER_CREDENTIAL -> PromoteAnonymousUserResult.UserIsNotAnonymous
                CONSTRAINT_UNIQUE_LOGIN_ID -> PromoteAnonymousUserResult.LoginIdUniquenessViolation
                else -> throw e
            }
        }

        return PromoteAnonymousUserResult.UserCredentialsInserted(userId)
    }

    private fun insertUserQuery(userId: String, userRoles: Set<UserRole>, name: String): PreparedStatement {
        val query = connection.prepareStatement(
            """
                insert into app_user(user_id, user_roles, name)
                values (?, ?, ?)
            """.trimIndent()
        )
        query.setString(1, userId)
        query.setArray(2, userRoles.toSqlArray())
        query.setString(3, name)

        return query
    }

    private fun insertCredentialQuery(userId: String, loginId: String, passwordHash: String): PreparedStatement {
        val query = connection.prepareStatement(
            """
                insert into user_credential(user_id, login_id, password_hash)
                values (?, ?, ?)
            """.trimIndent()
        )
        query.setString(1, userId)
        query.setString(2, loginId)
        query.setString(3, passwordHash)

        return query
    }

    private fun Set<UserRole>.toSqlArray() = connection.createArrayOf(
        "varchar",
        map { it.name }.toTypedArray(),
    )
}
