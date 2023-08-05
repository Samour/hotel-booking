package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import me.aburke.hotelbooking.facade.rest.RouteRegistry
import me.aburke.hotelbooking.facade.rest.Routes
import me.aburke.hotelbooking.facade.rest.authentication.EndpointRole

class SessionRoutes(
    private val getSessionHandler: GetSessionHandler,
    private val logInHandler: LogInHandler,
    private val createAnonymousSessionHandler: CreateAnonymousSessionHandler,
) : Routes {

    override fun addRoutes(registry: RouteRegistry) {
        registry.get("/api/auth/v1/session", EndpointRole.Any, getSessionHandler)
            .post("/api/auth/v1/session", EndpointRole.Public, logInHandler)
            .post("/api/auth/v1/session/anonymous", EndpointRole.Public, createAnonymousSessionHandler)
    }
}
