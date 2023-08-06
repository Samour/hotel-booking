package me.aburke.hotelbooking.facade.rest.api.admin.v1.roomtype

import io.javalin.Javalin
import io.javalin.testtools.JavalinTest.test
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.assertThatJson
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.scenario.room.AddRoomTypeDetails
import me.aburke.hotelbooking.scenario.room.AddRoomTypeResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

private const val TITLE = "title"
private const val DESCRIPTION = "description"
private const val STOCK_LEVEL = 35
private const val ROOM_TYPE_ID = "room-type-id"

private val imageUrls = listOf(
    "image-url-1",
    "image-url-2",
    "image-url-3",
)

class AddRoomTypeHandlerTest {

    private val stubs = Stubs()

    private lateinit var javalin: Javalin

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should create new room type`(withImages: Boolean) = test(javalin) { _, client ->
        every {
            stubs.addRoomTypeScenario.run(
                AddRoomTypeDetails(
                    title = TITLE,
                    description = DESCRIPTION,
                    imageUrls = imageUrls.takeIf { withImages } ?: emptyList(),
                    stockLevel = STOCK_LEVEL,
                )
            )
        } returns AddRoomTypeResult(ROOM_TYPE_ID)

        val sessionId = stubs.prepareSession(UserRole.MANAGE_ROOMS)
        val response = client.request("/api/admin/v1/room-type") { rb ->
            rb.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                .header("Content-Type", "application/json")
                .post(makeRequestBody(withImages))
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(201)
            s.assertThat(response.header("Content-Type")).isEqualTo("application/json")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "room_type_id": "$ROOM_TYPE_ID"
                    }
                """.trimIndent()
            )
            s.check {
                verify(exactly = 1) {
                    stubs.addRoomTypeScenario.run(
                        AddRoomTypeDetails(
                            title = TITLE,
                            description = DESCRIPTION,
                            imageUrls = imageUrls.takeIf { withImages } ?: emptyList(),
                            stockLevel = STOCK_LEVEL,
                        )
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    @Test
    fun `should return 403 when user does not have MANAGE_ROOMS permission`() = test(javalin) { _, client ->
        val sessionId = stubs.prepareSession(UserRole.CUSTOMER)
        val response = client.request("/api/admin/v1/room-type") { rb ->
            rb.header("Cookie", "$AUTH_COOKIE_KEY=$sessionId")
                .header("Content-Type", "application/json")
                .post(makeRequestBody(true))
        }

        assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(403)
            s.assertThat(response.header("Content-Type")).isEqualTo("application/problem+json;charset=utf-8")
            s.assertThatJson(response.body?.string()).isEqualTo(
                """
                    {
                        "title": "Forbidden",
                        "code": "FORBIDDEN",
                        "status": 403,
                        "detail": "Insufficient permissions to access resource",
                        "instance": "/api/admin/v1/room-type",
                        "extended_details": []
                    }
                    }
                """.trimIndent()
            )
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    private fun makeRequestBody(withImages: Boolean): RequestBody =
        if (withImages) {
            """
                {
                    "title": "$TITLE",
                    "description": "$DESCRIPTION",
                    "image_urls": [${imageUrls.joinToString(",") { "\"$it\"" }}],
                    "stock_level": $STOCK_LEVEL
                }
            """.trimIndent().toRequestBody("application/json".toMediaType())
        } else {
            """
                {
                    "title": "$TITLE",
                    "description": "$DESCRIPTION",
                    "stock_level": $STOCK_LEVEL
                }
            """.trimIndent().toRequestBody("application/json".toMediaType())
        }
}
