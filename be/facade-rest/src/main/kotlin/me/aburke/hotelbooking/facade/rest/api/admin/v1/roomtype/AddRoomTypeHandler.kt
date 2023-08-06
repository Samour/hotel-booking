package me.aburke.hotelbooking.facade.rest.api.admin.v1.roomtype

import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.HttpStatus
import io.javalin.http.bodyAsClass
import me.aburke.hotelbooking.scenario.room.AddRoomTypeDetails
import me.aburke.hotelbooking.scenario.room.AddRoomTypeScenario

data class AddRoomTypeRequest(
    val title: String,
    val description: String,
    val imageUrls: List<String> = emptyList(),
    val stockLevel: Int,
)

data class AddRoomTypeResponse(
    val roomTypeId: String,
)

class AddRoomTypeHandler(
    private val addRoomTypeScenario: AddRoomTypeScenario,
) : Handler {

    override fun handle(ctx: Context) {
        val request = ctx.bodyAsClass<AddRoomTypeRequest>()

        val result = addRoomTypeScenario.run(
            AddRoomTypeDetails(
                title = request.title,
                description = request.description,
                imageUrls = request.imageUrls,
                stockLevel = request.stockLevel,
            )
        )

        ctx.status(HttpStatus.CREATED)
        ctx.json(
            AddRoomTypeResponse(
                roomTypeId = result.roomTypeId,
            )
        )
    }
}
