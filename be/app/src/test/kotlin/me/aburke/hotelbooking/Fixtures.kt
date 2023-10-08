package me.aburke.hotelbooking

import io.javalin.testtools.JavalinTest.test
import io.mockk.every
import io.mockk.mockk
import me.aburke.hotelbooking.client.SimpleCookieJar
import me.aburke.hotelbooking.data.TestUser
import me.aburke.hotelbooking.migrations.postgres.executeScript
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.rest.client.api.AdminUnstableApi
import me.aburke.hotelbooking.rest.client.api.AuthUnstableApi
import me.aburke.hotelbooking.rest.client.invoker.ApiClient
import me.aburke.hotelbooking.rest.client.model.CreateUserRequest
import me.aburke.hotelbooking.rest.client.model.LogInRequest
import okhttp3.OkHttpClient
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.fileProperties
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.Protocol
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.sql.DataSource
import me.aburke.hotelbooking.rest.client.model.UserRole as UserRoleDto

class TestContext(
    val app: KoinApplication,
) {

    var time = Instant.now().minusSeconds(10_000)
        private set

    fun incrementClock() {
        time = time.plusMillis((10L..350L).random())
    }

    fun stubClock() {
        val clock = app.koin.get<Clock>()
        every { clock.instant() } returns time
    }
}

// TODO See what we can do here about re-using app context too
fun createApp(
    populateTestData: Boolean = true,
    useEndpointsProperties: Boolean = true,
): TestContext {
    val testModule = module {
        single<Clock> { mockk() }
    }

    val app = koinApplication {
        fileProperties()
        fileProperties("/features.properties")
        if (useEndpointsProperties) {
            fileProperties("/endpoints.properties")
        }
        modules(testModule, *appModules.toTypedArray())
    }
    app.koin.get<DataSource>().connection.use {
        it.executeScript("drop_db.sql")
        it.executeScript("bootstrap_db.sql")
        if (populateTestData) {
            it.executeScript("test_data.sql")
        }
    }
    app.koin.get<JedisPooled>().sendCommand(Protocol.Command.FLUSHDB)

    return TestContext(app).apply { stubClock() }
}

// Set this to true if you want the OkHttp client to have high timeouts eg. for setting breakpoints locally
// Make sure this is false in mainline code so that CI does not use this slow client during test steps
private const val USE_SLOW_CLIENT = false

fun KoinApplication.restTest(case: (ApiClient, SimpleCookieJar) -> Unit) = test(koin.get()) { javalin, _ ->
    val cookieJar = SimpleCookieJar()
    case(
        ApiClient(
            OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .apply {
                    if (USE_SLOW_CLIENT) {
                        connectTimeout(Duration.parse("PT10M"))
                        readTimeout(Duration.parse("PT10M"))
                        writeTimeout(Duration.parse("PT10M"))
                    }
                }.build(),
        ).also {
            it.basePath = "http://localhost:${javalin.port()}"
        },
        cookieJar,
    )
}

fun ApiClient.authenticateAsAdmin() = authenticateAs(TestUser.admin)

fun ApiClient.createUserWithRoles(vararg roles: UserRole): TestUser {
    val loginId = "test-${RandomString.make()}"
    val password = "test-${RandomString.make()}"
    authenticateAsAdmin()
    val userId = AdminUnstableApi(this).createUser(
        CreateUserRequest().also { r ->
            r.loginId = loginId
            r.password = password
            r.name = "test-${RandomString.make()}"
            r.roles = roles.map { UserRoleDto.fromValue(it.name) }
        },
    ).userId

    return TestUser(
        userId = userId,
        loginId = loginId,
        password = password,
    )
}

fun ApiClient.authenticateAs(user: TestUser) {
    AuthUnstableApi(this).logIn(
        LogInRequest().apply {
            loginId = user.loginId
            password = user.password
        },
    )
}

fun ApiClient.authenticateWith(vararg roles: UserRole) {
    authenticateAs(createUserWithRoles(*roles))
}
