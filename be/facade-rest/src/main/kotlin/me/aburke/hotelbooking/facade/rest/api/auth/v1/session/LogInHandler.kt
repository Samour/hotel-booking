package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import io.javalin.http.*
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import me.aburke.hotelbooking.facade.rest.responses.ProblemResponse
import me.aburke.hotelbooking.facade.rest.responses.problemJson
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.scenario.user.LogInCredentials
import me.aburke.hotelbooking.scenario.user.LogInResult
import me.aburke.hotelbooking.scenario.user.LogInScenario
import java.time.Instant

data class LogInRequest(
    val loginId: String,
    val password: String,
)

data class LogInResponse(
    val userId: String,
    val loginId: String?,
    val userRoles: List<String>,
    val anonymousUser: Boolean,
    val sessionExpiryTime: Instant,
)

class LogInHandler(
    private val logInScenario: LogInScenario,
) : Handler {

    override fun handle(ctx: Context) {
        val request = ctx.bodyAsClass<LogInRequest>()

        val logInResult = logInScenario.run(
            LogInCredentials(
                loginId = request.loginId,
                password = request.password,
            )
        )

        when (logInResult) {
            is LogInResult.UserSessionCreated -> ctx.sendSessionWithCookie(logInResult.session)
            is LogInResult.InvalidCredentials -> ctx.sendInvalidCredentials()
        }
    }
}

private fun Context.sendSessionWithCookie(session: UserSession) {
    status(HttpStatus.CREATED)
    cookie(
        Cookie(
            name = AUTH_COOKIE_KEY,
            value = session.sessionId,
            sameSite = SameSite.STRICT,
            isHttpOnly = true,
        )
    )
    json(
        LogInResponse(
            userId = session.userId,
            loginId = session.loginId,
            userRoles = session.userRoles.map { it.name },
            anonymousUser = session.anonymousUser,
            sessionExpiryTime = session.sessionExpiryTime,
        )
    )
}

private fun Context.sendInvalidCredentials() = problemJson(
    ProblemResponse(
        title = "Invalid Credentials",
        code = "UNAUTHORIZED",
        status = 401,
        detail = "Supplied credentials are not valid",
        instance = path(),
    )
)
