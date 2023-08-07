package me.aburke.hotelbooking.scenario.user

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.ports.repository.*
import me.aburke.hotelbooking.ports.scenario.user.AnonymousSession
import me.aburke.hotelbooking.ports.scenario.user.SignUpDetails
import me.aburke.hotelbooking.ports.scenario.user.SignUpPort
import me.aburke.hotelbooking.ports.scenario.user.SignUpResult
import me.aburke.hotelbooking.session.SessionFactory

class SignUpScenario(
    private val passwordHasher: PasswordHasher,
    private val sessionFactory: SessionFactory,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
) : SignUpPort {

    override fun run(details: SignUpDetails): SignUpResult {
        val passwordHash = passwordHasher.hashPassword(details.rawPassword)
        val insertRecord = InsertUserRecord(
            loginId = details.loginId,
            passwordHash = passwordHash,
            name = details.name,
            roles = setOf(UserRole.CUSTOMER),
        )

        return details.anonymousUser?.let {
            promoteAnonymousUser(it, insertRecord)
        } ?: createNewUser(insertRecord)
    }

    private fun createNewUser(insertRecord: InsertUserRecord): SignUpResult =
        when (val result = userRepository.insertUser(insertRecord)) {
            is InsertUserResult.UserInserted -> SignUpResult.Success(
                sessionFactory.createForUser(
                    userId = result.userId,
                    loginId = insertRecord.loginId,
                    userRoles = insertRecord.roles,
                    anonymousUser = false,
                ).also { sessionRepository.insertUserSession(it) }
            )

            is InsertUserResult.LoginIdUniquenessViolation -> SignUpResult.UsernameNotAvailable
        }

    private fun promoteAnonymousUser(anonymousSession: AnonymousSession, insertRecord: InsertUserRecord): SignUpResult =
        when (val result =
            userRepository.createCredentialsForAnonymousUser(anonymousSession.userId, insertRecord)) {
            is PromoteAnonymousUserResult.UserCredentialsInserted -> SignUpResult.Success(
                sessionFactory.createForUser(
                    userId = result.userId,
                    loginId = insertRecord.loginId,
                    userRoles = insertRecord.roles,
                    anonymousUser = false,
                ).copy(sessionId = anonymousSession.sessionId)
                    .also { sessionRepository.insertUserSession(it) }
            )

            is PromoteAnonymousUserResult.UserIsNotAnonymous -> SignUpResult.UserIsNotAnonymous

            is PromoteAnonymousUserResult.LoginIdUniquenessViolation -> SignUpResult.UsernameNotAvailable

            is PromoteAnonymousUserResult.AnonymousUserDoesNotExist -> SignUpResult.AnonymousUserDoesNotExist
        }
}
