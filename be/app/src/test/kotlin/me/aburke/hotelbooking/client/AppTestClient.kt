package me.aburke.hotelbooking.client

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import io.javalin.testtools.HttpClient
import me.aburke.hotelbooking.facade.rest.api.auth.v1.session.LogInRequest
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import me.aburke.hotelbooking.facade.rest.restObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

val objectMapper = restObjectMapper()

class AppTestClient(private val client: HttpClient) {

    private var sessionId: String? = null

    fun logIn(loginId: String, password: String): Response =
        client.request("/api/auth/v1/session") {
            it.header("Content-Type", "application/json")
                .post(
                    objectMapper.writeValueAsString(
                        LogInRequest(
                            loginId = loginId,
                            password = password,
                        )
                    ).toRequestBody("application/json".toMediaType())
                )
        }.also { response ->
            response.headers("Set-Cookie").firstOrNull {
                it.startsWith("$AUTH_COOKIE_KEY=")
            }?.let {
                sessionId = "$AUTH_COOKIE_KEY=([^;]*)(;.*)?".toRegex().matchEntire(it)
                    ?.groupValues?.get(1)
            }
        }

    fun getSession(): Response =
        client.get("/api/auth/v1/session") {
            it.withCredentials()
        }

    private fun Request.Builder.withCredentials() = sessionId?.let {
        header("Cookie", "$AUTH_COOKIE_KEY=$it")
    }
}

inline fun <reified T> Response.readBody(): T? =
    body?.let {
        try {
            objectMapper.readValue(it.string())
        } catch (_: JsonProcessingException) {
            null
        }
    }
