package me.aburke.hotelbooking.user

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.password.PasswordHasher
import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.scenario.user.*
import me.aburke.hotelbooking.stubs.Stubs
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication

private const val LOGIN_ID = "login-id"
private const val RAW_PASSWORD = "raw-password"
private const val NAME = "name"

class SignUpTest {

    private val stubs = Stubs()

    private lateinit var app: KoinApplication
    private lateinit var passwordHasher: PasswordHasher
    private lateinit var underTest: SignUpScenario

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
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = null,
            )
        )

        val userId = (result as? SignUpResult.Success)?.userId
        val userRecord = userId?.let { stubs.userRepositoryStub.getUsers()[it] }
        val passwordHashResult = userRecord?.passwordHash?.let {
            passwordHasher.passwordMatches(RAW_PASSWORD, it)
        }

        SoftAssertions.assertSoftly { s ->
            s.assertThat(result).isInstanceOf(SignUpResult.Success::class.java)
            s.assertThat(stubs.userRepositoryStub.getUsers().keys).containsExactly(userId)
            s.assertThat(stubs.userRepositoryStub.getAnonymousUserIds()).isEmpty()
            s.assertThat(userRecord).usingRecursiveComparison()
                .ignoringFields("passwordHash")
                .isEqualTo(
                    InsertUserRecord(
                        loginId = LOGIN_ID,
                        passwordHash = "",
                        name = NAME,
                        roles = setOf(UserRole.CUSTOMER),
                    )
                )
            s.assertThat(passwordHashResult).isTrue
        }
    }

    @Test
    fun `should return UsernameNotAvailable when loginId is not available for new user`() {
        val firstUserId = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = null,
            )
        ).let {
            Assertions.assertThat(it).isInstanceOf(SignUpResult.Success::class.java)
            (it as SignUpResult.Success).userId
        }

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = null,
            )
        )

        SoftAssertions.assertSoftly { s ->
            s.assertThat(result).isEqualTo(SignUpResult.UsernameNotAvailable)
            s.assertThat(stubs.userRepositoryStub.getUsers().keys).containsExactly(firstUserId)
            s.assertThat(stubs.userRepositoryStub.getAnonymousUserIds()).isEmpty()
        }
    }

    @Test
    fun `should create credentials for anonymous user`() {
        val anonymousUserId = stubs.userRepositoryStub.createAnonymousUser()

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = anonymousUserId,
            )
        )

        val userId = (result as? SignUpResult.Success)?.userId
        val userRecord = userId?.let { stubs.userRepositoryStub.getUsers()[it] }
        val passwordHashResult = userRecord?.passwordHash?.let {
            passwordHasher.passwordMatches(RAW_PASSWORD, it)
        }

        SoftAssertions.assertSoftly { s ->
            s.assertThat(result).isEqualTo(SignUpResult.Success(anonymousUserId))
            s.assertThat(stubs.userRepositoryStub.getUsers().keys).containsExactly(userId)
            s.assertThat(stubs.userRepositoryStub.getAnonymousUserIds()).containsExactly(userId)
            s.assertThat(userRecord).usingRecursiveComparison()
                .ignoringFields("passwordHash")
                .isEqualTo(
                    InsertUserRecord(
                        loginId = LOGIN_ID,
                        passwordHash = "",
                        name = NAME,
                        roles = setOf(UserRole.CUSTOMER),
                    )
                )
            s.assertThat(passwordHashResult).isTrue
        }
    }

    @Test
    fun `should return UserIsNotAnonymous when trying to set credentials on non-anonymous user`() {
        val firstUserId = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = null,
            )
        ).let {
            Assertions.assertThat(it).isInstanceOf(SignUpResult.Success::class.java)
            (it as SignUpResult.Success).userId
        }

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = firstUserId,
            )
        )

        SoftAssertions.assertSoftly { s ->
            s.assertThat(result).isEqualTo(SignUpResult.UserIsNotAnonymous)
            s.assertThat(stubs.userRepositoryStub.getUsers().keys).containsExactly(firstUserId)
        }
    }

    @Test
    fun `should return UsernameNotAvailable when loginId is not available for anonymous user`() {
        val firstUserId = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = null,
            )
        ).let {
            Assertions.assertThat(it).isInstanceOf(SignUpResult.Success::class.java)
            (it as SignUpResult.Success).userId
        }
        val anonymousUserId = stubs.userRepositoryStub.createAnonymousUser()

        val result = underTest.run(
            SignUpDetails(
                loginId = LOGIN_ID,
                rawPassword = RAW_PASSWORD,
                name = NAME,
                anonymousUserId = anonymousUserId,
            )
        )

        SoftAssertions.assertSoftly { s ->
            s.assertThat(result).isEqualTo(SignUpResult.UsernameNotAvailable)
            s.assertThat(stubs.userRepositoryStub.getUsers().keys).containsExactly(firstUserId)
        }
    }
}