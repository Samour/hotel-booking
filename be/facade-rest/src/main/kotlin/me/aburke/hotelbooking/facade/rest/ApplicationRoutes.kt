package me.aburke.hotelbooking.facade.rest

class ApplicationRoutes(private val allRoutes: List<Routes>) : Routes {

    override fun addRoutes(registry: RouteRegistry) {
        allRoutes.forEach { it.addRoutes(registry) }
    }
}
