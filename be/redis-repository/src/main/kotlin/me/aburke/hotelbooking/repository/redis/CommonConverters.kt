package me.aburke.hotelbooking.repository.redis

fun Map<String, String?>.purgeNullValues(): Map<String, String> =
    entries.mapNotNull { (k, v) -> v?.let { k to it } }
        .toMap()
