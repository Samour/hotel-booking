package me.aburke.hotelbooking.facade.rest.responses

import io.javalin.http.Context

data class ProblemResponse(
    val title: String,
    val code: String,
    val status: Int,
    val detail: String,
    val instance: String,
    val extendedDetails: List<ProblemAdditionalDetail> = emptyList(),
)

data class ProblemAdditionalDetail(
    val code: String,
    val detail: String,
)

fun Context.problemJson(response: ProblemResponse) {
    status(response.status)
    json(response)
    contentType("application/problem+json")
}

fun Context.notFoundResponse() = ProblemResponse(
    title = "Resource Not Found",
    code = "RESOURCE_NOT_FOUND",
    status = 404,
    detail = "Resource does not exist",
    instance = path(),
)

fun Context.unauthorizedResponse() = ProblemResponse(
    title = "Not Authorized",
    code = "UNAUTHORIZED",
    status = 401,
    detail = "Credentials not provided",
    instance = path(),
)

fun Context.forbiddenResponse() = ProblemResponse(
    title = "Forbidden",
    code = "FORBIDDEN",
    status = 403,
    detail = "Insufficient permissions to access resource",
    instance = path(),
)
