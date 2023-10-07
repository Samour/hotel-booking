package me.aburke.hotelbooking.lock

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import me.aburke.hotelbooking.ports.repository.LockRepository
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private const val LOCK_NAMESPACE = "lock-namespace"
private const val LOCK_KEY = "lock-key"
private const val LOCK_EXPIRY_SECONDS = 6000

private sealed interface TestOutcome {

    data object ActionExecuted : TestOutcome

    data object ActionNotExecuted : TestOutcome
}

@ExtendWith(MockKExtension::class)
class FastFailLockTest {

    @MockK
    lateinit var lockRepository: LockRepository

    private lateinit var underTest: FastFailLock

    @BeforeEach
    fun init() {
        underTest = FastFailLock(LOCK_NAMESPACE, LOCK_EXPIRY_SECONDS, lockRepository)
    }

    @Test
    fun `should acquire lock, execute action then release lock`() {
        val nonceSlot = slot<String>()
        every {
            lockRepository.acquireLock(
                eq("$LOCK_NAMESPACE:$LOCK_KEY"),
                capture(nonceSlot),
                eq(LOCK_EXPIRY_SECONDS),
            )
        } returns true
        every {
            lockRepository.releaseLock(eq("$LOCK_NAMESPACE:$LOCK_KEY"), any())
        } returns Unit

        val result = underTest.execute(LOCK_KEY, TestOutcome.ActionNotExecuted) {
            TestOutcome.ActionExecuted
        }

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(TestOutcome.ActionExecuted)
            s.check {
                verify(exactly = 1) {
                    lockRepository.acquireLock(
                        "$LOCK_NAMESPACE:$LOCK_KEY",
                        nonceSlot.captured,
                        LOCK_EXPIRY_SECONDS,
                    )
                }
            }
            s.check {
                verify(exactly = 1) {
                    lockRepository.releaseLock("$LOCK_NAMESPACE:$LOCK_KEY", nonceSlot.captured)
                }
            }
            s.check {
                confirmVerified(lockRepository)
            }
        }
    }

    @Test
    fun `should return specified value on lock conflict`() {
        every {
            lockRepository.acquireLock(eq("$LOCK_NAMESPACE:$LOCK_KEY"), any(), eq(LOCK_EXPIRY_SECONDS))
        } returns false

        val result = underTest.execute(LOCK_KEY, TestOutcome.ActionNotExecuted) {
            TestOutcome.ActionExecuted
        }

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(TestOutcome.ActionNotExecuted)
            s.check {
                verify(exactly = 1) {
                    lockRepository.acquireLock(
                        eq(
                            "$LOCK_NAMESPACE:$LOCK_KEY",
                        ),
                        any(),
                        eq(LOCK_EXPIRY_SECONDS),
                    )
                }
            }
            s.check {
                confirmVerified(lockRepository)
            }
        }
    }
}
