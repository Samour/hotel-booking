package me.aburke.hotelbooking.facade.rest

import io.javalin.Javalin
import me.aburke.hotelbooking.rest.client.invoker.ApiClient
import me.aburke.hotelbooking.rest.client.invoker.auth.ApiKeyAuth

fun Javalin.client(sessionId: String? = null) = ApiClient().also {
    it.withSessionId(sessionId)
    it.basePath = "http://localhost:${port()}"
}

fun ApiClient.withSessionId(sessionId: String?) = apply {
    (authentications["session"] as? ApiKeyAuth)?.apiKey = sessionId
}
