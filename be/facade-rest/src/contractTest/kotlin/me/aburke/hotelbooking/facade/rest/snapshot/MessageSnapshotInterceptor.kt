package me.aburke.hotelbooking.facade.rest.snapshot

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.http.RequestLine
import okio.Buffer
import org.json.JSONException
import java.nio.charset.StandardCharsets
import java.util.*

private const val USE_PRETTY_PRINT = true
private const val SIZE_4_MB = 1024L * 1024L * 4
private const val HTTP_VERSION = "HTTP/snapshot"

class MessageSnapshotInterceptor(private val snapshots: SnapshotTest) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        takeRequestSnapshot(chain.request())
        return chain.proceed(chain.request()).also {
            takeResponseSnapshot(it)
        }
    }

    private fun takeRequestSnapshot(request: Request) {
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
            snapshot.add(prettyPrintJson(buffer.readString(charset)))
        }

        snapshots.request.newSnapshot = snapshot.toString()
    }

    private fun takeResponseSnapshot(response: Response) {
        val snapshot = StringJoiner("\n")
        snapshot.add("$HTTP_VERSION ${response.code} ${response.message}")
        response.headers.forEachIndexed { i, it ->
            if (it.first.equals("date", ignoreCase = true)) {
                snapshots.response.ignoreLine(i + 1)
            }
            snapshot.add("${it.first}: ${it.second}")
        }

        if (response.body != null) {
            snapshot.add("")
            val bodyValue = response.peekBody(SIZE_4_MB).string()
            snapshot.add(prettyPrintJson(bodyValue))
        }

        snapshots.response.newSnapshot = snapshot.toString()
    }
}

private val prettyGson = GsonBuilder().setPrettyPrinting().create()

private fun prettyPrintJson(json: String) = try {
    if (USE_PRETTY_PRINT) {
        prettyGson.toJson(JsonParser.parseString(json))
    } else {
        json
    }
} catch (_: JSONException) {
    json
}
