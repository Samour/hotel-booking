package me.aburke.hotelbooking.facade.rest.api.auth.v1.user

import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.HttpStatus
import io.javalin.http.bodyAsClass
import me.aburke.hotelbooking.facade.rest.authentication.AUTH_COOKIE_KEY
import me.aburke.hotelbooking.facade.rest.authentication.setAuthCookie
import me.aburke.hotelbooking.facade.rest.responses.ProblemResponse
import me.aburke.hotelbooking.facade.rest.responses.problemJson
import me.aburke.hotelbooking.model.user.UserSession
import me.aburke.hotelbooking.scenario.user.*
import java.time.Instant

data class SignUpRequest(
    val loginId: String,
    val password: String,
    val name: String,
)

data class SignUpResponse(
    val userId: String,
    val loginId: String,
    val userRoles: List<String>,
    val anonymousUser: Boolean,
    val sessionExpiryTime: Instant,
)

class SignUpHandler(
    private val signUpScenario: SignUpScenario,
    private val getAuthStateScenario: GetAuthStateScenario,
) : Handler {

    override fun handle(ctx: Context) {
        val session = ctx.cookie(AUTH_COOKIE_KEY)?.let {
            getAuthStateScenario.run(GetAuthStateDetails(it)) as? GetAuthStateResult.SessionExists
        }?.session

        val request = ctx.bodyAsClass<SignUpRequest>()

        val result = signUpScenario.run(
            SignUpDetails(
                loginId = request.loginId,
                rawPassword = request.password,
                name = request.name,
                anonymousUser = session?.let {
                    AnonymousSession(
                        sessionId = it.sessionId,
                        userId = it.userId
                    )
                },
            )
        )

        when (result) {
            is SignUpResult.Success -> ctx.sendUserCreatedResponse(result.session, session != null)

            is SignUpResult.UsernameNotAvailable -> ctx.sendUsernameNotAvailable()

            is SignUpResult.UserIsNotAnonymous -> ctx.sendUserIsNotAnonymous()

            is SignUpResult.AnonymousUserDoesNotExist -> ctx.sendUserDoesNotExist()
        }
    }
}

private fun Context.sendUserCreatedResponse(session: UserSession, existingSession: Boolean) {
    status(HttpStatus.CREATED)
    if (!existingSession) {
        setAuthCookie(session)
    }
    json(
        SignUpResponse(
            userId = session.userId,
            loginId = session.loginId!!,
            userRoles = session.userRoles.map { it.name },
            anonymousUser = session.anonymousUser,
            sessionExpiryTime = session.sessionExpiryTime,
        )
    )
}

private fun Context.sendUsernameNotAvailable() = problemJson(
    ProblemResponse(
        title = "Username Conflict",
        code = "CONFLICT",
        status = 409,
        detail = "Username is not available",
        instance = path(),
    )
)

private fun Context.sendUserIsNotAnonymous() = problemJson(
    ProblemResponse(
        title = "User is not anonymous",
        code = "CONFLICT",
        status = 409,
        detail = "User is not anonymous",
        instance = path(),
    )
)

private fun Context.sendUserDoesNotExist() = problemJson(
    ProblemResponse(
        title = "User does not exist",
        code = "BAD_REQUEST",
        status = 400,
        detail = "User does not exist",
        instance = path(),
    )
)
