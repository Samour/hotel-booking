rootProject.name = "hotel-booking"

include(
    "ports",
    "core",

    "postgres-repository",
    "redis-repository",
    "facade-rest",
    "app",

    "postgres-migrations",
)
