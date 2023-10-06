package me.aburke.hotelbooking.facade.rest.api.customer.v1.roomtype

import me.aburke.hotelbooking.facade.rest.RouteRegistry
import me.aburke.hotelbooking.facade.rest.Routes
import me.aburke.hotelbooking.facade.rest.authentication.EndpointRole
import me.aburke.hotelbooking.model.user.UserRole

class RoomRoutes(private val fetchRoomsAvailabilityHandler: FetchRoomsAvailabilityHandler) : Routes {
    override fun addRoutes(registry: RouteRegistry) {
        registry.get(
            "/api/customer/v1/room-type/availability",
            EndpointRole.Optional(EndpointRole.anyOf(UserRole.CUSTOMER)),
        ) {
            fetchRoomsAvailabilityHandler.handle(
                it,
                it.queryParam("availability_range_start"),
                it.queryParam("availability_range_end"),
            )
        }
    }
}
