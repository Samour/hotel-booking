package me.aburke.hotelbooking.scenario.user

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.ports.repository.*
import me.aburke.hotelbooking.scenario.Scenario
import me.aburke.hotelbooking.session.SessionFactory

class SignUpDetails(
    val loginId: String,
    val rawPassword: String,
    val name: String,
    val anonymousUserId: String?,
) : Scenario.Details

sealed interface SignUpResult : Scenario.Result {

    data class Success(
        val session: UserSession,
    ) : SignUpResult

    data object UsernameNotAvailable : SignUpResult

    data object UserIsNotAnonymous : SignUpResult

    data object AnonymousUserDoesNotExist : SignUpResult
}

class SignUpScenario(
    private val passwordHasher: PasswordHasher,
    private val sessionFactory: SessionFactory,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
) : Scenario<SignUpDetails, SignUpResult> {

    override fun run(details: SignUpDetails): SignUpResult {
        val passwordHash = passwordHasher.hashPassword(details.rawPassword)
        val insertRecord = InsertUserRecord(
            loginId = details.loginId,
            passwordHash = passwordHash,
            name = details.name,
            roles = setOf(UserRole.CUSTOMER),
        )

        return if (details.anonymousUserId == null) {
            createNewUser(insertRecord)
        } else {
            promoteAnonymousUser(details.anonymousUserId, insertRecord)
        }
    }

    private fun createNewUser(insertRecord: InsertUserRecord): SignUpResult {
        return when (val result = userRepository.insertUser(insertRecord)) {
            is InsertUserResult.UserInserted -> SignUpResult.Success(
                sessionFactory.createForUser(
                    userId = result.userId,
                    userRoles = insertRecord.roles,
                    anonymousUser = false,
                ).also { sessionRepository.insertUserSession(it) }
            )

            is InsertUserResult.LoginIdUniquenessViolation -> SignUpResult.UsernameNotAvailable
        }
    }

    private fun promoteAnonymousUser(userId: String, insertRecord: InsertUserRecord): SignUpResult {
        return when (val result = userRepository.createCredentialsForAnonymousUser(userId, insertRecord)) {
            is PromoteAnonymousUserResult.UserCredentialsInserted -> SignUpResult.Success(
                sessionFactory.createForUser(
                    userId = result.userId,
                    userRoles = insertRecord.roles,
                    anonymousUser = false,
                ).also { sessionRepository.insertUserSession(it) }
            )

            is PromoteAnonymousUserResult.UserIsNotAnonymous -> SignUpResult.UserIsNotAnonymous

            is PromoteAnonymousUserResult.LoginIdUniquenessViolation -> SignUpResult.UsernameNotAvailable

            is PromoteAnonymousUserResult.AnonymousUserDoesNotExist -> SignUpResult.AnonymousUserDoesNotExist
        }
    }
}
