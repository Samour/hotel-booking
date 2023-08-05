package me.aburke.hotelbooking.facade.rest

import io.javalin.Javalin
import io.javalin.http.Handler
import me.aburke.hotelbooking.facade.rest.authentication.EndpointRole

typealias PropertySource = (String, String) -> String

class RouteRegistry(
    private val javalin: Javalin,
    private val configSource: PropertySource,
) {

    private fun endpointEnabled(method: String, path: String) =
        configSource(
            "endpoints${path.replace('/', '.')}.${method.lowercase()}",
            "ENABLED",
        ).uppercase() != "DISABLED"

    fun get(url: String, endpointRole: EndpointRole, handler: Handler): RouteRegistry {
        if (endpointEnabled("get", url)) {
            javalin.get(url, handler, endpointRole)
        }

        return this
    }

    fun post(url: String, endpointRole: EndpointRole, handler: Handler): RouteRegistry {
        if (endpointEnabled("post", url)) {
            javalin.post(url, handler, endpointRole)
        }

        return this
    }
}
