package me.aburke.hotelbooking.scenario.user

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.repository.SessionRepository
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.scenario.Scenario
import me.aburke.hotelbooking.session.SessionFactory

data class AnonymousUserCreated(
    val session: UserSession,
) : Scenario.Result

class CreateAnonymousUserScenario(
    private val sessionFactory: SessionFactory,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
) : Scenario<CreateAnonymousUserScenario.Detail, AnonymousUserCreated> {

    data object Detail : Scenario.Details

    override fun run(details: Detail): AnonymousUserCreated {
        return AnonymousUserCreated(
            sessionFactory.createForUser(
                userId = userRepository.createAnonymousUser(),
                userRoles = setOf(UserRole.CUSTOMER),
                anonymousUser = true,
            ).also { sessionRepository.insertUserSession(it) }
        )
    }
}
