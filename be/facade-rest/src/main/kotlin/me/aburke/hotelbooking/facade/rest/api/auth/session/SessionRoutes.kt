package me.aburke.hotelbooking.facade.rest.api.auth.session

import me.aburke.hotelbooking.facade.rest.RouteRegistry
import me.aburke.hotelbooking.facade.rest.Routes
import me.aburke.hotelbooking.facade.rest.authentication.EndpointRole

class SessionRoutes(
    private val getSessionHandler: GetSessionHandler,
    private val logInHandler: LogInHandler,
    private val createAnonymousSessionHandler: CreateAnonymousSessionHandler,
) : Routes {

    override fun addRoutes(registry: RouteRegistry) {
        registry.get("/api/auth/v0/session", EndpointRole.Any, getSessionHandler)
            .post("/api/auth/v0/session", EndpointRole.Public, logInHandler)
            .post("/api/auth/v0/session/anonymous", EndpointRole.Public, createAnonymousSessionHandler)
    }
}
