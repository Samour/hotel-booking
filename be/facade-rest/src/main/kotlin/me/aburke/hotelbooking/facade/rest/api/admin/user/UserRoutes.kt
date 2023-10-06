package me.aburke.hotelbooking.facade.rest.api.admin.user

import me.aburke.hotelbooking.facade.rest.RouteRegistry
import me.aburke.hotelbooking.facade.rest.Routes
import me.aburke.hotelbooking.facade.rest.authentication.EndpointRole
import me.aburke.hotelbooking.model.user.UserRole

class UserRoutes(
    private val createUserHandler: CreateUserHandler,
) : Routes {

    override fun addRoutes(registry: RouteRegistry) {
        registry.post("/api/admin/v0/user", EndpointRole.allOf(UserRole.MANAGE_USERS), createUserHandler)
    }
}
