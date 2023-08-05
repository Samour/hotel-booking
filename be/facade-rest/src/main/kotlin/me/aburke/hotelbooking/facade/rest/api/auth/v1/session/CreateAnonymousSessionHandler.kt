package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.HttpStatus
import me.aburke.hotelbooking.facade.rest.authentication.setAuthCookie
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.scenario.user.CreateAnonymousUserScenario
import java.time.Instant

data class CreateAnonymousSessionResponse(
    val userId: String,
    val userRoles: List<String>,
    val anonymousUser: Boolean,
    val sessionExpiryTime: Instant,
)

class CreateAnonymousSessionHandler(
    private val createAnonymousUserScenario: CreateAnonymousUserScenario,
) : Handler {

    override fun handle(ctx: Context) {
        val session = createAnonymousUserScenario.run(CreateAnonymousUserScenario.Detail).session
        ctx.status(HttpStatus.CREATED)
        ctx.setAuthCookie(session)
        ctx.json(session.toResponse())
    }
}

private fun UserSession.toResponse() = CreateAnonymousSessionResponse(
    userId = userId,
    userRoles = userRoles.map { it.name },
    anonymousUser = true,
    sessionExpiryTime = sessionExpiryTime,
)
