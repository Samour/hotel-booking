rootProject.name = "hotel-booking"

include(
    "ports-primary",
    "ports-secondary",
    "domain",

    "repository-postgres",
    "repository-redis",
    "facade-rest",
    "app",

    "tool-postgres-migrations",
    "test-rest-client",
)
