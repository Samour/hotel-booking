package me.aburke.hotelbooking.scenario.user

import me.aburke.hotelbooking.model.user.toDbModel
import me.aburke.hotelbooking.model.user.toUserRoles
import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.ports.repository.SessionRepository
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.ports.scenario.user.LogInCredentials
import me.aburke.hotelbooking.ports.scenario.user.LogInPort
import me.aburke.hotelbooking.ports.scenario.user.LogInResult
import me.aburke.hotelbooking.session.SessionFactory

private const val DUMMY_PW_HASH = "\$2a\$06\$8vB.M.kAHzcx2fFItjFG3.nY4UBiHTvV9P2xdsHlNmtAFzZ8.QQc."

class LogInScenario(
    private val passwordHasher: PasswordHasher,
    private val sessionFactory: SessionFactory,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
) : LogInPort {

    override fun run(details: LogInCredentials): LogInResult {
        val user = userRepository.findUserByLoginId(details.loginId)

        if (user == null) {
            // Run password hash comparison anyway to frustrate timing attacks
            passwordHasher.passwordMatches(DUMMY_PW_HASH, "dummy-password")
            return LogInResult.InvalidCredentials
        }

        if (!passwordHasher.passwordMatches(details.password, user.credential.passwordHash)) {
            return LogInResult.InvalidCredentials
        }

        val session = sessionFactory.createForUser(
            userId = user.userId,
            loginId = user.credential.loginId,
            userRoles = user.userRoles.toUserRoles(),
            anonymousUser = false,
        )
        sessionRepository.insertUserSession(session.toDbModel())

        return LogInResult.UserSessionCreated(session)
    }
}
