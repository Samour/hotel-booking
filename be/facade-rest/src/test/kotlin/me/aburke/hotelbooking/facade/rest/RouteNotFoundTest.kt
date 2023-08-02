package me.aburke.hotelbooking.facade.rest

import io.javalin.Javalin
import io.javalin.testtools.JavalinTest.test
import okhttp3.MediaType.Companion.toMediaType
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RouteNotFoundTest {

    private lateinit var stubs: Stubs
    private lateinit var javalin: Javalin

    @BeforeEach
    fun init() {
        stubs = Stubs()
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    @Test
    fun `should return NOT_FOUND when resource not found`() = test(javalin) { _, client ->
        val response = client.get("/not-an-endpoint")

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(404)
            s.assertThat(response.body?.contentType()).isEqualTo("application/problem+json;charset=utf-8".toMediaType())
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "title": "Resource Not Found",
                        "code": "RESOURCE_NOT_FOUND",
                        "status": 404,
                        "detail": "Resource does not exist",
                        "instance": "/not-an-endpoint",
                        "extended_details": []
                    }
                """.trimIndent()
            )
        }
    }
}
