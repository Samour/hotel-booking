package me.aburke.hotelbooking.ports.scenario.user

import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.Scenario

data class LogInCredentials(
    val loginId: String,
    val password: String,
) : Scenario.Details

sealed interface LogInResult : Scenario.Result {

    data class UserSessionCreated(
        val session: UserSession,
    ) : LogInResult

    data object InvalidCredentials : LogInResult
}

interface LogInPort : Scenario<LogInCredentials, LogInResult>
