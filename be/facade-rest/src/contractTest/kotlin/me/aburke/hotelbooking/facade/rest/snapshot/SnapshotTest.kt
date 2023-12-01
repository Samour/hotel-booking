package me.aburke.hotelbooking.facade.rest.snapshot

import io.javalin.Javalin
import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.rest.client.invoker.ApiClient
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import java.lang.StackWalker.StackFrame
import java.lang.reflect.Method
import kotlin.streams.asSequence

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

fun snapshotTest(javalin: Javalin, case: (ApiClient) -> Unit) = test(javalin) { _, _ ->
    val testMethod = determineTestMethod()
    val snapshotTest = SnapshotTest.fromTestMethod(testMethod.className, testMethod.methodName)
    val apiClient = ApiClient(
        OkHttpClient.Builder()
            .addInterceptor(MessageSnapshotInterceptor(snapshotTest))
            .build(),
    ).apply {
        basePath = "http://localhost:${javalin.port()}"
    }

    case(apiClient)
    snapshotTest.request.verify()
    snapshotTest.response.verify()
}

@Suppress("NOTHING_TO_INLINE")
private inline fun determineTestMethod(): StackFrame = StackWalker.getInstance(
    StackWalker.Option.RETAIN_CLASS_REFERENCE,
)
    .walk { frames ->
        frames.asSequence()
            .filter { frame ->
                val method = frame.declaringClass.getMethodOrNull(frame.methodName, *frame.methodType.parameterArray())
                method?.annotations?.any {
                    it is Test
                } ?: false
            }.firstOrNull()
    } ?: throw IllegalStateException("Could not determine test method")

private fun Class<*>.getMethodOrNull(name: String, vararg parameterTypes: Class<*>): Method? = try {
    getMethod(name, *parameterTypes)
} catch (_: NoSuchMethodException) {
    null
}
