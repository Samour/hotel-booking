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
import me.aburke.hotelbooking.scenario.user.GetAuthStateDetails
import me.aburke.hotelbooking.scenario.user.GetAuthStateResult
import me.aburke.hotelbooking.scenario.user.GetAuthStateScenario

class AuthenticationInterceptor(
    private val getAuthStateScenario: GetAuthStateScenario,
) : AccessManager {

    override fun manage(handler: Handler, ctx: Context, routeRoles: Set<RouteRole>) {
        val endpointRoles = routeRoles.mapNotNull { it as? EndpointRole }
            .takeUnless { it.isEmpty() } ?: listOf(EndpointRole.Any)

        if (endpointRoles == listOf(EndpointRole.Public)) {
            handler.handle(ctx)
            return
        }

        val userSession = ctx.cookie(AUTH_COOKIE_KEY)?.let {
            getAuthStateScenario.run(GetAuthStateDetails(it)) as? GetAuthStateResult.SessionExists
        }?.session

        if (userSession == null) {
            ctx.problemJson(ctx.unauthorizedResponse())
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
