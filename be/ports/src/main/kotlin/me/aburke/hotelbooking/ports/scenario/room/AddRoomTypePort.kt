package me.aburke.hotelbooking.ports.scenario.room

import me.aburke.hotelbooking.ports.scenario.Scenario

data class AddRoomTypeDetails(
    val title: String,
    val description: String,
    val imageUrls: List<String>,
    val stockLevel: Int,
) : Scenario.Details

data class AddRoomTypeResult(
    val roomTypeId: String,
) : Scenario.Result

interface AddRoomTypePort : Scenario<AddRoomTypeDetails, AddRoomTypeResult>
