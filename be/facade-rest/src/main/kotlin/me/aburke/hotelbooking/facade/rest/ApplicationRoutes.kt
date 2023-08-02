package me.aburke.hotelbooking.facade.rest

import io.javalin.Javalin

class ApplicationRoutes(private val allRoutes: List<Routes>) : Routes {

    override fun addRoutes(app: Javalin) {
        allRoutes.forEach { it.addRoutes(app) }
    }
}
