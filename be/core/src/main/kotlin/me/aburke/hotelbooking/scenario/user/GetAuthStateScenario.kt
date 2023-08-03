package me.aburke.hotelbooking.scenario.user

import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.repository.SessionRepository
import me.aburke.hotelbooking.scenario.Scenario

data class GetAuthStateDetails(
    val sessionId: String,
) : Scenario.Details

sealed interface GetAuthStateResult : Scenario.Result {

    data class SessionExists(
        val session: UserSession,
    ) : GetAuthStateResult

    data object SessionDoesNotExist : GetAuthStateResult
}

class GetAuthStateScenario(
    private val sessionRepository: SessionRepository,
) : Scenario<GetAuthStateDetails, GetAuthStateResult> {

    override fun run(details: GetAuthStateDetails): GetAuthStateResult =
        sessionRepository.loadUserSession(details.sessionId)?.let {
            GetAuthStateResult.SessionExists(it)
        } ?: GetAuthStateResult.SessionDoesNotExist
}
