# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I would refactor towards a single, consistent pattern. Currently the codebase mixes three approaches: Active Record (Store using PanacheEntity), Repository pattern (Product using PanacheRepository), and Hexagonal/Ports-and-Adapters (Warehouse with WarehouseStore port and WarehouseRepository adapter). I would standardize on the Repository pattern (or Ports-and-Adapters) since it decouples business logic from the persistence framework, makes unit testing easier without a database, and follows the Single Responsibility Principle. The Active Record approach in Store mixes persistence concerns into the domain model, which is harder to test and maintain as the application grows.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Code generation from OpenAPI ensures the API contract is the single source of truth, reduces boilerplate, and keeps server and client in sync. The downside is less flexibility and potential difficulty when the generator doesn't produce idiomatic code. Hand-coded endpoints offer full control and are simpler for small CRUD operations, but risk divergence between the spec and implementation over time. My preference is to use OpenAPI generation for all endpoints once the API is stable, as it enforces contract-first development, simplifies documentation, and makes it easier to add new clients or evolve the API with confidence.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would prioritize in this order: (1) Unit tests for domain use cases (CreateWarehouseUseCase, ReplaceWarehouseUseCase) with mocked dependencies - they cover critical business rules and run fast. (2) Integration tests for the REST endpoints (like the existing WarehouseEndpointIT and ProductEndpointTest) to verify the full stack works. (3) Repository-level tests for custom queries. To keep coverage effective, I'd run tests on every commit via CI, enforce a minimum coverage threshold, and use mutation testing periodically. The key is to focus on business logic and public API contracts rather than aiming for 100% line coverage on infrastructure code.
```