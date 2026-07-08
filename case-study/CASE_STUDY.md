# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**
Key challenges: attributing shared costs (transportation, overhead) to specific warehouses/stores, data granularity, and legacy system silos. Important to define clear cost drivers and allocation rules upfront.

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**
Strategies: route optimization, inventory pooling, warehouse consolidation, and automation. Prioritize by ROI impact (e.g., high-transport routes first). Implement incrementally with measurable KPIs.

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**
Benefits: single source of truth for costs, real-time visibility, automated reconciliation, and better compliance. Ensure integration via APIs with idempotent endpoints, error handling, and audit trails for data sync.

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**
Budgeting is essential for resource planning and spend control. Design system with historical data analysis, driver-based models (seasonality, volume), rolling forecasts, and scenario simulation to improve accuracy.

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
Preserving cost history ensures accurate trend analysis, auditability, and performance benchmarking. Without it, cost overruns in the new warehouse are invisible. Relate historical data to the new BUC to track budget variances over time.

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.

---

# Detailed Analysis: Challenges, Strategies & Solutions

## Scenario 1: Cost Allocation and Tracking

### Challenges

| Challenge | Description |
|-----------|-------------|
| **Shared cost attribution** | Transportation and overhead costs span multiple warehouses/stores — no single natural owner. For example, a truck delivering to 3 stores must split its cost fairly. |
| **Data granularity mismatch** | Operational data (shipments, labor hours) lives at transaction level; financial data aggregates at month/quarter level. Reconciling the two requires extra transformation. |
| **Legacy system silos** | Warehousing, store management, and finance each use different systems with incompatible taxonomies and update cycles. |
| **Indirect cost allocation** | Overhead (rent, utilities, management) is hard to pin to a specific fulfillment activity. Arbitrary allocation percentages can distort profitability analysis. |
| **Volume variability** | Seasonal peaks and ad-hoc promotions change cost patterns, making static allocation models inaccurate. |

### Strategies & Solutions

1. **Driver-based allocation model** — Identify measurable cost drivers (e.g., pallet moves for warehouse labor, distance × weight for transportation). Allocate indirect costs proportionally to these drivers rather than using arbitrary percentages.
2. **Cost tagging at source** — Instrument each transaction (order pick, shipment, restock) with a cost center ID (warehouse/store/region). Capture cost at the most granular level possible so aggregation is reliable.
3. **Integration layer / data warehouse** — Build an ETL pipeline that normalizes cost data from all legacy systems into a unified star schema (fact: cost transactions; dimensions: time, location, cost type, business unit). This sidesteps the silo problem.
4. **Periodic reconciliation** — Run automated reconciliation between operational cost totals and financial ledger entries each month. Flag variances > threshold (e.g., 2%) for manual review.
5. **Dynamic allocation rules** — Use a rules engine that adjusts allocation percentages based on real-time volume data rather than annual static percentages.

---

## Scenario 2: Cost Optimization Strategies

### Challenges

| Challenge | Description |
|-----------|-------------|
| **Identifying high-impact levers** | Many potential optimizations (routing, inventory, labor scheduling, procurement) — hard to know which will yield the biggest savings. |
| **Service quality trade-off** | Cutting costs too aggressively (e.g., reducing inventory buffer) can hurt delivery SLAs and customer satisfaction. |
| **Organizational inertia** | Cross-functional changes (e.g., consolidating warehouses) require buy-in from operations, sales, and finance. |
| **Measurement & attribution** | It's difficult to isolate the impact of a single optimization when multiple changes happen simultaneously. |

### Strategies & Solutions

1. **ROI-prioritized roadmap** — Score each optimization candidate by expected savings (bottom-up estimate), implementation complexity, and risk to service. Tackle high-ROI, low-complexity items first (e.g., route optimization before warehouse consolidation).
2. **A/B testing & pilot programs** — Run a controlled pilot in one region or warehouse before rolling out company-wide. Measure fulfillment cost per order, on-time delivery rate, and stock-out frequency.
3. **Total Cost to Serve (TCS) framework** — Calculate the full cost of serving each customer/product/store including picking, packing, shipping, returns, and overhead. Identify the 20% of customers/products that drive 80% of costs.
4. **Incremental rollout with KPIs** — Roll out changes incrementally (e.g., one optimized route per week) with leading KPIs (cost per unit shipped, truck utilization rate) and lagging KPIs (gross margin, working capital).
5. **Scenario modeling** — Use a "what-if" simulator to model the impact of changes before implementation. Example: "What if we consolidate warehouse A into warehouse B?" — simulate impact on transportation costs, delivery times, and inventory carrying cost.

---

## Scenario 3: Integration with Financial Systems

### Challenges

| Challenge | Description |
|-----------|-------------|
| **Real-time vs. batch reality** | Financial systems (ERP, GL) are often batch-oriented (daily/monthly close). Real-time synchronization requires either changing the ERP or building a buffer layer. |
| **Data model mismatch** | Fulfillment tool tracks cost by warehouse/store/activity; financial system tracks by cost center and GL account. Mapping between the two is non-trivial. |
| **Idempotency & reliability** | Network failures or duplicate messages can cause double-counting of costs if the integration is not idempotent. |
| **Audit & compliance** | Financial data must be tamper-proof. The integration must support audit trails and be able to replay historical syncs. |
| **Latency expectations** | Different stakeholders have different freshness requirements: operations needs minutes, finance can tolerate daily. Building a single pipe that satisfies both adds complexity. |

