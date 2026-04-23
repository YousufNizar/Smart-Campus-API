# Smart Campus Sensor & Room Management REST API

This project implements the 5COSC022W coursework using **JAX-RS only** (Jersey) as a **Maven WAR** project for **Apache Tomcat** in NetBeans.

## Technology and Constraints Compliance

- Framework: JAX-RS (Jersey 2.x)
- Build: Maven
- Packaging: WAR
- Runtime: Apache Tomcat
- Persistence: In-memory data structures (`ConcurrentHashMap`, `ArrayList`) only
- No Spring Boot
- No SQL/NoSQL database

## API Base URL

When deployed to Tomcat with context path `smart-campus-api`:

- Base: `http://localhost:8080/smart-campus-api/api/v1`  
  (Note the **`/api/v1` segment** — not `/v1` alone. The project will redirect `/v1` to `/api/v1` automatically.)

## Project Structure

- `src/main/java/smartcampus/SmartCampusApplication.java` - JAX-RS app bootstrap (`@ApplicationPath("/api/v1")`; Jersey servlet maps `/*` in `web.xml`)
- `src/main/java/smartcampus/model` - POJOs (`Room`, `Sensor`, `SensorReading`)
- `src/main/java/smartcampus/store/InMemoryStore.java` - shared in-memory state
- `src/main/java/smartcampus/resource` - REST resources
- `src/main/java/smartcampus/exception` - custom exceptions
- `src/main/java/smartcampus/exception/mapper` - exception mappers
- `src/main/java/smartcampus/filter/ApiLoggingFilter.java` - request/response logging

## Build and Run (NetBeans + Tomcat)

1. Open NetBeans.
2. `File -> Open Project` and select the **project root** folder that contains this `pom.xml` (e.g. `CSA CW`), not a nested subfolder.
3. Add/configure Tomcat in NetBeans:
   - `Tools -> Servers -> Add Server -> Apache Tomcat`
   - Select local Tomcat installation folder.
4. Right-click project -> `Properties -> Run`:
   - Set Server: `Apache Tomcat`
   - Set Context Path: `/smart-campus-api`
5. Right-click project -> `Clean and Build`.
6. Right-click project -> `Run` (deploys WAR to Tomcat).

## Build and Run (Maven CLI)

```bash
mvn clean package
```

Deploy generated `target/smart-campus-api.war` to Tomcat (`webapps` folder), then start Tomcat.

## API Endpoints

### Discovery
- `GET /api/v1`

### Rooms
- `GET /api/v1/rooms`
- `POST /api/v1/rooms`
- `GET /api/v1/rooms/{roomId}`
- `DELETE /api/v1/rooms/{roomId}`

### Sensors
- `GET /api/v1/sensors`
- `GET /api/v1/sensors?type=CO2`
- `POST /api/v1/sensors`

### Sensor Readings (Sub-resource)
- `GET /api/v1/sensors/{sensorId}/readings`
- `POST /api/v1/sensors/{sensorId}/readings`

## Sample curl Commands (Postman Alternative)

Assume:
`BASE=http://localhost:8080/smart-campus-api/api/v1`

1) Discovery:
```bash
curl -i "$BASE"
```

2) Create room:
```bash
curl -i -X POST "$BASE/rooms" \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":80}"
```

3) Get all rooms:
```bash
curl -i "$BASE/rooms"
```

4) Create sensor linked to room:
```bash
curl -i -X POST "$BASE/sensors" \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":420.0,\"roomId\":\"LIB-301\"}"
```

5) Filter sensors by type:
```bash
curl -i "$BASE/sensors?type=CO2"
```

6) Add reading to sensor (also updates sensor currentValue):
```bash
curl -i -X POST "$BASE/sensors/CO2-001/readings" \
  -H "Content-Type: application/json" \
  -d "{\"value\":455.7}"
```

7) Retrieve reading history:
```bash
curl -i "$BASE/sensors/CO2-001/readings"
```

8) Attempt to delete room with assigned sensor (expect 409):
```bash
curl -i -X DELETE "$BASE/rooms/LIB-301"
```

9) Attempt sensor creation with invalid roomId (expect 422):
```bash
curl -i -X POST "$BASE/sensors" \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"TEMP-404\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":21.0,\"roomId\":\"NO-ROOM\"}"
```

## Conceptual Report Answers (Required Questions)

### Part 1.1 - JAX-RS Resource Lifecycle and Data Synchronization

In default JAX-RS behavior, resource classes are usually request-scoped, so a new instance is created per request unless explicitly configured otherwise. Because this API stores state in shared in-memory maps/lists (outside resource instances), thread safety is still essential. Concurrent requests can read/write the same data simultaneously, so synchronization and concurrent collections (e.g., `ConcurrentHashMap`) prevent race conditions and inconsistent state.

### Part 1.2 - Why Hypermedia (HATEOAS) Matters

Hypermedia helps clients discover valid actions dynamically through links/paths returned by the API. This reduces tight coupling to external static documentation and makes clients more resilient to API evolution, because navigation can be driven by response metadata instead of hard-coded URLs.

### Part 2.1 - Returning IDs vs Full Room Objects

Returning only IDs reduces payload size and bandwidth usage but forces clients to issue extra requests to fetch details. Returning full room objects increases payload size but reduces round-trips and simplifies client-side rendering. The best option depends on collection size and client needs; for moderate lists, full objects often improve usability.

### Part 2.2 - DELETE Idempotency

DELETE is idempotent in terms of final server state: after the first successful delete, the room no longer exists. Repeating the same DELETE request should not create new side effects; it typically returns 404 Not Found on later attempts because the resource is already gone. The state remains unchanged after repeated calls.

### Part 3.1 - Consequence of `@Consumes(MediaType.APPLICATION_JSON)` Mismatch

If the method consumes only JSON but the client sends `text/plain` or `application/xml`, JAX-RS content negotiation rejects the request, typically returning HTTP `415 Unsupported Media Type`. This protects endpoint contracts and prevents unsupported deserialization.

### Part 3.2 - Query Parameter vs Path Segment for Filtering

Filtering/searching a collection is best represented by query parameters (`/sensors?type=CO2`) because the base resource remains the same (`/sensors`) and filters are optional modifiers. Path segments are better for hierarchical resource identity, not ad-hoc search criteria.

### Part 4.1 - Benefits of Sub-Resource Locator Pattern

Sub-resource locators delegate nested responsibilities to dedicated classes, improving modularity and readability. This avoids monolithic controller classes and keeps sensor logic separate from reading-history logic, which scales better as nested operations grow.

### Part 4.2 - Historical Data and Consistency Side Effect

When a new reading is posted, the service stores it in sensor history and updates the parent sensor's `currentValue`. This keeps summary data (`currentValue`) consistent with the newest event and avoids stale values across endpoints.

### Part 5.2 - Why 422 Is Better Than 404 for Missing Linked IDs in Payload

`422 Unprocessable Entity` is semantically accurate when the request body is syntactically valid JSON but contains invalid business references (e.g., non-existent `roomId`). `404` is usually for a missing target URI resource, not an invalid relationship inside the payload.

### Part 5.4 - Security Risk of Exposed Stack Traces

Raw stack traces may leak package names, class names, internal file paths, framework versions, and implementation details. Attackers can use this data for targeted exploitation, dependency vulnerability mapping, and reconnaissance of backend architecture.

### Part 5.5 - Why Logging Filters for Cross-Cutting Concerns

JAX-RS request/response filters apply logging centrally for all endpoints, ensuring consistency and avoiding repeated `Logger.info()` statements in each method. This reduces boilerplate and makes observability easier to maintain.


