package me.aburke.hotelbooking.facade.rest.api.auth.v1.user

import io.javalin.Javalin
import me.aburke.hotelbooking.facade.rest.Routes
import me.aburke.hotelbooking.facade.rest.authentication.EndpointRole

class UserRoutes(
    private val signUpHandler: SignUpHandler,
) : Routes {

    override fun addRoutes(app: Javalin) {
        app.post("/api/auth/v1/user", signUpHandler, EndpointRole.Public)
    }
}
