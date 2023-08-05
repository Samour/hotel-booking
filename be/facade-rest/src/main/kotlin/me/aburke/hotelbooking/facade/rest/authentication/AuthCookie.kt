package me.aburke.hotelbooking.facade.rest.authentication

import io.javalin.http.Context
import io.javalin.http.Cookie
import io.javalin.http.SameSite
import me.aburke.hotelbooking.model.user.UserSession

const val AUTH_COOKIE_KEY = "SessionId"
const val USER_SESSION_ATTRIBUTE = "UserSession"

fun Context.getUserSession(): UserSession = attribute(USER_SESSION_ATTRIBUTE)!!

fun Context.setAuthCookie(session: UserSession) = cookie(
    Cookie(
        name = AUTH_COOKIE_KEY,
        value = session.sessionId,
        sameSite = SameSite.STRICT,
        isHttpOnly = true,
    )
)
