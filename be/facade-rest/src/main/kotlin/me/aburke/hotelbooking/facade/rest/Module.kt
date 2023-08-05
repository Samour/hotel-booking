package me.aburke.hotelbooking.facade.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import me.aburke.hotelbooking.facade.rest.api.admin.v1.user.CreateUserHandler
import me.aburke.hotelbooking.facade.rest.api.auth.v1.session.CreateAnonymousSessionHandler
import me.aburke.hotelbooking.facade.rest.api.auth.v1.session.GetSessionHandler
import me.aburke.hotelbooking.facade.rest.api.auth.v1.session.LogInHandler
import me.aburke.hotelbooking.facade.rest.api.auth.v1.session.SessionRoutes
import me.aburke.hotelbooking.facade.rest.api.auth.v1.user.SignUpHandler
import me.aburke.hotelbooking.facade.rest.interceptors.AuthenticationInterceptor
import me.aburke.hotelbooking.facade.rest.interceptors.ExceptionHandler.registerExceptionHandlers
import org.koin.dsl.module

import me.aburke.hotelbooking.facade.rest.api.admin.v1.user.UserRoutes as AdminUserRoutes
import me.aburke.hotelbooking.facade.rest.api.auth.v1.user.UserRoutes as AuthUserRoutes

val restModule = module {
    single { GetSessionHandler() }
    single { LogInHandler(get()) }
    single { CreateAnonymousSessionHandler(get()) }
    single { SessionRoutes(get(), get(), get()) }

    single { SignUpHandler(get(), get()) }
    single { AuthUserRoutes(get()) }

    single { CreateUserHandler(get()) }
    single { AdminUserRoutes(get()) }

    single { AuthenticationInterceptor(get()) }
    single {
        ApplicationRoutes(
            listOf(
                get<SessionRoutes>(),
                get<AuthUserRoutes>(),
                get<AdminUserRoutes>(),
            )
        )
    }
    single { buildJavalin(get(), get(), ::getProperty) }
}

fun restObjectMapper() = jacksonObjectMapper().apply {
    setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    registerModule(JavaTimeModule())
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    setSerializationInclusion(JsonInclude.Include.NON_NULL)

}

fun buildJavalin(
    authenticationInterceptor: AuthenticationInterceptor,
    applicationRoutes: ApplicationRoutes,
    propertySource: PropertySource,
): Javalin =
    Javalin.create { config ->
        config.jsonMapper(JavalinJackson(restObjectMapper()))
        config.accessManager(authenticationInterceptor)
    }.apply {
        applicationRoutes.addRoutes(RouteRegistry(this, propertySource))
        registerExceptionHandlers(this)
    }
