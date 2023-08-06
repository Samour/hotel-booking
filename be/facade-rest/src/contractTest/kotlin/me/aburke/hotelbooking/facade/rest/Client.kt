package me.aburke.hotelbooking.facade.rest

import io.javalin.Javalin
import me.aburke.hotelbooking.rest.client.api.AuthApi
import me.aburke.hotelbooking.rest.client.invoker.ApiClient
import me.aburke.hotelbooking.rest.client.invoker.auth.ApiKeyAuth

fun Javalin.client(sessionId: String? = null) = AuthApi(
    ApiClient().also {
        (it.authentications["session"] as? ApiKeyAuth)?.apiKey = sessionId
        it.basePath = "http://localhost:${port()}"
    }
)
