rootProject.name = "hotel-booking"

include(
    "ports",
    "domain",

    "repository-postgres",
    "repository-redis",
    "facade-rest",
    "app",

    "tool-postgres-migrations",
    "test-rest-client",
)
