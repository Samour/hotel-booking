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
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.reflect.Method
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.streams.asSequence

private const val HTTP_VERSION = "HTTP/snapshot"

private class MessageSnapshotInterceptor(private val snapshots: SnapshotTest) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        snapshots.request.takeRequestSnapshot(chain.request())
        return chain.proceed(chain.request()).also {
            snapshots.response.takeResponseSnapshot(it)
        }
    }

    private fun SnapshotFile.takeRequestSnapshot(request: Request) {
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

        newSnapshot = snapshot.toString()
    }

    private fun SnapshotFile.takeResponseSnapshot(response: Response) {
        val snapshot = StringJoiner("\n")
        snapshot.add("$HTTP_VERSION ${response.code} ${response.message}")
        response.headers.forEachIndexed { i, it ->
            if (it.first.equals("date", ignoreCase = true)) {
                ignoreLine(i + 1)
            }
            snapshot.add("${it.first}: ${it.second}")
        }

        response.body?.let { body ->
            snapshot.add("")
            val bodyValue = response.peekBody(1024L * 1024L * 4) // 4 MB
            snapshot.add(bodyValue.string())
        }

        newSnapshot = snapshot.toString()
    }
}

private val snapshotsRoot = System.getenv("TEST_SNAPSHOT_DIR")

data class SnapshotTest(val request: SnapshotFile, val response: SnapshotFile) {
    companion object {
        fun fromTestMethod(className: String, method: String) = SnapshotTest(
            request = SnapshotFile(createFileName(className, method), "request"),
            response = SnapshotFile(createFileName(className, method), "response"),
        )

        private fun createFileName(className: String, methodName: String) =
            "${className.replace(".", "/")}/$methodName"
    }
}

class SnapshotFile(fnamePrefix: String, private val specType: String) {

    private val specFname = "$fnamePrefix.$specType.spec"
    private val file = File("$snapshotsRoot/$specFname")
    private val newSpecFile = File("$snapshotsRoot/$fnamePrefix.$specType.spec.new")
    var newSnapshot: String? = null
    private val ignoredLines: MutableSet<Int> = mutableSetOf()

    fun ignoreLine(lineNo: Int) = ignoredLines.add(lineNo)

    fun verify() {
        newSnapshot?.let { newSnapshot ->
            file.parentFile.mkdirs()
            if (!file.exists()) {
                println("No snapshot exists for $specFname; writing result to new spec file")
                newSpecFile.writeText(newSnapshot)
                return
            }

            val oldSnapshot = file.readText()
            if (!valuesEquivalent(oldSnapshot, newSnapshot)) {
                println("Writing mismatch spec to $specFname.new")
                newSpecFile.writeText(newSnapshot)
                throw AssertionError(
                    "${specType.replaceFirstChar { it.titlecase() }} spec does not match the new data",
                )
            }

            if (newSpecFile.exists()) {
                println("Deleting new spec file for $specFname")
                newSpecFile.delete()
            }
        }
    }

    private fun valuesEquivalent(rawOldValue: String, rawNewValue: String): Boolean =
        if (ignoredLines.isEmpty()) {
            rawOldValue == rawNewValue
        } else {
            normalizedValue(rawOldValue) == normalizedValue(rawNewValue)
        }

    private fun normalizedValue(rawValue: String) = rawValue.split('\n')
        .filterIndexed { i, _ -> !ignoredLines.contains(i) }
}

fun snapshotTest(javalin: Javalin, case: (ApiClient) -> Unit) = test(javalin) { _, _ ->
    val testMethod = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk { frames ->
        frames.asSequence()
            .filter { frame ->
                val method = frame.declaringClass.getMethodOrNull(frame.methodName, *frame.methodType.parameterArray())
                method?.annotations?.any {
                    it is Test
                } ?: false
            }.firstOrNull()
    } ?: throw IllegalStateException("Could not determine test method")
    val snapshotTest = SnapshotTest.fromTestMethod(testMethod.className, testMethod.methodName)

    case(
        ApiClient(
            OkHttpClient.Builder()
                .addInterceptor(MessageSnapshotInterceptor(snapshotTest))
                .build(),
        ).apply {
            basePath = "http://localhost:${javalin.port()}"
        },
    )
    snapshotTest.request.verify()
    snapshotTest.response.verify()
}

private fun Class<*>.getMethodOrNull(name: String, vararg parameterTypes: Class<*>): Method? = try {
    getMethod(name, *parameterTypes)
} catch (_: NoSuchMethodException) {
    null
}
