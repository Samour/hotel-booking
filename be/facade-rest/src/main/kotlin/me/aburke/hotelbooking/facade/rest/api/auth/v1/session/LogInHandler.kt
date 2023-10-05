package me.aburke.hotelbooking.facade.rest.api.auth.v1.session

import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.HttpStatus
import io.javalin.http.bodyAsClass
import me.aburke.hotelbooking.facade.rest.authentication.setAuthCookie
import me.aburke.hotelbooking.facade.rest.responses.ProblemResponse
import me.aburke.hotelbooking.facade.rest.responses.SessionResponse
import me.aburke.hotelbooking.facade.rest.responses.problemJson
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.ports.scenario.user.LogInCredentials
import me.aburke.hotelbooking.ports.scenario.user.LogInPort
import me.aburke.hotelbooking.ports.scenario.user.LogInResult

data class LogInRequest(
    val loginId: String,
    val password: String,
)

class LogInHandler(
    private val logInPort: LogInPort,
) : Handler {

    override fun handle(ctx: Context) {
        val request = ctx.bodyAsClass<LogInRequest>()

        val logInResult = logInPort.run(
            LogInCredentials(
                loginId = request.loginId,
                password = request.password,
            ),
        )

        when (logInResult) {
            is LogInResult.UserSessionCreated -> ctx.sendSessionWithCookie(logInResult.session)
            is LogInResult.InvalidCredentials -> ctx.sendInvalidCredentials()
        }
    }
}

private fun Context.sendSessionWithCookie(session: UserSession) {
    status(HttpStatus.CREATED)
    setAuthCookie(session)
    json(
        SessionResponse(
            userId = session.userId,
            loginId = session.loginId,
            userRoles = session.userRoles.map { it.name },
            anonymousUser = session.anonymousUser,
            sessionExpiryTime = session.sessionExpiryTime,
        ),
    )
}

private fun Context.sendInvalidCredentials() = problemJson(
    ProblemResponse(
        title = "Invalid Credentials",
        code = "UNAUTHORIZED",
        status = 401,
        detail = "Supplied credentials are not valid",
        instance = path(),
    ),
)
