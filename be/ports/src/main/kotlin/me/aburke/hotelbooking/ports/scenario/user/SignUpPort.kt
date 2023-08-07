package me.aburke.hotelbooking.ports.scenario.user

import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.Scenario

data class SignUpDetails(
    val loginId: String,
    val rawPassword: String,
    val name: String,
    val anonymousUser: AnonymousSession?,
) : Scenario.Details

data class AnonymousSession(
    val sessionId: String,
    val userId: String,
)

sealed interface SignUpResult : Scenario.Result {

    data class Success(
        val session: UserSession,
    ) : SignUpResult

    data object UsernameNotAvailable : SignUpResult

    data object UserIsNotAnonymous : SignUpResult

    data object AnonymousUserDoesNotExist : SignUpResult
}

interface SignUpPort : Scenario<SignUpDetails, SignUpResult>
