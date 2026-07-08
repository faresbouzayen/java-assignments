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

IntelliJ IDEA can import JaCoCo `.exec` files from `target/jacoco.exec`.

### Skip coverage check (if needed)

```sh
./mvnw verify -Djacoco.skip=true
```

## Coverage Analysis

### Classes under test

| Package | Class | Covered | Notes |
|---------|-------|---------|-------|
| `location` | `LocationGateway` | ✅ | 5 unit tests (positive, negative, edge cases) |
| `warehouses.domain.usecases` | `CreateWarehouseUseCase` | ✅ | 8 unit tests covering all validation rules |
| `warehouses.domain.usecases` | `ReplaceWarehouseUseCase` | ✅ | 7 unit tests covering all validation rules |
| `warehouses.domain.usecases` | `ArchiveWarehouseUseCase` | ✅ | 2 unit tests covering archive logic |
| `warehouses.adapters.database` | `WarehouseRepository` | ⚠️ | Integration tested via endpoint tests |
| `warehouses.adapters.restapi` | `WarehouseResourceImpl` | ⚠️ | Integration tested via `WarehouseEndpointIT` |
| `fulfillment` | `FulfillmentResource` | ⚠️ | Integration tested via `FulfillmentEndpointIT` |
| `stores` | `StoreResource` | ⚠️ | Integration tested via product/store tests |
| `products` | `ProductResource` | ⚠️ | Integration tested via `ProductEndpointTest` |

### Gaps & Improvements

- **`WarehouseRepository`**: Direct unit tests would improve coverage. Currently tested indirectly through `WarehouseEndpointIT`.
- **`StoreResource`**: Lacks dedicated unit tests. Covered indirectly through existing tests.
- **`ProductResource`**: Lacks dedicated unit tests (only basic CRUD test exists).

All domain use cases (business logic) are fully unit-tested with Mockito, achieving the 80% target.
