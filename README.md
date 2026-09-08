# Box Delivery Service

A REST API for managing delivery boxes and the items loaded into them, built as a take-home
assessment for Polaris Digitech Limited.

A **Box** represents a small autonomous delivery unit with a battery and a weight capacity.
An **Item** is something placed inside a box for delivery. The service allows creating boxes,
loading them with items, and querying their state.

## Tech Stack

- Java 21
- Spring Boot 4.1.1 (Web, Data JPA, Validation)
- PostgreSQL 16 (via Docker Compose)
- Maven (wrapper included — no local Maven install required)
- Lombok
- JUnit 5 + Mockito + AssertJ (unit tests)

## Prerequisites

- Java 21 (JDK)
- Docker and Docker Compose

## Build & Run

1. **Clone the repository**
```bash
   git clone <repo-url>
   cd box-delivery-service
```

2. **Configure environment variables**

   Copy `.env.example` to `.env` and adjust values if needed (defaults work out of the box):
```bash
   cp .env.example .env
```

3. **Start PostgreSQL**
```bash
   docker compose up -d
```
Confirm it's healthy:
```bash
   docker ps
```

4. **Run the application**
```bash
   ./mvnw spring-boot:run
```
The API will be available at `http://localhost:8080`.

5. **Run tests**
```bash
   ./mvnw test
```

## Seed Data

On every startup, `src/main/resources/data.sql` resets and repopulates the database with 5 boxes
(a mix of states and battery levels) and 2 pre-loaded items, so all endpoints can be exercised
immediately without first creating data:

| txref     | Weight Limit | Battery | State  |
|-----------|--------------|---------|--------|
| BOX-0001  | 500g         | 80%     | IDLE   |
| BOX-0002  | 300g         | 15%     | IDLE   |
| BOX-0003  | 450g         | 60%     | LOADED (has 2 items) |
| BOX-0004  | 200g         | 90%     | IDLE   |
| BOX-0005  | 500g         | 25%     | IDLE   |

## API Reference

### Create a box
`POST /api/boxes`

```json
{
    "txref": "BOX-0006",
    "weightLimit": 400,
    "batteryCapacity": 70
}
```
Returns `201 Created` with the new box (state `IDLE`).

### Load a box with items
`POST /api/boxes/{id}/load`

```json
[
    {
        "name": "Water_Bottle",
        "weight": 100,
        "code": "WTR_001"
    }
]
```
Returns `200 OK` with the updated box (state `LOADED`). Fails with `422` if the box isn't
`IDLE`, if battery is below 25%, or if the total weight would exceed the box's limit.

### Get items loaded in a box
`GET /api/boxes/{id}/items`

Returns `200 OK` with the list of items in the box.

### List boxes available for loading
`GET /api/boxes/available`

Returns `200 OK` with boxes in `IDLE` state with battery ≥ 25%.

### Check battery level for a box
`GET /api/boxes/{id}/battery`

```json
{
    "boxId": 1,
    "batteryCapacity": 80
}
```
## Postman Collection

A Postman collection covering all endpoints (including failure cases for the business rules)
is available at `postman/Box-Delivery-Service.postman_collection.json` — import it directly
into Postman to test the API.

## Assumptions

Per the task's invitation to document design assumptions:

- **"Available for loading"** is defined as: box state is `IDLE` **and** battery ≥ 25%.
- **Loading is batch-based** — the load endpoint accepts an array of items in a single request
  rather than one item at a time, so the weight-limit check happens atomically against the
  full batch.
- **State transitions** — a box moves `IDLE → LOADING → LOADED` automatically within a single
  load request. Only these three states have enforced business rules; `DELIVERING`,
  `DELIVERED`, and `RETURNING` are modeled per the spec but have no endpoints, since none were
  requested.
- **Weight limit is cumulative** — if a box already has items loaded, a new load request is
  validated against the *combined* total, not just the new items.
- **PostgreSQL over H2** — chosen for a more realistic setup; run via Docker Compose so the
  reviewer needs no local Postgres install.
- **No hardcoded credentials** — all database configuration is externalized via `.env`
  (see `.env.example`), with no fallback defaults on username/password, so misconfiguration
  fails loudly rather than silently.

## Business Rules Enforced

- A box cannot be loaded with more weight than its `weightLimit` (500g max per box).
- A box cannot enter `LOADING` state if its battery is below 25%.