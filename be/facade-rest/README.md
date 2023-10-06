# Unit Tests

```bash
./gradlew facade-rest:test
```

# Contract tests

**Regenerate client based on current open API spec**

```bash
./gradlew test-rest-client:clean test-rest-client:openApiGenerate
```

**Run tests**

```bash
./gradlew facade-rest:contractTest
```
