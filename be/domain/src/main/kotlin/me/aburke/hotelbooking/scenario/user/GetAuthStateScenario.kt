package me.aburke.hotelbooking.scenario.user

import me.aburke.hotelbooking.model.user.toUserSession
import me.aburke.hotelbooking.ports.repository.SessionRepository
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateDetails
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStatePort
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateResult

class GetAuthStateScenario(
    private val sessionRepository: SessionRepository,
) : GetAuthStatePort {

    override fun run(details: GetAuthStateDetails): GetAuthStateResult =
        sessionRepository.loadUserSession(details.sessionId)?.let {
            GetAuthStateResult.SessionExists(it.toUserSession())
        } ?: GetAuthStateResult.SessionDoesNotExist
}
