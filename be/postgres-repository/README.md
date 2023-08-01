# Integration tests

First, start PostgreSQL database using `postgres_test` DB

```bash
DB_NAME=postgres_test docker compose up postgresql
```

Then run tests

```bash
./gradlew mysql-repository:test
```
