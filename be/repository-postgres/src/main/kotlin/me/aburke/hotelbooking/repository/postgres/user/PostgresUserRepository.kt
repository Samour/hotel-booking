package me.aburke.hotelbooking.repository.postgres.user

import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.ports.repository.InsertUserResult
import me.aburke.hotelbooking.ports.repository.NonAnonymousUserRecord
import me.aburke.hotelbooking.ports.repository.PromoteAnonymousUserResult
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.repository.postgres.executeQueryWithRollback
import org.postgresql.util.PSQLException
import java.util.UUID.randomUUID
import javax.sql.DataSource

private const val CONSTRAINT_UNIQUE_LOGIN_ID = "idx__user_credential__login_id"
private const val CONSTRAINT_DUPLICATE_USER_CREDENTIAL = "user_credential_pkey"

class PostgresUserRepository(
    private val dataSource: DataSource,
) : UserRepository {

    override fun createAnonymousUser(): String = dataSource.connection.use { connection ->
        val userId = randomUUID().toString()

        val query = connection.insertUserQuery(
            userId = userId,
            userRoles = setOf("CUSTOMER"),
            name = "",
        )

        try {
            query.executeUpdate()
            connection.commit()
        } catch (e: PSQLException) {
            connection.rollback()
            throw e
        }

        return userId
    }

    override fun insertUser(
        userRecord: InsertUserRecord,
    ): InsertUserResult = dataSource.connection.use { connection ->
        val userId = randomUUID().toString()

        val userQuery = connection.insertUserQuery(
            userId = userId,
            userRoles = userRecord.roles,
            name = userRecord.name,
        )
        val credentialQuery = connection.insertCredentialQuery(
            userId = userId,
            loginId = userRecord.loginId,
            passwordHash = userRecord.passwordHash,
        )

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
        credentials: InsertUserRecord,
    ): PromoteAnonymousUserResult = dataSource.connection.use { connection ->
        val userQuery = connection.updateUserQuery(
            userId = userId,
            roles = credentials.roles,
            name = credentials.name,
        )
        val credentialQuery = connection.insertCredentialQuery(
            userId = userId,
            loginId = credentials.loginId,
            passwordHash = credentials.passwordHash,
        )

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

    override fun findUserByLoginId(
        loginId: String,
    ): NonAnonymousUserRecord? = dataSource.connection.use { connection ->
        connection.findUserByLoginIdQuery(loginId)
            .executeQueryWithRollback()
            .takeIf { it.next() }
            ?.toNonAnonymousUserRecord()
    }
}
