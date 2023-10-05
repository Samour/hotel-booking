package me.aburke.hotelbooking.stubs.repository

import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.ports.repository.InsertUserResult
import me.aburke.hotelbooking.ports.repository.NonAnonymousUserRecord
import me.aburke.hotelbooking.ports.repository.PromoteAnonymousUserResult
import me.aburke.hotelbooking.ports.repository.UserCredentialRecord
import me.aburke.hotelbooking.ports.repository.UserRepository
import java.util.UUID.randomUUID

class UserRepositoryStub : UserRepository {

    private val users = mutableMapOf<String, InsertUserRecord>()
    private val loginIds = mutableMapOf<String, String>()
    private val anonymousUsers = mutableSetOf<String>()

    override fun createAnonymousUser(): String {
        return randomUUID().toString().also(anonymousUsers::add)
    }

    override fun insertUser(userRecord: InsertUserRecord): InsertUserResult {
        if (loginIds.contains(userRecord.loginId)) {
            return InsertUserResult.LoginIdUniquenessViolation
        }

        val userId = randomUUID().toString()
        loginIds[userRecord.loginId] = userId
        users[userId] = userRecord

        return InsertUserResult.UserInserted(userId)
    }

    override fun createCredentialsForAnonymousUser(
        userId: String,
        credentials: InsertUserRecord,
    ): PromoteAnonymousUserResult {
        if (users.containsKey(userId)) {
            return PromoteAnonymousUserResult.UserIsNotAnonymous
        } else if (!anonymousUsers.contains(userId)) {
            return PromoteAnonymousUserResult.AnonymousUserDoesNotExist
        } else if (loginIds.contains(credentials.loginId)) {
            return PromoteAnonymousUserResult.LoginIdUniquenessViolation
        }

        loginIds[credentials.loginId] = userId
        users[userId] = credentials

        return PromoteAnonymousUserResult.UserCredentialsInserted(userId)
    }

    override fun findUserByLoginId(loginId: String): NonAnonymousUserRecord? =
        loginIds[loginId]?.let { userId ->
            users[userId]?.let {
                NonAnonymousUserRecord(
                    userId = userId,
                    userRoles = it.roles,
                    name = it.name,
                    credential = UserCredentialRecord(
                        loginId = loginId,
                        passwordHash = it.passwordHash,
                    ),
                )
            }
        }

    fun getAnonymousUserIds(): Set<String> = anonymousUsers

    fun getUsers(): Map<String, InsertUserRecord> = users
}
