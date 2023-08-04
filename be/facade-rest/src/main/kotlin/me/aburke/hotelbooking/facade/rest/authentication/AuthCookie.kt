package me.aburke.hotelbooking.facade.rest.authentication

import io.javalin.http.Context
import me.aburke.hotelbooking.model.user.UserSession

const val AUTH_COOKIE_KEY = "SessionId"
const val USER_SESSION_ATTRIBUTE = "UserSession"

fun Context.getUserSession(): UserSession = attribute(USER_SESSION_ATTRIBUTE)!!
