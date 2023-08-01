package me.aburke.hotelbooking.scenario.user

import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.scenario.Scenario

data class AnonymousUserCreated(
    val userId: String,
) : Scenario.Result

class CreateAnonymousUserScenario(
    private val userRepository: UserRepository,
) : Scenario<CreateAnonymousUserScenario.Detail, AnonymousUserCreated> {

    data object Detail : Scenario.Details

    override fun run(details: Detail): AnonymousUserCreated {
        return AnonymousUserCreated(
            userId = userRepository.createAnonymousUser(),
        )
    }
}
