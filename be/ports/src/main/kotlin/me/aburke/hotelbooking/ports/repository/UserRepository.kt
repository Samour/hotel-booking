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

data class UserRecord(
    val userId: String,
    val userRoles: Set<UserRole>,
    val name: String,
    val credential: UserCredentialRecord?,
)

data class NonAnonymousUserRecord(
    val userId: String,
    val userRoles: Set<UserRole>,
    val name: String,
    val credential: UserCredentialRecord,
)

data class UserCredentialRecord(
    val loginId: String,
    val passwordHash: String,
)

sealed interface PromoteAnonymousUserResult {

    data class UserCredentialsInserted(val userId: String) : PromoteAnonymousUserResult

    data object UserIsNotAnonymous : PromoteAnonymousUserResult

    data object AnonymousUserDoesNotExist : PromoteAnonymousUserResult

    data object LoginIdUniquenessViolation : PromoteAnonymousUserResult
}

interface UserRepository {

    fun createAnonymousUser(): String

    fun insertUser(userRecord: InsertUserRecord): InsertUserResult

    fun createCredentialsForAnonymousUser(userId: String, credentials: InsertUserRecord): PromoteAnonymousUserResult

    fun findUserByLoginId(loginId: String): NonAnonymousUserRecord?
}
