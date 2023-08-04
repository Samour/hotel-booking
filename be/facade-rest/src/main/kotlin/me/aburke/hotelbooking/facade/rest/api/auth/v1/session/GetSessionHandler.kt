package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import io.javalin.http.Context
import io.javalin.http.Handler
import me.aburke.hotelbooking.facade.rest.authentication.getUserSession
import me.aburke.hotelbooking.model.user.UserSession
import java.time.Instant

data class SessionResponse(
    val userId: String,
    val userRoles: List<String>,
    val anonymousUser: Boolean,
    val sessionExpiryTime: Instant,
)

class GetSessionHandler : Handler {

    override fun handle(ctx: Context) {
        ctx.json(ctx.getUserSession().toSessionResponse())
    }
}

private fun UserSession.toSessionResponse() = SessionResponse(
    userId = userId,
    userRoles = userRoles.map { it.name },
    anonymousUser = anonymousUser,
    sessionExpiryTime = sessionExpiryTime,
)