### Strategies & Solutions

1. **API-first with idempotent endpoints** — Design idempotent REST endpoints (using idempotency keys) so that retries are safe. The financial system calls `/api/costs` with a unique transaction ID — duplicate calls produce the same result.
2. **Change Data Capture (CDC)** — Use CDC (e.g., Debezium) on the cost tool's database to stream cost events to a message broker (Kafka/RabbitMQ). The financial system consumes events at its own pace.
3. **Canonical data model** — Define a shared cost event schema (e.g., CloudEvents format) that both systems agree on. Use a transformation layer to map between the canonical model and each system's internal model.
4. **Outbox pattern** — Write cost events to an "outbox" table in the same database transaction as the cost record. A separate worker reads the outbox and pushes to the integration queue — guarantees exactly-once semantics without distributed transactions.
5. **Audit trail + replay** — Log every integration event with timestamp, payload hash, and processing status. Provide a replay mechanism for the finance team to re-process a date range if needed.
6. **Multi-SLA sync** — Offer two sync modes: near-real-time (streaming for ops dashboards) and daily batch snapshots (for GL closure). The batch sync reconciles and corrects any discrepancies from the streaming path.

---

## Scenario 4: Budgeting and Forecasting

### Challenges

| Challenge | Description |
|-----------|-------------|
| **Demand volatility** | Fulfillment costs are tied to order volume, which fluctuates seasonally and with promotions. A static annual budget becomes obsolete within weeks. |
| **Driver complexity** | Costs depend on multiple drivers: volume, fuel prices, labor rates, warehouse utilization. A simple linear model won't capture interactions. |
| **Data availability** | Forecasting requires historical data at sufficient granularity. New warehouses or new business lines may have little history. |
| **Forecast accuracy vs. effort** | Highly sophisticated ML models bring diminishing returns. Need to balance accuracy with maintainability. |
| **Organizational trust** | If forecasts are repeatedly wrong, stakeholders stop using them for decision-making. |

### Strategies & Solutions

1. **Driver-based rolling forecast** — Model cost as a function of measurable drivers: `Cost = f(order_volume, fuel_index, headcount, utilization_rate)`. Refresh the forecast monthly (rolling 12-month window) rather than sticking to an annual plan.
2. **Ensemble approach** — Combine a time-series model (ARIMA/SARIMA for baseline) with a driver-based regression model. Use the simpler model for well-established patterns; flag anomalies for manual review.
3. **Scenario simulation** — Allow planners to adjust driver assumptions (e.g., "order volume +15%", "fuel cost +10%") and instantly see the impact on budget. This builds confidence and enables proactive decision-making.
4. **Zero-based budgeting for new entities** — For new warehouses or stores with no history, use zero-based budgeting: build the budget from first principles (expected throughput × standard cost per unit).
5. **Variance analysis dashboard** — Show actual vs. budget vs. forecast for each cost category at each warehouse/store. Automatically flag variances > 10% with drill-down to transaction-level detail.
6. **ML-based demand sensing** — Use short-term (2-4 week) demand forecasts based on point-of-sale data, weather, and economic indicators to adjust fulfillment cost forecasts dynamically.

---

## Scenario 5: Cost Control in Warehouse Replacement

### Challenges

| Challenge | Description |
|-----------|-------------|
| **Historical cost continuity** | The old warehouse's cost history (labor, rent, transportation, etc.) is tied to the old entity. After replacement, analysts need to compare costs under the same BUC. |
| **Transition cost separation** | During the replacement period (old winding down, new ramping up), costs from both warehouses may overlap. Must distinguish one-time transition costs from ongoing operational costs. |
| **Baseline for budget tracking** | Without historical cost data, there is no baseline to judge whether the new warehouse is more or less expensive than the old one. |
| **Archived warehouse data retention** | The old warehouse's data must remain queryable for trend analysis and audits, even though the warehouse is no longer active. |

### Strategies & Solutions

1. **Cost history roll-forward** — When archiving the old warehouse and creating the new one with the same BUC, tag each cost record with a `warehouse_generation` field (e.g., "v1" for old, "v2" for new). Both generations share the BUC, so queries over the BUC return the full history.
2. **Transition cost tagging** — During the ramp-up phase (e.g., first 90 days), flag all costs for the new warehouse as "transition" via a dedicated cost category. This keeps transition costs visible but separable from steady-state operations.
3. **Period-over-period benchmarking** — Generate automated reports comparing the new warehouse's cost per unit, cost per sq. ft., and labor productivity against the old warehouse's last 12 months of data (adjusted for inflation/volume).
4. **Cost KPI dashboard for replacement** — Build a dedicated dashboard during replacement that shows:
   - Old warehouse historical trend (last 12 months)
   - New warehouse current performance (weekly)
   - Budget vs. actual for the new warehouse
   - Cumulative transition cost vs. budget
5. **Retention policy for archived entities** — Cost data for archived warehouses is retained in the same fact tables with a `warehouse_status = ARCHIVED` flag. Query the data mart with or without archived entities as needed.
6. **Budget baseline adjustment** — Set the initial budget for the new warehouse based on the old warehouse's actual costs, adjusted for known differences (new lease terms, automation level, location). This provides a realistic starting point.
7. **Post-replacement audit** — 6 months after replacement, run a formal audit comparing actual costs vs. the old warehouse benchmark. Publish findings to justify (or challenge) future replacement decisions.
