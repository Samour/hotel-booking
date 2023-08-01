package me.aburke.hotelbooking.stubs.repository

import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.ports.repository.InsertUserResult
import me.aburke.hotelbooking.ports.repository.PromoteAnonymousUserResult
import me.aburke.hotelbooking.ports.repository.UserRepository
import java.util.UUID.randomUUID

class UserRepositoryStub : UserRepository {

    private val users = mutableMapOf<String, InsertUserRecord>()
    private val loginIds = mutableSetOf<String>()
    private val anonymousUsers = mutableSetOf<String>()

    override fun createAnonymousUser(): String {
        return randomUUID().toString().also(anonymousUsers::add)
    }

    override fun insertUser(userRecord: InsertUserRecord): InsertUserResult {
        if (loginIds.contains(userRecord.loginId)) {
            return InsertUserResult.LoginIdUniquenessViolation
        }

        val userId = randomUUID().toString()
        loginIds.add(userRecord.loginId)
        users[userId] = userRecord

        return InsertUserResult.UserInserted(userId)
    }

    override fun createCredentialsForAnonymousUser(
        userId: String,
        credentials: InsertUserRecord
    ): PromoteAnonymousUserResult {
        if (users.containsKey(userId)) {
            return PromoteAnonymousUserResult.UserIsNotAnonymous
        } else if (!anonymousUsers.contains(userId)) {
            return PromoteAnonymousUserResult.AnonymousUserDoesNotExist
        } else if (loginIds.contains(credentials.loginId)) {
            return PromoteAnonymousUserResult.LoginIdUniquenessViolation
        }

        loginIds.add(credentials.loginId)
        users[userId] = credentials

        return PromoteAnonymousUserResult.UserCredentialsInserted(userId)
    }

    fun getAnonymousUserIds(): Set<String> = anonymousUsers

    fun getUsers(): Map<String, InsertUserRecord> = users
}
