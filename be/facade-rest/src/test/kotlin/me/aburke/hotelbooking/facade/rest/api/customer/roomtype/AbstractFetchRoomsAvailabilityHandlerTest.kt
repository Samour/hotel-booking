package me.aburke.hotelbooking.facade.rest.api.customer.roomtype

import io.javalin.Javalin
import io.mockk.every
import io.mockk.verify
import me.aburke.hotelbooking.facade.rest.Stubs
import me.aburke.hotelbooking.facade.rest.TestRequest
import me.aburke.hotelbooking.facade.rest.createRandomSession
import me.aburke.hotelbooking.model.date.DateRange
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.room.ListRoomsDetails
import me.aburke.hotelbooking.ports.scenario.room.ListRoomsResult
import me.aburke.hotelbooking.ports.scenario.room.RoomAvailability
import me.aburke.hotelbooking.ports.scenario.room.RoomDescription
import me.aburke.hotelbooking.ports.scenario.room.RoomTypeInfo
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateDetails
import me.aburke.hotelbooking.ports.scenario.user.GetAuthStateResult
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.time.LocalDate

abstract class AbstractFetchRoomsAvailabilityHandlerTest {

    protected val userId = "user-id"
    protected val searchRangeStart = LocalDate.parse("2023-10-07")!!
    protected val searchRangeEnd = LocalDate.parse("2023-10-17")!!

    protected val roomTypeInfo = (1..3).map { i ->
        RoomTypeInfo(
            roomTypeId = "room-type-id-$i",
            description = RoomDescription(
                title = "room-title-$i",
                pricePerNight = (10 + i) * 10_00,
                description = "room-description-$i",
                imageUrls = (1..5).map { j -> "https://test.local/room-$i-image-url-$j" },
            ),
            roomAvailability = (0..9).map { j ->
                RoomAvailability(
                    date = searchRangeStart.plusDays(j.toLong()),
                    available = (i + j) % 3 != 0,
                )
            },
        )
    }

    private val stubs = Stubs()

    protected lateinit var javalin: Javalin

    protected lateinit var session: UserSession

    @BeforeEach
    fun init() {
        javalin = stubs.make()
    }

    @AfterEach
    fun cleanUp() = stubs.cleanUp()

    protected fun <T : Any> `RUN should fetch room availabilities for unauthenticated user`(
        testRequest: TestRequest<T>,
    ) {
        every {
            stubs.listRoomsPort.run(
                ListRoomsDetails(
                    currentUserId = null,
                    availabilitySearchRange = DateRange(
                        rangeStart = (searchRangeStart),
                        rangeEnd = searchRangeEnd,
                    ),
                ),
            )
        } returns ListRoomsResult(rooms = roomTypeInfo)

        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            s.check {
                verify(exactly = 1) {
                    stubs.listRoomsPort.run(
                        ListRoomsDetails(
                            currentUserId = null,
                            availabilitySearchRange = DateRange(
                                rangeStart = (searchRangeStart),
                                rangeEnd = searchRangeEnd,
                            ),
                        ),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should fetch room availabilities for authenticated user`(testRequest: TestRequest<T>) {
        session = stubs.prepareSession(UserRole.CUSTOMER)
        every {
            stubs.listRoomsPort.run(
                ListRoomsDetails(
                    currentUserId = session.userId,
                    availabilitySearchRange = DateRange(
                        rangeStart = (searchRangeStart),
                        rangeEnd = searchRangeEnd,
                    ),
                ),
            )
        } returns ListRoomsResult(rooms = roomTypeInfo)

        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            s.check {
                verify(exactly = 1) {
                    stubs.listRoomsPort.run(
                        ListRoomsDetails(
                            currentUserId = session.userId,
                            availabilitySearchRange = DateRange(
                                rangeStart = (searchRangeStart),
                                rangeEnd = searchRangeEnd,
                            ),
                        ),
                    )
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should return 400 if date is in invalid format or missing`(
        testRequest: TestRequest<T>,
    ) {
        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should return 401 if invalid session ID provided`(testRequest: TestRequest<T>) {
        session = createRandomSession(UserRole.CUSTOMER)
        every {
            stubs.getAuthStatePort.run(
                GetAuthStateDetails(session.sessionId),
            )
        } returns GetAuthStateResult.SessionDoesNotExist

        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            s.check {
                verify {
                    stubs.getAuthStatePort.run(GetAuthStateDetails(session.sessionId))
                }
            }
            with(stubs) {
                s.verifyStubs()
            }
        }
    }

    protected fun <T : Any> `RUN should return 403 if authenticated session does not have CUSTOMER permission`(
        testRequest: TestRequest<T>,
    ) {
        session = stubs.prepareSession(UserRole.MANAGE_USERS)
        testRequest.executeRequest()

        assertSoftly { s ->
            testRequest.makeAssertions(s)
            with(stubs) {
                s.verifyStubs()
            }
        }
    }
}
