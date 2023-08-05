package me.aburke.hotelbooking

import io.javalin.testtools.JavalinTest.test
import io.mockk.every
import io.mockk.mockk
import me.aburke.hotelbooking.client.AppTestClient
import me.aburke.hotelbooking.client.parseBody
import me.aburke.hotelbooking.data.TestUser
import me.aburke.hotelbooking.facade.rest.api.admin.v1.user.CreateUserRequest
import me.aburke.hotelbooking.facade.rest.api.admin.v1.user.CreateUserResponse
import me.aburke.hotelbooking.facade.rest.api.auth.v1.session.LogInRequest
import me.aburke.hotelbooking.migrations.postgres.executeScript
import me.aburke.hotelbooking.model.user.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.fileProperties
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.Protocol
import java.sql.Connection
import java.time.Clock
import java.time.Instant

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

fun KoinApplication.restTest(case: (AppTestClient) -> Unit) = test(koin.get()) { _, client ->
    case(AppTestClient(client))
}

fun AppTestClient.authenticateAsAdmin() = authenticateAs(TestUser.admin)

fun AppTestClient.createUserWithRoles(vararg roles: UserRole): TestUser {
    val loginId = "test-${RandomString.make()}"
    val password = "test-${RandomString.make()}"
    authenticateAsAdmin()
    val (userId) = createUser(
        CreateUserRequest(
            loginId = loginId,
            password = password,
            name = "test-${RandomString.make()}",
            roles = listOf(*roles),
        )
    ).also { assertThat(it.code).isEqualTo(201) }
        .parseBody<CreateUserResponse>()!!

    return TestUser(
        userId = userId,
        loginId = loginId,
        password = password,
    )
}

fun AppTestClient.authenticateAs(user: TestUser) {
    logIn(
        LogInRequest(
            loginId = user.loginId,
            password = user.password,
        )
    ).also { assertThat(it.code).isEqualTo(201) }
}
