package me.aburke.hotelbooking.facade.rest

import io.javalin.Javalin
import io.javalin.http.Handler
import me.aburke.hotelbooking.facade.rest.authentication.EndpointRole
import org.slf4j.LoggerFactory

typealias PropertySource = (String, String) -> String

class RouteRegistry(
    private val javalin: Javalin,
    private val configSource: PropertySource,
) {

    private val logger = LoggerFactory.getLogger(RouteRegistry::class.java)

    private fun endpointEnabled(method: String, path: String) =
        configSource(
            "endpoints${path.replace('/', '.')}.${method.uppercase()}",
            "ENABLED",
        ).uppercase() != "DISABLED"

    fun get(url: String, endpointRole: EndpointRole, handler: Handler): RouteRegistry {
        if (endpointEnabled("get", url)) {
            javalin.get(url, handler, endpointRole)
        } else {
            logger.info("Endpoint disabled via config: GET {}", url)
        }

        return this
    }

    fun post(url: String, endpointRole: EndpointRole, handler: Handler): RouteRegistry {
        if (endpointEnabled("post", url)) {
            javalin.post(url, handler, endpointRole)
        } else {
            logger.info("Endpoint disabled via config: POST {}", url)
        }

        return this
    }
}
