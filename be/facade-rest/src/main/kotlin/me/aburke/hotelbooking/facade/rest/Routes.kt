package me.aburke.hotelbooking.facade.rest

import io.javalin.Javalin

interface Routes {

    fun addRoutes(app: Javalin)
}
