package me.aburke.hotelbooking.scenario.user

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import me.aburke.hotelbooking.ports.repository.UserRepository
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private const val USER_ID = "user-id"

@ExtendWith(MockKExtension::class)
class CreateAnonymousUserScenarioTest {

    @MockK
    lateinit var userRepository: UserRepository

    @InjectMockKs
    lateinit var underTest: CreateAnonymousUserScenario

    @Test
    fun `should create anonymous user`() {
        every {
            userRepository.createAnonymousUser()
        } returns USER_ID

        val result = underTest.run(CreateAnonymousUserScenario.Detail)

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                AnonymousUserCreated(USER_ID)
            )
            s.check {
                verify(exactly = 1) {
                    userRepository.createAnonymousUser()
                }
            }
            s.check {
                confirmVerified(userRepository)
            }
        }
    }
}
