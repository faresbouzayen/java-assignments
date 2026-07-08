# Code Coverage Setup

## Overview

This project uses [JaCoCo](https://www.jacoco.org/) (Java Code Coverage) integrated via the `quarkus-jacoco` extension and the `jacoco-maven-plugin` to measure and enforce code coverage.

## Configuration

### Components

1. **`quarkus-jacoco`** (test dependency) — Automatically instruments classes during Quarkus test runs (`@QuarkusTest`, `@QuarkusIntegrationTest`).

2. **`jacoco-maven-plugin`** (build plugin) — Prepares the JaCoCo agent and generates coverage reports. Also enforces minimum coverage thresholds during the `verify` phase.

### Coverage Targets

| Metric | Target |
|--------|--------|
| Line coverage | ≥ 80% |
| Branch coverage | ≥ 80% |

### Exclusions

- `com.warehouse.api.*` — OpenAPI generated code (not under our control)

## Execution

### Generate coverage report

```sh
./mvnw clean verify
```

The HTML report is generated at:
```
target/site/jacoco/index.html
```

### View coverage in IDE

IntelliJ IDEA can import JaCoCo `.exec` files from `target/jacoco.exec`. In IntelliJ, run tests with JaCoCo and open `target/site/jacoco/index.html` in a browser.

### Skip coverage check (if needed)

```sh
./mvnw verify -Djacoco.skip=true
```

## Coverage Analysis

### Test inventory

| Test class | Type | Tests | Scope |
|------------|------|-------|-------|
| `LocationGatewayTest` | Unit (JUnit 5) | 5 | Positive + negative + edge cases |
| `CreateWarehouseUseCaseTest` | Unit (Mockito) | 9 | All validation rules (BUC, location, capacity, stock) |
| `ReplaceWarehouseUseCaseTest` | Unit (Mockito) | 7 | All replace validations |
| `ArchiveWarehouseUseCaseTest` | Unit (Mockito) | 2 | Archive + re-archive |
| `StoreEndpointIT` | Integration (QuarkusTest) | 8 | CRUD + validation + delete |
| `ProductEndpointTest` | Integration (QuarkusTest) | 1 | List + delete |
| `WarehouseEndpointIT` | Integration (QuarkusIntegrationTest) | 4 | List + archive + 404 |
| `FulfillmentEndpointIT` | Integration (QuarkusTest) | 7 | CRUD + validation + 404 |
| **Total** | | **43** | |

### Coverage by layer

| Layer | Approach | Status |
|-------|----------|--------|
| **Domain use cases** | Mockito unit tests | ✅ All business rules covered |
| **REST endpoints** | Quarkus integration tests | ✅ All endpoints covered |
| **Persistence** | Integration tests | ⚠️ Tested end-to-end, not in isolation |
| **Gateway/Location** | Unit tests | ✅ All paths covered |

### Gaps & Improvements

- **`WarehouseRepository`**: Could add direct unit tests. Currently exercised through `WarehouseEndpointIT`.
- **`ProductResource`**: Only has basic CRUD test. Could add edge-case tests (404, validation).
- **`LegacyStoreManagerGateway`**: Not directly tested (integration-only concern).

All domain use cases (business logic) are fully unit-tested with Mockito. Integration tests cover REST endpoints end-to-end. The 80% target is achievable given the comprehensive test coverage across all layers.

## Environment Note

To run the build successfully, ensure:
- At least 4 GB of free disk space (for Maven dependencies, build artifacts, and JVM page file)
- Docker or PostgreSQL running (for Quarkus test/dev mode)
- JDK 17+ configured via `JAVA_HOME`
