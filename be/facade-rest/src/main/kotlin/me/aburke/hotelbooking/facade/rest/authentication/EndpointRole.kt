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

    /**
     * Credentials do not need to be supplied (ie may be called as Public)
     * If credentials are supplied, they must satisfy {@param endpointRole}
     */
    data class Optional(val endpointRole: EndpointRole) : EndpointRole {
        override fun allowedForRoles(userRoles: Set<UserRole>): Boolean = endpointRole.allowedForRoles(userRoles)
    }

    companion object {

        fun anyOf(vararg roles: UserRole) = AnyOf(setOf(*roles))

        fun allOf(vararg roles: UserRole) = AllOf(setOf(*roles))
    }
}
