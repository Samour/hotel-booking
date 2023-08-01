package me.aburke.hotelbooking.repository.postgres

import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.ports.repository.InsertUserResult
import me.aburke.hotelbooking.ports.repository.PromoteAnonymousUserResult
import me.aburke.hotelbooking.ports.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.sql.Connection

private const val LOGIN_ID = "login-id"
private const val PASSWORD_HASH = "password-hash"
private const val NAME = "name"

private data class UserRecord(
    val userId: String,
    val userRoles: Set<String>,
    val name: String,
    val credential: UserCredentialRecord?,
)

private data class UserCredentialRecord(
    val loginId: String,
    val passwordHash: String,
)

class PostgresUserRepositoryTest {

    private lateinit var app: KoinApplication
    private lateinit var connection: Connection

    private lateinit var underTest: UserRepository

    @BeforeEach
    fun init() {
        app = createApp()
        connection = app.koin.get()
        underTest = app.koin.get()
    }

    @AfterEach
    fun cleanUp() = app.close()

    @Test
    fun `should create user record for anonymous user`() {
        val result = underTest.createAnonymousUser()

        val users = loadAllUsers()

        assertThat(users).containsExactly(
            UserRecord(
                userId = result,
                userRoles = setOf(UserRole.CUSTOMER.name),
                name = "",
                credential = null,
            )
        )
    }

    @Test
    fun `should create user record with credentials`() {
        val result = underTest.insertUser(
            InsertUserRecord(
                loginId = LOGIN_ID,
                passwordHash = PASSWORD_HASH,
                name = NAME,
                roles = setOf(UserRole.MANAGE_ROOMS),
            )
        )

        val userId = (result as? InsertUserResult.UserInserted)?.userId
        val users = loadAllUsers()

        assertSoftly { s ->
            s.assertThat(result).isInstanceOf(InsertUserResult.UserInserted::class.java)
            s.assertThat(users).containsExactly(
                UserRecord(
                    userId = userId ?: "",
                    userRoles = setOf(UserRole.MANAGE_ROOMS.name),
                    name = NAME,
                    credential = UserCredentialRecord(
                        loginId = LOGIN_ID,
                        passwordHash = PASSWORD_HASH,
                    ),
                )
            )
        }
    }

