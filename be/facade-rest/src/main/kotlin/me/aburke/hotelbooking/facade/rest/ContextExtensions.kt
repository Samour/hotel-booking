package me.aburke.hotelbooking.facade.rest

import io.javalin.http.Context

fun Context.pathWithParams() = listOfNotNull(
    path(),
    queryParamMap()
        .takeUnless { it.isEmpty() }
        ?.entries
        ?.flatMap { (k, v) -> v.map { k to it } }
        ?.joinToString("&") { (k, v) -> "$k=$v" }
).joinToString("?")
