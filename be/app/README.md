# Running

First, start databases

```bash
docker compose up
```

If the database schema has not been defined, it can be created with the command

```bash
./gradlew postgres-migrations:bootstrapDb
```

Then start app

```bash
./gradlew app:run
```

# Integration tests

First, start databases using `postgres_test` DB

```bash
DB_NAME=postgres_test docker compose up
```

Then run tests

```bash
./gradlew app:test
```
