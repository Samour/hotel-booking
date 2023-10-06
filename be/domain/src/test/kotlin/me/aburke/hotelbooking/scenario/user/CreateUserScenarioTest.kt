package me.aburke.hotelbooking.scenario.user

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.ports.repository.InsertUserResult
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.ports.scenario.user.CreateUserDetails
import me.aburke.hotelbooking.ports.scenario.user.CreateUserResult
import org.assertj.core.api.SoftAssertions
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private const val LOGIN_ID = "login-id"
private const val NAME = "name"
private const val RAW_PASSWORD = "raw-password"
private const val HASHED_PASSWORD = "hashed-password"
private const val USER_ID = "user-id"

private val roles = setOf(UserRole.MANAGE_USERS, UserRole.MANAGE_ROOMS)
private val roleNames = setOf(UserRole.MANAGE_USERS.name, UserRole.MANAGE_ROOMS.name)

@ExtendWith(MockKExtension::class)
class CreateUserScenarioTest {

    @MockK
    lateinit var passwordHasher: PasswordHasher

    @MockK
    lateinit var userRepository: UserRepository

    @InjectMockKs
    lateinit var underTest: CreateUserScenario

    @Test
    fun `should insert new user`() {
        every {
            passwordHasher.hashPassword(RAW_PASSWORD)
        } returns HASHED_PASSWORD
        every {
            userRepository.insertUser(
                InsertUserRecord(
                    loginId = LOGIN_ID,
                    passwordHash = HASHED_PASSWORD,
                    name = NAME,
                    roles = roleNames,
                ),
            )
        } returns InsertUserResult.UserInserted(
            userId = USER_ID,
        )

        val result = underTest.run(
            CreateUserDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                userRoles = roles,
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                CreateUserResult.Success(
                    userId = USER_ID,
                ),
            )
            s.check {
                verify(exactly = 1) {
                    passwordHasher.hashPassword(RAW_PASSWORD)
                }
            }
            s.check {
                verify(exactly = 1) {
                    userRepository.insertUser(
                        InsertUserRecord(
                            loginId = LOGIN_ID,
                            passwordHash = HASHED_PASSWORD,
                            name = NAME,
                            roles = roleNames,
                        ),
                    )
                }
            }
            s.confirmMocks()
        }
    }

    @Test
    fun `should return UsernameNotAvailable when loginId is not available for new user`() {
        every {
            passwordHasher.hashPassword(RAW_PASSWORD)
        } returns HASHED_PASSWORD
        every {
            userRepository.insertUser(
                InsertUserRecord(
                    loginId = LOGIN_ID,
                    passwordHash = HASHED_PASSWORD,
                    name = NAME,
                    roles = roleNames,
                ),
            )
        } returns InsertUserResult.LoginIdUniquenessViolation

        val result = underTest.run(
            CreateUserDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                userRoles = roles,
            ),
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                CreateUserResult.UsernameNotAvailable,
            )
            s.check {
                verify(exactly = 1) {
                    passwordHasher.hashPassword(RAW_PASSWORD)
                }
            }
            s.check {
                verify(exactly = 1) {
                    userRepository.insertUser(
                        InsertUserRecord(
                            loginId = LOGIN_ID,
                            passwordHash = HASHED_PASSWORD,
                            name = NAME,
                            roles = roleNames,
                        ),
                    )
                }
            }
            s.confirmMocks()
        }
    }

    private fun SoftAssertions.confirmMocks() = check {
        confirmVerified(
            passwordHasher,
            userRepository,
        )
    }
}
