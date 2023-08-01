package me.aburke.hotelbooking.user

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.scenario.user.CreateUserDetails
import me.aburke.hotelbooking.scenario.user.CreateUserResult
import me.aburke.hotelbooking.scenario.user.CreateUserScenario
import me.aburke.hotelbooking.stubs.Stubs
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication

private const val LOGIN_ID = "login-id"
private const val RAW_PASSWORD = "raw-password"
private const val NAME = "name"

private val roles = setOf(UserRole.CUSTOMER)

class CreateUserTest {

    private val stubs = Stubs()

    private lateinit var app: KoinApplication
    private lateinit var passwordHasher: PasswordHasher
    private lateinit var underTest: CreateUserScenario

    @BeforeEach
    fun init() {
        app = stubs.make()
        passwordHasher = app.koin.get()
        underTest = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should insert new user`() {
        val result = underTest.run(
            CreateUserDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                userRoles = roles,
                anonymousUserId = null,
            )
        )

        val userId = (result as? CreateUserResult.Success)?.userId
        val userRecord = userId?.let { stubs.userRepositoryStub.getUsers()[it] }
        val passwordHashResult = userRecord?.passwordHash?.let {
            passwordHasher.passwordMatches(RAW_PASSWORD, it)
        }

        assertSoftly { s ->
            s.assertThat(result).isInstanceOf(CreateUserResult.Success::class.java)
            s.assertThat(stubs.userRepositoryStub.getUsers().keys).containsExactly(userId)
            s.assertThat(stubs.userRepositoryStub.getAnonymousUserIds()).isEmpty()
            s.assertThat(userRecord).usingRecursiveComparison()
                .ignoringFields("passwordHash")
                .isEqualTo(
                    InsertUserRecord(
                        loginId = LOGIN_ID,
                        passwordHash = "",
                        name = NAME,
                        roles = roles,
                    )
                )
            s.assertThat(passwordHashResult).isTrue
        }
    }

    @Test
    fun `should return UsernameNotAvailable when loginId is not available for new user`() {
        val firstUserId = underTest.run(
            CreateUserDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                userRoles = roles,
                anonymousUserId = null,
            )
        ).let {
            assertThat(it).isInstanceOf(CreateUserResult.Success::class.java)
            (it as CreateUserResult.Success).userId
        }

        val result = underTest.run(
            CreateUserDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                userRoles = roles,
                anonymousUserId = null,
            )
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(CreateUserResult.UsernameNotAvailable)
            s.assertThat(stubs.userRepositoryStub.getUsers().keys).containsExactly(firstUserId)
            s.assertThat(stubs.userRepositoryStub.getAnonymousUserIds()).isEmpty()
        }
    }

    @Test
    fun `should create credentials for anonymous user`() {
        val anonymousUserId = stubs.userRepositoryStub.createAnonymousUser()

        val result = underTest.run(
            CreateUserDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                userRoles = roles,
                anonymousUserId = anonymousUserId,
            )
        )

        val userId = (result as? CreateUserResult.Success)?.userId
        val userRecord = userId?.let { stubs.userRepositoryStub.getUsers()[it] }
        val passwordHashResult = userRecord?.passwordHash?.let {
            passwordHasher.passwordMatches(RAW_PASSWORD, it)
        }

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(CreateUserResult.Success(anonymousUserId))
            s.assertThat(stubs.userRepositoryStub.getUsers().keys).containsExactly(userId)
            s.assertThat(stubs.userRepositoryStub.getAnonymousUserIds()).containsExactly(userId)
            s.assertThat(userRecord).usingRecursiveComparison()
                .ignoringFields("passwordHash")
                .isEqualTo(
                    InsertUserRecord(
                        loginId = LOGIN_ID,
                        passwordHash = "",
                        name = NAME,
                        roles = roles,
                    )
                )
            s.assertThat(passwordHashResult).isTrue
        }
    }

    @Test
    fun `should return UserIsNotAnonymous when trying to set credentials on non-anonymous user`() {
        val firstUserId = underTest.run(
            CreateUserDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                userRoles = roles,
                anonymousUserId = null,
            )
        ).let {
            assertThat(it).isInstanceOf(CreateUserResult.Success::class.java)
            (it as CreateUserResult.Success).userId
        }

        val result = underTest.run(
            CreateUserDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                userRoles = roles,
                anonymousUserId = firstUserId,
            )
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(CreateUserResult.UserIsNotAnonymous)
            s.assertThat(stubs.userRepositoryStub.getUsers().keys).containsExactly(firstUserId)
        }
    }

    @Test
    fun `should return UsernameNotAvailable when loginId is not available for anonymous user`() {
        val firstUserId = underTest.run(
            CreateUserDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                userRoles = roles,
                anonymousUserId = null,
            )
        ).let {
            assertThat(it).isInstanceOf(CreateUserResult.Success::class.java)
            (it as CreateUserResult.Success).userId
        }
        val anonymousUserId = stubs.userRepositoryStub.createAnonymousUser()

        val result = underTest.run(
            CreateUserDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                userRoles = roles,
                anonymousUserId = anonymousUserId,
            )
        )

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(CreateUserResult.UsernameNotAvailable)
            s.assertThat(stubs.userRepositoryStub.getUsers().keys).containsExactly(firstUserId)
        }
    }
}