    @Test
    fun `should return LoginIdUniquenessViolation when login ID is already used in new user insert`() {
        val firstUserId = underTest.insertUser(
            InsertUserRecord(
                loginId = LOGIN_ID,
                passwordHash = PASSWORD_HASH,
                name = NAME,
                roles = setOf(UserRole.MANAGE_ROOMS),
            )
        ).let {
            assertThat(it).isInstanceOf(InsertUserResult.UserInserted::class.java)
            it as InsertUserResult.UserInserted
        }.userId

        val result = underTest.insertUser(
            InsertUserRecord(
                loginId = LOGIN_ID,
                passwordHash = RandomString.make(),
                name = RandomString.make(),
                roles = setOf(UserRole.MANAGE_ROOMS),
            )
        )

        val users = loadAllUsers()

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(InsertUserResult.LoginIdUniquenessViolation)
            s.assertThat(users).containsExactly(
                UserRecord(
                    userId = firstUserId,
                    userRoles = setOf(UserRole.MANAGE_ROOMS.name),
                    name = NAME,
                    credential = UserCredentialRecord(
                        loginId = LOGIN_ID,
                        passwordHash = PASSWORD_HASH,
                    ),
                )
            )
        }
    }

    @Test
    fun `should attach credentials to anonymous user`() {
        val userId = underTest.createAnonymousUser()

        val result = underTest.createCredentialsForAnonymousUser(
            userId,
            InsertUserRecord(
                loginId = LOGIN_ID,
                passwordHash = PASSWORD_HASH,
                name = NAME,
                roles = setOf(UserRole.MANAGE_USERS),
            )
        )

        val users = loadAllUsers()

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                PromoteAnonymousUserResult.UserCredentialsInserted(userId)
            )
            s.assertThat(users).containsExactly(
                UserRecord(
                    userId = userId,
                    userRoles = setOf(UserRole.MANAGE_USERS.name),
                    name = NAME,
                    credential = UserCredentialRecord(
                        loginId = LOGIN_ID,
                        passwordHash = PASSWORD_HASH,
                    ),
                )
            )
        }
    }

    @Test
    fun `should return UserIsNotAnonymous when user record is already associated with credentials`() {
        val userId = underTest.insertUser(
            InsertUserRecord(
                loginId = LOGIN_ID,
                passwordHash = PASSWORD_HASH,
                name = NAME,
                roles = setOf(UserRole.MANAGE_ROOMS),
            )
        ).let {
            assertThat(it).isInstanceOf(InsertUserResult.UserInserted::class.java)
            it as InsertUserResult.UserInserted
        }.userId

        val result = underTest.createCredentialsForAnonymousUser(
            userId,
            InsertUserRecord(
                loginId = LOGIN_ID,
                passwordHash = RandomString.make(),
                name = RandomString.make(),
                roles = setOf(UserRole.MANAGE_USERS),
            )
        )

        val users = loadAllUsers()

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(PromoteAnonymousUserResult.UserIsNotAnonymous)
            s.assertThat(users).containsExactly(
                UserRecord(
                    userId = userId,
                    userRoles = setOf(UserRole.MANAGE_ROOMS.name),
                    name = NAME,
                    credential = UserCredentialRecord(
                        loginId = LOGIN_ID,
                        passwordHash = PASSWORD_HASH,
                    ),
                )
            )
        }
    }

    @Test
    fun `should return AnonymousUserDoesNotExist when user record does not exist`() {
        val result = underTest.createCredentialsForAnonymousUser(
            "not-a-user",
            InsertUserRecord(
                loginId = LOGIN_ID,
                passwordHash = PASSWORD_HASH,
                name = NAME,
                roles = setOf(UserRole.MANAGE_USERS),
            )
        )

        val users = loadAllUsers()

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(PromoteAnonymousUserResult.AnonymousUserDoesNotExist)
            s.assertThat(users).isEmpty()
        }
    }

    @Test
    fun `should return LoginIdUniquenessViolation when login ID is already used in anonymous user promotion`() {
        val anonymousUserId = underTest.createAnonymousUser()
        val existingUserId = underTest.insertUser(
            InsertUserRecord(
                loginId = LOGIN_ID,
                passwordHash = PASSWORD_HASH,
                name = NAME,
                roles = setOf(UserRole.MANAGE_ROOMS),
            )
        ).let {
            assertThat(it).isInstanceOf(InsertUserResult.UserInserted::class.java)
            it as InsertUserResult.UserInserted
        }.userId

        val result = underTest.createCredentialsForAnonymousUser(
            anonymousUserId,
            InsertUserRecord(
                loginId = LOGIN_ID,
                passwordHash = RandomString.make(),
                name = RandomString.make(),
                roles = setOf(UserRole.MANAGE_USERS),
            )
        )

        val users = loadAllUsers()

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(PromoteAnonymousUserResult.LoginIdUniquenessViolation)
            s.assertThat(users).containsExactlyInAnyOrder(
                UserRecord(
                    userId = anonymousUserId,
                    userRoles = setOf(UserRole.CUSTOMER.name),
                    name = "",
                    credential = null,
                ),
                UserRecord(
                    userId = existingUserId,
                    userRoles = setOf(UserRole.MANAGE_ROOMS.name),
                    name = NAME,
                    credential = UserCredentialRecord(
                        loginId = LOGIN_ID,
                        passwordHash = PASSWORD_HASH,
                    ),
                )
            )
        }
    }

    private fun loadAllUsers(): List<UserRecord> {
        val results = connection.prepareStatement(
            """
                select u.user_id, u.user_roles, u.name, c.login_id, c.password_hash
                from app_user u
                left outer join user_credential c on c.user_id = u.user_id
            """.trimIndent()
        ).executeQuery()

        val records = mutableListOf<UserRecord>()
        while (results.next()) {
            val userId = results.getString("user_id")
            val userRoles = results.getArray("user_roles").array as Array<String>
            val name = results.getString("name")
            val loginId = results.getString("login_id")
            val passwordHash = results.getString("password_hash")

            records.add(
                UserRecord(
                    userId = userId,
                    userRoles = setOf(*userRoles),
                    name = name,
                    credential = if (loginId != null && passwordHash != null) {
                        UserCredentialRecord(
                            loginId = loginId,
                            passwordHash = passwordHash,
                        )
                    } else {
                        null
                    },
                )
            )
        }

        return records
    }
}
