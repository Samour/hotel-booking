package me.aburke.hotelbooking.facade.rest.authentication

import io.javalin.security.RouteRole
import me.aburke.hotelbooking.model.user.UserRole

sealed interface EndpointRole : RouteRole {

    fun allowedForRoles(userRoles: Set<UserRole>): Boolean

    data object Public : EndpointRole {
        override fun allowedForRoles(userRoles: Set<UserRole>): Boolean = true
    }

    data object Any : EndpointRole {
        override fun allowedForRoles(userRoles: Set<UserRole>): Boolean = true
    }

    data class AnyOf(val roles: Set<UserRole>) : EndpointRole {
        override fun allowedForRoles(userRoles: Set<UserRole>): Boolean =
            roles.intersect(userRoles).isNotEmpty()
    }

    data class AllOf(val roles: Set<UserRole>) : EndpointRole {
        override fun allowedForRoles(userRoles: Set<UserRole>): Boolean =
            userRoles.containsAll(roles)
    }
}
