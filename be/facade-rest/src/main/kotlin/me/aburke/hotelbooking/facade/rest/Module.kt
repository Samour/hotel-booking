package me.aburke.hotelbooking.facade.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import me.aburke.hotelbooking.facade.rest.api.admin.roomtype.AddRoomTypeHandler
import me.aburke.hotelbooking.facade.rest.api.admin.user.CreateUserHandler
import me.aburke.hotelbooking.facade.rest.api.auth.session.CreateAnonymousSessionHandler
import me.aburke.hotelbooking.facade.rest.api.auth.session.GetSessionHandler
import me.aburke.hotelbooking.facade.rest.api.auth.session.LogInHandler
import me.aburke.hotelbooking.facade.rest.api.auth.session.SessionRoutes
import me.aburke.hotelbooking.facade.rest.api.auth.user.SignUpHandler
import me.aburke.hotelbooking.facade.rest.api.customer.roomtype.FetchRoomsAvailabilityHandler
import me.aburke.hotelbooking.facade.rest.interceptors.AuthenticationInterceptor
import me.aburke.hotelbooking.facade.rest.interceptors.ExceptionHandler.registerExceptionHandlers
import org.koin.dsl.module
import org.koin.dsl.onClose
import me.aburke.hotelbooking.facade.rest.api.admin.roomtype.RoomRoutes as AdminRoomRoutes
import me.aburke.hotelbooking.facade.rest.api.admin.user.UserRoutes as AdminUserRoutes
import me.aburke.hotelbooking.facade.rest.api.auth.user.UserRoutes as AuthUserRoutes
import me.aburke.hotelbooking.facade.rest.api.customer.roomtype.RoomRoutes as CustomerRoomRoutes

val restModule = module {
    single { GetSessionHandler() }
    single { LogInHandler(get()) }
    single { CreateAnonymousSessionHandler(get()) }
    single { SessionRoutes(get(), get(), get()) }

    single { SignUpHandler(get(), get()) }
    single { AuthUserRoutes(get()) }

    single { CreateUserHandler(get()) }
    single { AdminUserRoutes(get()) }

    single { AddRoomTypeHandler(get()) }
    single { AdminRoomRoutes(get()) }

    single { FetchRoomsAvailabilityHandler(get()) }
    single { CustomerRoomRoutes(get()) }

    single { AuthenticationInterceptor(get()) }
    single {
        ApplicationRoutes(
            listOf(
                get<SessionRoutes>(),
                get<AuthUserRoutes>(),
                get<AdminUserRoutes>(),
                get<AdminRoomRoutes>(),
                get<CustomerRoomRoutes>(),
            ),
        )
    }
    single {
        buildJavalin(get(), get(), ::getProperty)
    } onClose { it?.stop() }
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
