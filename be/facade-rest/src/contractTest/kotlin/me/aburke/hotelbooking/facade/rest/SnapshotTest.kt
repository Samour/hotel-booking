package me.aburke.hotelbooking.facade.rest

import io.javalin.Javalin
import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.rest.client.invoker.ApiClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.http.RequestLine
import okio.Buffer
import java.nio.charset.StandardCharsets
import java.util.*

private const val HTTP_VERSION = "HTTP/snapshot"

private class MessageSnapshotInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val reqSnapshot = takeRequestSnapshot(chain.request())
        val response = chain.proceed(chain.request())
        val resSnapshot = takeResponseSnapshot(response)
        return response
    }

    private fun takeRequestSnapshot(request: Request): String {
        val snapshot = StringJoiner("\n")
        snapshot.add("${request.method} ${RequestLine.requestPath(request.url)} $HTTP_VERSION")
        request.headers.forEach {
            snapshot.add("${it.first}: ${it.second}")
        }

        request.body?.let { body ->
            snapshot.add("")
            val buffer = Buffer()
            body.writeTo(buffer)
            val charset = body.contentType()?.charset() ?: StandardCharsets.UTF_8
            snapshot.add(buffer.readString(charset))
        }

        return snapshot.toString()
    }

    private fun takeResponseSnapshot(response: Response): String {
        val snapshot = StringJoiner("\n")
        snapshot.add("$HTTP_VERSION ${response.code} ${response.message}")
        response.headers.forEach {
            snapshot.add("${it.first}: ${it.second}")
        }

        response.body?.let { body ->
            snapshot.add("")
            val bodyValue = response.peekBody(1024L * 1024L * 4) // 4 MB
            snapshot.add(bodyValue.string())
        }

        return snapshot.toString()
    }
}

fun snapshotTest(javalin: Javalin, case: (ApiClient) -> Unit) = test(javalin) { _, _ ->
    case(
        ApiClient(
            OkHttpClient.Builder()
                .addInterceptor(MessageSnapshotInterceptor())
                .build(),
        ).apply {
            basePath = "http://localhost:${javalin.port()}"
        },
    )
}
