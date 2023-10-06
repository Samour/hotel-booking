package me.aburke.hotelbooking.ports.scenario.user

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.ports.scenario.Scenario

data class CreateUserDetails(
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

interface CreateUserPort : Scenario<CreateUserDetails, CreateUserResult>
