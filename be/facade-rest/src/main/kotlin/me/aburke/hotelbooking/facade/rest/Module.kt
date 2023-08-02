package me.aburke.hotelbooking.facade.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import me.aburke.hotelbooking.facade.rest.interceptors.ExceptionHandler.registerExceptionHandlers
import org.koin.dsl.module

val restModule = module {
    single {
        ApplicationRoutes(
            listOf()
        )
    }
    single { buildJavalin(get()) }
}

fun restObjectMapper() = jacksonObjectMapper().apply {
    setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    registerModule(JavaTimeModule())
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    setSerializationInclusion(JsonInclude.Include.NON_NULL)

}

fun buildJavalin(applicationRoutes: ApplicationRoutes): Javalin =
    Javalin.create { config ->
        config.jsonMapper(JavalinJackson(restObjectMapper()))
    }.apply {
        applicationRoutes.addRoutes(this)
        registerExceptionHandlers(this)
    }
