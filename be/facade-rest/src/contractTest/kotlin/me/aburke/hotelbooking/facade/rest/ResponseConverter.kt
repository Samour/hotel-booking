package me.aburke.hotelbooking.facade.rest

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue

val objectMapper = restObjectMapper()

inline fun <reified T> String.parseResponse(): T? =
    try {
        objectMapper.readValue<T>(this)
    } catch (_: JsonProcessingException) {
        null
    }
