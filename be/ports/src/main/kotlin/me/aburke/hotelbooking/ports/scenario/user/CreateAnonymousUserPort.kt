package me.aburke.hotelbooking.ports.scenario.user

import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.Scenario

data class AnonymousUserCreated(
    val session: UserSession,
) : Scenario.Result

interface CreateAnonymousUserPort : Scenario<CreateAnonymousUserPort.Details, AnonymousUserCreated> {

    data object Details : Scenario.Details
}
