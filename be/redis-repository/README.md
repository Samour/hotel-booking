# Integration tests

First, start Redis database

```bash
docker compose up redis
```

Then run tests

```bash
./gradlew redis-repository:test
```
