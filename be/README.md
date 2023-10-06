# Hotel Booking

## System architecture

![app-architecture](./docs/app-architecture.png)

## Module structure

Code is structured using a
[ports and adapters](https://codesoapbox.dev/ports-adapters-aka-hexagonal-architecture-explained/) pattern

Each module has its own README with more details & test/running instructions

### Module naming scheme

- `facade-` prefix indicates a **driving adaptor** which provides access to invoke domain logic
- `repository-` or `receiver-` prefix indicates a **driven adaptor** which provides the domain logic with access to
external resources
- `tool-` prefix indicates a module which contains scripts or a program to perform some utility tasks. These modules
are **not** built in to the application binary. They may be imported into test source sets to assist with test setup or
execution.
- `test-` prefix indicates a module which provides some functionality specifically for use in test source sets. They are
**not** built in to the application binary.

The special module names `domain`, `ports` and `app` are explained below

### Application modules

- `domain` contains domain logic for the application
  - `test` contains unit tests of `domain` classes
  - `integTest` contains behavioural tests with all components in `domain` assembled, running against stubs for
downstream ports
- `ports` contains driving ports to invoke `domain` logic and driven ports for `domain` to invoke downstream adaptors
- `facade-rest` contains HTTP REST driving adaptor which exposes the application functionality. It also contains an
Open API spec for the REST interface.
  - `test` contains HTTP integration tests with driving ports stubbed. HTTP requests & responses are defined at a
low-level to show the message format & prove the API behaviour.
  - `contractTest` repeats the tests defined in `facade-test:test`, but makes calls using a client generated from the
Open API spec. This proves that the API implementation matches the Open API contract.
- `repository-postgres` contains the driven adaptor for PostgreSQL DB interactions
  - `test` contains DB integration tests run by invoking the adaptor interface. These tests depend on a PostgreSQL DB
running in Docker.
- `repository-redis` contains the driven adaptor for Redis DB interactions
  - `test` contains DB integration tests run by invoking the adaptor interface. These tests depend on a Redis DB
running in Docker.
- `app` integrates the domain logic & all of the adaptors. It is the entry point for the runnable application
  - `test` contains component tests against the entire assembled application. It triggers behaviour by invoking HTTP
endpoints, using a client generated from the Open API specification. It depends on PostgreSQL & Redis DBs running in
Docker.

**Utility modules**

- `tool-postgres-migrations` contains SQL scripts to populate the DB & gradle tasks to help with executing the script/s
against a DB instance
- `test-rest-client` provides a Java HTTP client interfaces generated from the REST Open API contract

## Linting

Linting performed by ktlint. Rule configs specified in `.editorconfig`

**Check lint**

```sh
./gradlew ktlintCheck
```

**Auto-apply lint fixes**

```sh
./gradlew ktlintFormat
```
