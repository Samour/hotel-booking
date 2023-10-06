package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import io.javalin.http.Context
import io.javalin.http.Handler
import me.aburke.hotelbooking.facade.rest.authentication.userSession
import me.aburke.hotelbooking.facade.rest.responses.SessionResponse
import me.aburke.hotelbooking.model.user.UserSession

class GetSessionHandler : Handler {

    override fun handle(ctx: Context) {
        ctx.json(ctx.userSession().toSessionResponse())
    }
}

private fun UserSession.toSessionResponse() = SessionResponse(
    userId = userId,
    loginId = loginId,
    userRoles = userRoles.map { it.name },
    anonymousUser = anonymousUser,
    sessionExpiryTime = sessionExpiryTime,
)
