package me.aburke.hotelbooking.facade.rest.api.auth.v1.user

import me.aburke.hotelbooking.facade.rest.RouteRegistry
import me.aburke.hotelbooking.facade.rest.Routes
import me.aburke.hotelbooking.facade.rest.authentication.EndpointRole

class UserRoutes(
    private val signUpHandler: SignUpHandler,
) : Routes {

    override fun addRoutes(registry: RouteRegistry) {
        registry.post("/api/auth/v1/user", EndpointRole.Optional(EndpointRole.Any), signUpHandler)
    }
}
