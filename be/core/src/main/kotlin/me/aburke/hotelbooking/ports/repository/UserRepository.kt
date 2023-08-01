package me.aburke.hotelbooking.ports.repository

import me.aburke.hotelbooking.model.user.UserRole

data class InsertUserRecord(
    val loginId: String,
    val passwordHash: String,
    val name: String,
    val roles: Set<UserRole>,
)

sealed interface InsertUserResult {

    data class UserInserted(val userId: String) : InsertUserResult

    data object LoginIdUniquenessViolation : InsertUserResult
}

sealed interface PromoteAnonymousUserResult {

    data class UserCredentialsInserted(val userId: String) : PromoteAnonymousUserResult

    data object UserIsNotAnonymous : PromoteAnonymousUserResult

    data object LoginIdUniquenessViolation : PromoteAnonymousUserResult
}

interface UserRepository {

    fun insertUser(userRecord: InsertUserRecord): InsertUserResult

    fun createCredentialsForAnonymousUser(userId: String, credentials: InsertUserRecord): PromoteAnonymousUserResult
}
