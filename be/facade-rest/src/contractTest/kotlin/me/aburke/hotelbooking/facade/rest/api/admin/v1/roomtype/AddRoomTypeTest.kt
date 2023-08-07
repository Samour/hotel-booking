package me.aburke.hotelbooking.facade.rest.api.admin.v1.roomtype

import io.javalin.Javalin
import io.javalin.testtools.JavalinTest.test
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.client
import me.aburke.hotelbooking.facade.rest.parseResponse
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.ports.scenario.room.AddRoomTypeDetails
import me.aburke.hotelbooking.ports.scenario.room.AddRoomTypeResult
import me.aburke.hotelbooking.rest.client.api.AdminApi
import me.aburke.hotelbooking.rest.client.invoker.ApiException
import me.aburke.hotelbooking.rest.client.model.AddRoomType201Response
import me.aburke.hotelbooking.rest.client.model.AddRoomTypeRequest
import me.aburke.hotelbooking.rest.client.model.ProblemResponse
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

class AddRoomTypeTest {

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
    fun `should create new room type`(withImages: Boolean) = test(javalin) { _, _ ->
        every {
            stubs.addRoomTypePort.run(
                AddRoomTypeDetails(
                    title = TITLE,
                    description = DESCRIPTION,
                    imageUrls = imageUrls.takeIf { withImages } ?: emptyList(),
                    stockLevel = STOCK_LEVEL,
                )
            )
        } returns AddRoomTypeResult(ROOM_TYPE_ID)

        val sessionId = stubs.prepareSession(UserRole.MANAGE_ROOMS)
        val response = AdminApi(javalin.client(sessionId)).addRoomTypeWithHttpInfo(
            AddRoomTypeRequest().also {
                it.title = TITLE
                it.description = DESCRIPTION
                it.stockLevel = STOCK_LEVEL
                if (withImages) {
                    it.imageUrls = imageUrls
                }
            }
        )

        SoftAssertions.assertSoftly { s ->
            s.assertThat(response.statusCode).isEqualTo(201)
            s.assertThat(response.data).isEqualTo(
                AddRoomType201Response().apply {
                    roomTypeId = ROOM_TYPE_ID
                }
            )
            s.check {
                verify(exactly = 1) {
                    stubs.addRoomTypePort.run(
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
    fun `should return 403 when user does not have MANAGE_ROOMS permission`() = test(javalin) { _, _ ->
        val sessionId = stubs.prepareSession(UserRole.CUSTOMER)
        val response = assertThrows<ApiException> {
            AdminApi(javalin.client(sessionId)).addRoomType(
                AddRoomTypeRequest().also {
                    it.title = TITLE
                    it.description = DESCRIPTION
                    it.stockLevel = STOCK_LEVEL
                    it.imageUrls = imageUrls
                }
            )
        }
        val responseBody = response.responseBody.parseResponse<ProblemResponse>()

        SoftAssertions.assertSoftly { s ->
            s.assertThat(response.code).isEqualTo(403)
            s.assertThat(response.responseHeaders["Content-Type"]).containsExactly("application/problem+json;charset=utf-8")
            s.assertThat(responseBody).isEqualTo(
                ProblemResponse().apply {
                    title = "Forbidden"
                    code = "FORBIDDEN"
                    status = 403
                    detail = "Insufficient permissions to access resource"
                    instance = "/api/admin/v1/room-type"
                    extendedDetails = emptyList()
                }
            )
            with(stubs) {
                s.verifyStubs()
            }
        }
    }
}
