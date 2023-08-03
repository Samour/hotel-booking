package me.aburke.hotelbooking.scenario.user

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.ports.repository.InsertUserResult
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.scenario.Scenario

class CreateUserDetails(
    val loginId: String,
    val rawPassword: String,
    val name: String,
    val userRoles: Set<UserRole>,
) : Scenario.Details

sealed interface CreateUserResult : Scenario.Result {

    data class Success(
        val userId: String,
    ) : CreateUserResult

    data object UsernameNotAvailable : CreateUserResult
}

class CreateUserScenario(
    private val passwordHasher: PasswordHasher,
    private val userRepository: UserRepository,
) : Scenario<CreateUserDetails, CreateUserResult> {

    override fun run(details: CreateUserDetails): CreateUserResult {
        val passwordHash = passwordHasher.hashPassword(details.rawPassword)
        val insertRecord = InsertUserRecord(
            loginId = details.loginId,
            passwordHash = passwordHash,
            name = details.name,
            roles = details.userRoles,
        )

        return when (val result = userRepository.insertUser(insertRecord)) {
            is InsertUserResult.UserInserted -> CreateUserResult.Success(
                userId = result.userId,
            )

            is InsertUserResult.LoginIdUniquenessViolation -> CreateUserResult.UsernameNotAvailable
        }
    }
}
