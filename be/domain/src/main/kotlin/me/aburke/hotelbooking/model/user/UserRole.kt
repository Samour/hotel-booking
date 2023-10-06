package me.aburke.hotelbooking.model.user

fun Set<UserRole>.toNameSet() = asSequence().map { it.name }.toSet()

fun Set<String>.toUserRoles() = asSequence().map { UserRole.valueOf(it) }.toSet()
