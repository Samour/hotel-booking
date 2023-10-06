package me.aburke.hotelbooking.facade.rest.api.admin.user

import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.HttpStatus
import io.javalin.http.bodyAsClass
import me.aburke.hotelbooking.facade.rest.responses.ProblemResponse
import me.aburke.hotelbooking.facade.rest.responses.problemJson
import me.aburke.hotelbooking.model.user.UserRole
import me.aburke.hotelbooking.ports.scenario.user.CreateUserDetails
import me.aburke.hotelbooking.ports.scenario.user.CreateUserPort
import me.aburke.hotelbooking.ports.scenario.user.CreateUserResult

data class CreateUserRequest(
    val loginId: String,
    val password: String,
    val name: String,
    val roles: List<UserRole>,
)

data class CreateUserResponse(
    val userId: String,
)

class CreateUserHandler(
    private val createUserPort: CreateUserPort,
) : Handler {

    override fun handle(ctx: Context) {
        val request = ctx.bodyAsClass<CreateUserRequest>()

        val result = createUserPort.run(
            CreateUserDetails(
                loginId = request.loginId,
                rawPassword = request.password,
                name = request.name,
                userRoles = request.roles.toSet(),
            ),
        )

        when (result) {
            is CreateUserResult.Success -> ctx.sendUserCreated(result.userId)

            is CreateUserResult.UsernameNotAvailable -> ctx.sendUsernameNotAvailable()
        }
    }
}

private fun Context.sendUserCreated(userId: String) {
    status(HttpStatus.CREATED)
    json(
        CreateUserResponse(
            userId = userId,
        ),
    )
}

private fun Context.sendUsernameNotAvailable() = problemJson(
    ProblemResponse(
        title = "Username Conflict",
        code = "CONFLICT",
        status = 409,
        detail = "Username is not available",
        instance = path(),
    ),
)
