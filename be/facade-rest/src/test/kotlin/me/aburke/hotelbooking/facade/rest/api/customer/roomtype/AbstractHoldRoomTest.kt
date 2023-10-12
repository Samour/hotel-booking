package me.aburke.hotelbooking.facade.rest.api.customer.roomtype

import io.javalin.Javalin
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.room.HoldRoomDetail
import me.aburke.hotelbooking.ports.scenario.room.HoldRoomResult
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.fail
import java.time.Instant
import java.time.LocalDate

abstract class AbstractHoldRoomTest {

    protected val roomTypeId = "room-type-id"
    protected val holdStartDate = LocalDate.parse("2023-10-13")
    protected val holdEndDate = LocalDate.parse("2023-10-18")
    protected val roomHoldId = "room-hold-id"
    protected val holdExpiry = Instant.now().plusSeconds(500)
    protected val removedRoomHoldId = "removed-room-hold-id"

    private val stubs = Stubs()
    protected lateinit var javalin: Javalin
    protected lateinit var session: UserSession

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    protected fun <T : Any> `RUN should place a hold on the specified room type`(
        testRequest: TestRequest<T>,
        withExistingHoldRemoved: Boolean,
    ) {
        session = stubs.prepareSession(UserRole.CUSTOMER)
        every {
            stubs.holdRoomPort.run(
                HoldRoomDetail(
                    userId = session.userId,
                    roomTypeId = roomTypeId,
                    holdStartDate = holdStartDate,
                    holdEndDate = holdEndDate,
                ),
            )
        } returns HoldRoomResult.RoomHoldCreated(
            roomHoldId = roomHoldId,
            holdExpiry = holdExpiry,
            removedRoomHoldId = if (withExistingHoldRemoved) removedRoomHoldId else null,
        )

        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            s.check {
                verify(exactly = 1) {
                    stubs.holdRoomPort.run(
                        HoldRoomDetail(
                            userId = session.userId,
                            roomTypeId = roomTypeId,
                            holdStartDate = holdStartDate,
                            holdEndDate = holdEndDate,
                        ),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should return 409 if stock not available to hold`(testRequest: TestRequest<T>) {
        fail("TODO")
    }

    protected fun <T : Any> `RUN should return 409 if concurrent request received from user`(
        testRequest: TestRequest<T>,
    ) {
        fail("TODO")
    }

    protected fun <T : Any> `RUN should return 403 if client does not have CUSTOMER permission`(
        testRequest: TestRequest<T>,
    ) {
        fail("TODO")
    }
}
