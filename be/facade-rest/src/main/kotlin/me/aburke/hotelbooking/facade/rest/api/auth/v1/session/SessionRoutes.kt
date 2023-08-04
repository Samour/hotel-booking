package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import io.javalin.Javalin
import me.aburke.hotelbooking.facade.rest.Routes
import me.aburke.hotelbooking.facade.rest.authentication.EndpointRole

class SessionRoutes(
    private val getSessionHandler: GetSessionHandler,
    private val logInHandler: LogInHandler,
) : Routes {

    override fun addRoutes(app: Javalin) {
        app.get("/api/auth/v1/session", getSessionHandler, EndpointRole.Any)
            .post("/api/auth/v1/session", logInHandler, EndpointRole.Public)
    }
}
