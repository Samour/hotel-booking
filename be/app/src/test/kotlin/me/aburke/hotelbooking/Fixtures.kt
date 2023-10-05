package me.aburke.hotelbooking

import io.javalin.testtools.JavalinTest.test
import io.mockk.every
import io.mockk.mockk
import me.aburke.hotelbooking.client.SimpleCookieJar
import me.aburke.hotelbooking.data.TestUser
import me.aburke.hotelbooking.migrations.postgres.executeScript
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.rest.client.api.AdminApi
import me.aburke.hotelbooking.rest.client.api.AuthApi
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
import java.sql.Connection
import java.time.Clock
import java.time.Instant
import me.aburke.hotelbooking.rest.client.model.UserRole as UserRoleDto

fun createApp(populateTestData: Boolean = true): Pair<KoinApplication, Instant> {
    val instant = Instant.now()
    val clock = mockk<Clock>()
    every {
        clock.instant()
    } returns instant

    val testModule = module {
        single<Clock> { clock }
    }

    val app = koinApplication {
        fileProperties()
        fileProperties("/features.properties")
        modules(testModule, *appModules.toTypedArray())
    }
    app.koin.get<Connection>().apply {
        executeScript("drop_db.sql")
        executeScript("bootstrap_db.sql")
        if (populateTestData) {
            executeScript("test_data.sql")
        }
    }
    app.koin.get<JedisPooled>().sendCommand(Protocol.Command.FLUSHDB)

    return app to instant
}

fun KoinApplication.restTest(case: (ApiClient, SimpleCookieJar) -> Unit) = test(koin.get()) { javalin, _ ->
    val cookieJar = SimpleCookieJar()
    case(
        ApiClient(
            OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build(),
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
    val userId = AdminApi(this).createUser(
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
    AuthApi(this).logIn(
        LogInRequest().apply {
            loginId = user.loginId
            password = user.password
        },
    )
}

fun ApiClient.authenticateWith(vararg roles: UserRole) {
    authenticateAs(createUserWithRoles(*roles))
}
