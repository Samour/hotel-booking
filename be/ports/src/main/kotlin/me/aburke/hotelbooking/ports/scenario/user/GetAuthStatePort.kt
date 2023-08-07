package me.aburke.hotelbooking.ports.scenario.user

import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.Scenario

data class GetAuthStateDetails(
    val sessionId: String,
) : Scenario.Details

sealed interface GetAuthStateResult : Scenario.Result {

    data class SessionExists(
        val session: UserSession,
    ) : GetAuthStateResult

    data object SessionDoesNotExist : GetAuthStateResult
}

interface GetAuthStatePort : Scenario<GetAuthStateDetails, GetAuthStateResult>
