package me.aburke.hotelbooking.facade.rest.interceptors

import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.security.AccessManager
import io.javalin.security.RouteRole
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import me.aburke.hotelbooking.facade.rest.authentication.EndpointRole
import me.aburke.hotelbooking.facade.rest.authentication.USER_SESSION_ATTRIBUTE
import me.aburke.hotelbooking.facade.rest.responses.forbiddenResponse
import me.aburke.hotelbooking.facade.rest.responses.problemJson
import me.aburke.hotelbooking.facade.rest.responses.unauthorizedResponse
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateDetails
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStatePort
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateResult

class AuthenticationInterceptor(
    private val getAuthStatePort: GetAuthStatePort,
) : AccessManager {

    override fun manage(handler: Handler, ctx: Context, routeRoles: Set<RouteRole>) {
        val endpointRoles = routeRoles.mapNotNull { it as? EndpointRole }
            .takeUnless { it.isEmpty() } ?: listOf(EndpointRole.Any)

        if (endpointRoles == listOf(EndpointRole.Public)) {
            handler.handle(ctx)
            return
        }

        val sessionId = ctx.cookie(AUTH_COOKIE_KEY)
        val userSession = sessionId?.let {
            getAuthStatePort.run(GetAuthStateDetails(it)) as? GetAuthStateResult.SessionExists
        }?.session

        if (userSession == null) {
            // TODO we can pull this line up & merge with line 25?
            if (sessionId == null && endpointRoles.all { it == EndpointRole.Public || it is EndpointRole.Optional }) {
                handler.handle(ctx)
            } else {
                ctx.problemJson(ctx.unauthorizedResponse())
            }
            return
        }

        ctx.attribute(USER_SESSION_ATTRIBUTE, userSession)
        val hasNecessaryPermissions = endpointRoles.all {
            it.allowedForRoles(userSession.userRoles)
        }

        if (hasNecessaryPermissions) {
            handler.handle(ctx)
        } else {
            ctx.problemJson(ctx.forbiddenResponse())
        }
    }
}
