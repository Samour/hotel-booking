package me.aburke.hotelbooking.facade.rest

import io.javalin.testtools.HttpClient
import io.javalin.testtools.JavalinTest.test
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

data class Endpoint(
    val path: String,
    val method: String,
    val expectEnabled: Boolean,
) {

    val property = "endpoints${path.replace('/', '.')}.${method.lowercase()}"

    fun makeRequest(client: HttpClient) =
        when (method) {
            "POST" -> client.post(path, "{}")
            else -> client.request(path) { it.method(method, null) }
        }
}

class EndpointFeatureFlagTest {

    private lateinit var stubs: Stubs

    @BeforeEach
    fun init() {
        stubs = Stubs()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    @ParameterizedTest
    @ArgumentsSource(EndpointsProvider::class)
    fun `disabled endpoint should not be accessible`(endpoints: List<Endpoint>) {
        val javalin = stubs.make(
            endpoints.filter { !it.expectEnabled }
                .associate { it.property to "DISABLED" },
        )

        test(javalin) { _, client ->
            val responses = endpoints.map { endpoint ->
                endpoint to endpoint.makeRequest(client)
            }

            assertSoftly { s ->
                responses.forEach { (endpoint, response) ->
                    if (endpoint.expectEnabled) {
                        s.assertThat(response.code)
                            .withFailMessage(
                                "Expected endpoint to exist, but 404 returned: ${endpoint.method} " +
                                    endpoint.path,
                            )
                            .isNotEqualTo(404)
                    } else {
                        s.assertThat(response.code)
                            .withFailMessage(
                                "Expected endpoint to be not found, but ${response.code} returned: " +
                                    "${endpoint.method} ${endpoint.path}",
                            )
                            .isEqualTo(404)
                    }
                }
            }
        }
    }
}

class EndpointsProvider : ArgumentsProvider {

    private val endpoints = listOf(
        "GET" to "/api/auth/v1/session",
        "POST" to "/api/auth/v1/session",
        "POST" to "/api/auth/v1/session/anonymous",

        "POST" to "/api/auth/v1/user",

        "POST" to "/api/admin/v1/user",

        "POST" to "/api/admin/v1/room-type",
    )

    override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
        return endpoints.stream().map { (disabledMethod, disabledPath) ->
            Arguments.of(
                endpoints.map { (method, path) ->
                    Endpoint(
                        path = path,
                        method = method,
                        expectEnabled = method != disabledMethod || path != disabledPath,
                    )
                },
            )
        }
    }
}
