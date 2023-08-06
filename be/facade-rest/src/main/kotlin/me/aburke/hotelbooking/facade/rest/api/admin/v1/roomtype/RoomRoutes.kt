package me.aburke.hotelbooking.facade.rest.api.admin.v1.roomtype

import me.aburke.hotelbooking.facade.rest.RouteRegistry
import me.aburke.hotelbooking.facade.rest.Routes
import me.aburke.hotelbooking.facade.rest.authentication.EndpointRole
import me.aburke.hotelbooking.model.user.UserRole

class RoomRoutes(
    private val addRoomTypeHandler: AddRoomTypeHandler,
) : Routes {

    override fun addRoutes(registry: RouteRegistry) {
        registry.post("/api/admin/v1/room-type", EndpointRole.allOf(UserRole.MANAGE_ROOMS), addRoomTypeHandler)
    }
}
