package me.aburke.hotelbooking.repository.postgres.user

import me.aburke.hotelbooking.ports.repository.InsertUserRecord
import me.aburke.hotelbooking.ports.repository.InsertUserResult
import me.aburke.hotelbooking.ports.repository.NonAnonymousUserRecord
import me.aburke.hotelbooking.ports.repository.PromoteAnonymousUserResult
import me.aburke.hotelbooking.ports.repository.UserCredentialRecord
import me.aburke.hotelbooking.ports.repository.UserRecord
import me.aburke.hotelbooking.ports.repository.UserRepository
import me.aburke.hotelbooking.repository.postgres.RandomString
import me.aburke.hotelbooking.repository.postgres.createApp
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.KoinApplication
import java.sql.Connection
import javax.sql.DataSource

private const val LOGIN_ID = "login-id"
private const val PASSWORD_HASH = "password-hash"
private const val NAME = "name"

class PostgresUserRepositoryTest {

    private lateinit var app: KoinApplication
    private lateinit var connection: Connection

    private lateinit var underTest: UserRepository

    @BeforeEach
    fun init() {
        app = createApp()
        connection = app.koin.get<DataSource>().connection
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
                userRoles = setOf("CUSTOMER"),
                name = "",
                credential = null,
            ),
        )
    }

    @Test
    fun `should create user record with credentials`() {
        val result = underTest.insertUser(
            InsertUserRecord(
                loginId = LOGIN_ID,
                passwordHash = PASSWORD_HASH,
                name = NAME,
                roles = setOf("MANAGE_ROOMS"),
            ),
        )

        val userId = (result as? InsertUserResult.UserInserted)?.userId
        val users = loadAllUsers()

        assertSoftly { s ->
            s.assertThat(result).isInstanceOf(InsertUserResult.UserInserted::class.java)
            s.assertThat(users).containsExactly(
                UserRecord(
                    userId = userId ?: "",
                    userRoles = setOf("MANAGE_ROOMS"),
                    name = NAME,
                    credential = UserCredentialRecord(
                        loginId = LOGIN_ID,
                        passwordHash = PASSWORD_HASH,
                    ),
                ),
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
                roles = setOf("MANAGE_ROOMS"),
            ),
        ).let {
            assertThat(it).isInstanceOf(InsertUserResult.UserInserted::class.java)
            it as InsertUserResult.UserInserted
        }.userId

        val result = underTest.insertUser(
            InsertUserRecord(
                loginId = LOGIN_ID,
                passwordHash = RandomString.make(),
                name = RandomString.make(),
                roles = setOf("MANAGE_ROOMS"),
            ),
        )

        val users = loadAllUsers()

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(InsertUserResult.LoginIdUniquenessViolation)
            s.assertThat(users).containsExactly(
                UserRecord(
                    userId = firstUserId,
                    userRoles = setOf("MANAGE_ROOMS"),
                    name = NAME,
                    credential = UserCredentialRecord(
                        loginId = LOGIN_ID,
                        passwordHash = PASSWORD_HASH,
                    ),
                ),
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
                roles = setOf("MANAGE_USERS"),
            ),
        )

        val users = loadAllUsers()

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(
                PromoteAnonymousUserResult.UserCredentialsInserted(userId),
            )
            s.assertThat(users).containsExactly(
                UserRecord(
                    userId = userId,
                    userRoles = setOf("MANAGE_USERS"),
                    name = NAME,
                    credential = UserCredentialRecord(
                        loginId = LOGIN_ID,
                        passwordHash = PASSWORD_HASH,
                    ),
                ),
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
                roles = setOf("MANAGE_ROOMS"),
            ),
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
                roles = setOf("MANAGE_USERS"),
            ),
        )

        val users = loadAllUsers()

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(PromoteAnonymousUserResult.UserIsNotAnonymous)
            s.assertThat(users).containsExactly(
                UserRecord(
                    userId = userId,
                    userRoles = setOf("MANAGE_ROOMS"),
                    name = NAME,
                    credential = UserCredentialRecord(
                        loginId = LOGIN_ID,
                        passwordHash = PASSWORD_HASH,
                    ),
                ),
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
                roles = setOf("MANAGE_USERS"),
            ),
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
                roles = setOf("MANAGE_ROOMS"),
            ),
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
                roles = setOf("MANAGE_USERS"),
            ),
        )

        val users = loadAllUsers()

        assertSoftly { s ->
            s.assertThat(result).isEqualTo(PromoteAnonymousUserResult.LoginIdUniquenessViolation)
            s.assertThat(users).containsExactlyInAnyOrder(
                UserRecord(
                    userId = anonymousUserId,
                    userRoles = setOf("CUSTOMER"),
                    name = "",
                    credential = null,
                ),
                UserRecord(
                    userId = existingUserId,
                    userRoles = setOf("MANAGE_ROOMS"),
                    name = NAME,
                    credential = UserCredentialRecord(
                        loginId = LOGIN_ID,
                        passwordHash = PASSWORD_HASH,
                    ),
                ),
            )
        }
    }

    @Test
    fun `should return user record with matching login ID`() {
        val userId = (
            underTest.insertUser(
                InsertUserRecord(
                    loginId = LOGIN_ID,
                    passwordHash = PASSWORD_HASH,
                    name = NAME,
                    roles = setOf("CUSTOMER"),
                ),
            ) as InsertUserResult.UserInserted
            ).userId

        val result = underTest.findUserByLoginId(LOGIN_ID)

        assertThat(result).isEqualTo(
            NonAnonymousUserRecord(
                userId = userId,
                userRoles = setOf("CUSTOMER"),
                name = NAME,
                credential = UserCredentialRecord(
                    loginId = LOGIN_ID,
                    passwordHash = PASSWORD_HASH,
                ),
            ),
        )
    }

    @Test
    fun `should return null when no records match login ID`() {
        val result = underTest.findUserByLoginId(LOGIN_ID)

        assertThat(result).isNull()
    }

    private fun loadAllUsers(): List<UserRecord> {
        val results = connection.prepareStatement(
            """
                select u.user_id, u.user_roles, u.name, c.login_id, c.password_hash
                from app_user u
                left outer join user_credential c on c.user_id = u.user_id
            """.trimIndent(),
        ).executeQuery()

        val records = mutableListOf<UserRecord>()
        while (results.next()) {
            val userId = results.getString("user_id")
            val userRoles = results.getArray("user_roles").toUserRoles()
            val name = results.getString("name")
            val loginId = results.getString("login_id")
            val passwordHash = results.getString("password_hash")

            records.add(
                UserRecord(
                    userId = userId,
                    userRoles = userRoles,
                    name = name,
                    credential = if (loginId != null && passwordHash != null) {
                        UserCredentialRecord(
                            loginId = loginId,
                            passwordHash = passwordHash,
                        )
                    } else {
                        null
                    },
                ),
            )
        }

        return records
    }
}
