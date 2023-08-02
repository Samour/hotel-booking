rootProject.name = "hotel-booking"

include(
    "core",
    "postgres-repository",
    "redis-repository",
    "facade-rest",

    "postgres-migrations",
)
