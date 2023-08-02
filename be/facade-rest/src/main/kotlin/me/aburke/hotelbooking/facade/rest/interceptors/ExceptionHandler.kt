package me.aburke.hotelbooking.facade.rest.interceptors

import com.fasterxml.jackson.databind.JsonMappingException
import io.javalin.Javalin
import io.javalin.http.NotFoundResponse
import me.aburke.hotelbooking.facade.rest.responses.ProblemResponse
import me.aburke.hotelbooking.facade.rest.responses.notFoundResponse
import me.aburke.hotelbooking.facade.rest.responses.problemJson
import org.slf4j.LoggerFactory

object ExceptionHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun registerExceptionHandlers(app: Javalin) {
        app.exception(NotFoundResponse::class.java) { _, ctx ->
            ctx.problemJson(ctx.notFoundResponse())
        }

        app.exception(JsonMappingException::class.java) { e, context ->
            logger.warn("Invalid request received", e)
            context.problemJson(
                ProblemResponse(
                    title = "Invalid Request",
                    code = "INVALID_REQUEST",
                    status = 400,
                    detail = "Request body is not valid",
                    instance = context.path(),
                )
            )
        }

        app.exception(Exception::class.java) { e, context ->
            logger.error("Unhandled exception occurred", e)
            context.problemJson(
                ProblemResponse(
                    title = "Unexpected Error",
                    code = "UNEXPECTED_ERROR",
                    status = 500,
                    detail = "Unhandled exception occurred",
                    instance = context.path(),
                )
            )
        }
    }
}
