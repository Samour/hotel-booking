package me.aburke.hotelbooking.scenario.user

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.ports.repository.SessionRepository
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.ports.scenario.user.AnonymousUserCreated
import me.aburke.hotelbooking.ports.scenario.user.CreateAnonymousUserPort
import me.aburke.hotelbooking.session.SessionFactory

class CreateAnonymousUserScenario(
    private val sessionFactory: SessionFactory,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
) : CreateAnonymousUserPort {

    override fun run(details: CreateAnonymousUserPort.Details): AnonymousUserCreated {
        return AnonymousUserCreated(
            sessionFactory.createForUser(
                userId = userRepository.createAnonymousUser(),
                loginId = null,
                userRoles = setOf(UserRole.CUSTOMER),
                anonymousUser = true,
            ).also { sessionRepository.insertUserSession(it) },
        )
    }
}
