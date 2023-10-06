package me.aburke.hotelbooking.facade.rest.api.admin.roomtype

import io.javalin.Javalin
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.ports.scenario.room.AddRoomTypeDetails
import me.aburke.hotelbooking.ports.scenario.room.AddRoomTypeResult
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class AbstractAddRoomTypeTest {

    protected val title = "title"
    protected val description = "description"
    protected val pricePerNight = 150_00
    protected val stockLevel = 35
    protected val roomTypeId = "room-type-id"

    protected val imageUrls = listOf(
        "image-url-1",
        "image-url-2",
        "image-url-3",
    )

    private val stubs = Stubs()

    protected lateinit var javalin: Javalin

    protected lateinit var sessionId: String

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    protected fun <T : Any> `RUN should create new room type`(withImages: Boolean, testRequest: TestRequest<T>) {
        every {
            stubs.addRoomTypePort.run(
                AddRoomTypeDetails(
                    title = title,
                    description = description,
                    imageUrls = imageUrls.takeIf { withImages } ?: emptyList(),
                    pricePerNight = pricePerNight,
                    stockLevel = stockLevel,
                ),
            )
        } returns AddRoomTypeResult(roomTypeId)

        sessionId = stubs.prepareSession(UserRole.MANAGE_ROOMS).sessionId
        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            s.check {
                verify(exactly = 1) {
                    stubs.addRoomTypePort.run(
                        AddRoomTypeDetails(
                            title = title,
                            description = description,
                            imageUrls = imageUrls.takeIf { withImages } ?: emptyList(),
                            pricePerNight = pricePerNight,
                            stockLevel = stockLevel,
                        ),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should return 403 when user does not have MANAGE_ROOMS permission`(
        testRequest: TestRequest<T>,
    ) {
        sessionId = stubs.prepareSession(UserRole.CUSTOMER).sessionId
        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            with(stubs) {
                s.verifyStubs()
            }
        }
    }
}
