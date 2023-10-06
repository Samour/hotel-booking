package me.aburke.hotelbooking.facade.rest.api.auth.session

import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.HttpStatus
import me.aburke.hotelbooking.facade.rest.authentication.setAuthCookie
import me.aburke.hotelbooking.facade.rest.responses.SessionResponse
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.user.CreateAnonymousUserPort

class CreateAnonymousSessionHandler(
    private val createAnonymousUserPort: CreateAnonymousUserPort,
) : Handler {

    override fun handle(ctx: Context) {
        val session = createAnonymousUserPort.run(CreateAnonymousUserPort.Details).session
        ctx.status(HttpStatus.CREATED)
        ctx.setAuthCookie(session)
        ctx.json(session.toResponse())
    }
}

private fun UserSession.toResponse() = SessionResponse(
    userId = userId,
    loginId = null,
    userRoles = userRoles.map { it.name },
    anonymousUser = true,
    sessionExpiryTime = sessionExpiryTime,
)
