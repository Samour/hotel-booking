package me.aburke.hotelbooking

import io.javalin.testtools.JavalinTest.test
import me.aburke.hotelbooking.client.SimpleCookieJar
import me.aburke.hotelbooking.data.TestUser
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.rest.client.api.AdminUnstableApi
import me.aburke.hotelbooking.rest.client.api.AuthUnstableApi
import me.aburke.hotelbooking.rest.client.invoker.ApiClient
import me.aburke.hotelbooking.rest.client.model.CreateUserRequest
import me.aburke.hotelbooking.rest.client.model.LogInRequest
import okhttp3.OkHttpClient
import org.koin.core.KoinApplication
import java.time.Duration
import me.aburke.hotelbooking.rest.client.model.UserRole as UserRoleDto

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
