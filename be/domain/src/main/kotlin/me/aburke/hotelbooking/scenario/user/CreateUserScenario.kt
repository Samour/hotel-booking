package me.aburke.hotelbooking.scenario.user

import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.ports.repository.InsertUserResult
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.ports.scenario.user.CreateUserDetails
import me.aburke.hotelbooking.ports.scenario.user.CreateUserPort
import me.aburke.hotelbooking.ports.scenario.user.CreateUserResult

class CreateUserScenario(
    private val passwordHasher: PasswordHasher,
    private val userRepository: UserRepository,
) : CreateUserPort {

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
