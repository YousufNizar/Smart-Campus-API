# Smart Campus Sensor & Room Management API

This project implements the 5COSC022W Client-Server Architectures coursework using **JAX-RS only** (Jersey) as a **Maven WAR** project deployed on **Apache Tomcat**. It provides a RESTful API for managing campus rooms, sensors, and sensor readings as part of the Smart Campus initiative.

---

## API Design Overview

This API is a RESTful Smart Campus management system built with JAX-RS (Jersey) and deployed on Apache Tomcat 9. It manages three core resources:

- **Rooms** — Physical campus spaces identified by a unique ID (e.g. `LIB-301`), with a name, capacity, and a list of assigned sensor IDs.
- **Sensors** — Hardware devices (CO2, Temperature, Occupancy etc.) assigned to rooms, with a status (`ACTIVE`, `MAINTENANCE`, `OFFLINE`) and a current reading value.
- **Sensor Readings** — A historical log of measurement events recorded by each sensor, implemented as a sub-resource of Sensors.

### Resource Hierarchy

```
/api/v1
├── /rooms
│   ├── GET    → list all rooms
│   ├── POST   → create a room
│   └── /{roomId}
│       ├── GET    → get a single room by ID
│       └── DELETE → decommission a room (blocked if sensors are assigned)
└── /sensors
    ├── GET    → list all sensors (supports optional ?type= filter)
    ├── POST   → register a new sensor (validates roomId exists)
    ├── GET /{sensorId} → get a single sensor by ID
    └── /{sensorId}/readings
        ├── GET  → retrieve full reading history for that sensor
        └── POST → append a new reading (also updates sensor currentValue)
```

### Data Storage

All data is held in memory using `ConcurrentHashMap` inside a singleton `InMemoryStore` class. No database is used. All data resets on server restart.

### Error Handling Strategy

The API uses custom JAX-RS Exception Mappers for every error scenario — no raw Java stack traces are ever exposed to the caller:

| HTTP Status | Scenario | Exception |
|---|---|---|
| 409 Conflict | Deleting a room that still has sensors assigned | `RoomNotEmptyException` |
| 422 Unprocessable Entity | Creating a sensor with a non-existent roomId | `LinkedResourceNotFoundException` |
| 403 Forbidden | Posting a reading to a MAINTENANCE sensor | `SensorUnavailableException` |


---

## Technology and Constraints Compliance

- Framework: JAX-RS (Jersey 2.x)
- Build: Maven
- Packaging: WAR
- Runtime: Apache Tomcat 9
- Persistence: In-memory data structures (`ConcurrentHashMap`, `ArrayList`) only
- No Spring Boot
- No SQL/NoSQL database

---

## API Base URL

When deployed to Tomcat with context path `smart-campus-api`:

```
http://localhost:8080/smart-campus-api/api/v1
```

> **Note:** The `/api/v1` segment is required — not `/v1` alone. The project automatically redirects `/v1` to `/api/v1`.

---

## Project Structure

```
src/main/java/smartcampus/
├── SmartCampusApplication.java        — JAX-RS bootstrap (@ApplicationPath("/api/v1"))
├── model/
│   ├── Room.java                      — Room POJO
│   ├── Sensor.java                    — Sensor POJO
│   └── SensorReading.java             — SensorReading POJO
├── store/
│   └── InMemoryStore.java             — Shared in-memory singleton (ConcurrentHashMap)
├── resource/
│   ├── DiscoveryResource.java         — GET /api/v1 discovery endpoint
│   ├── SensorRoomResource.java        — Room endpoints
│   ├── SensorResource.java            — Sensor endpoints
│   └── SensorReadingResource.java     — Sub-resource for sensor readings
├── exception/
│   ├── RoomNotEmptyException.java
│   ├── LinkedResourceNotFoundException.java
│   └── SensorUnavailableException.java
├── exception/mapper/
│   ├── RoomNotEmptyExceptionMapper.java
│   ├── LinkedResourceNotFoundExceptionMapper.java
│   ├── SensorUnavailableExceptionMapper.java
│   └── ThrowableExceptionMapper.java  — Global 500 safety net
└── filter/
    └── ApiLoggingFilter.java          — Request/response logging filter
```

---

## Build and Run (NetBeans + Tomcat)

1. Open NetBeans.
2. Go to `File → Open Project` and select the **project root** folder that contains `pom.xml` (e.g. `CSA CW`). Do not select a nested subfolder.
3. Add Tomcat to NetBeans if not already configured:
   - Go to `Tools → Servers → Add Server → Apache Tomcat`
   - Browse to your local Tomcat 9 installation folder and click Finish.
4. Configure the project to use Tomcat:
   - Right-click the project → `Properties → Run`
   - Set **Server**: `Apache Tomcat`
   - Set **Context Path**: `/smart-campus-api`
   - Click OK.
5. Right-click the project → **Clean and Build**. Wait for `BUILD SUCCESS` in the Output panel.
6. Right-click the project → **Run**. NetBeans will deploy the WAR to Tomcat automatically.
  or
  Right-click the project → Clean and Build
Copy new smart-campus-api.war(inside the Target Folder) to Tomcat webapps folder
Restart Tomcat manually in the Powershell (shutdown.bat → startup.bat)
7. Open your browser or Postman and visit:
   ```
   http://localhost:8080/smart-campus-api/api/v1
   ```

---

## Build and Run (Maven CLI + Manual Deploy)

**Step 1 — Build the WAR:**
```bash
mvn clean package
```

**Step 2 — Copy the WAR to Tomcat:**
```bash
# Windows
copy target\smart-campus-api.war C:\apache-tomcat-9.x.x\webapps\

# Mac/Linux
cp target/smart-campus-api.war /opt/tomcat/webapps/
```

**Step 3 — Start Tomcat:**
```bash
# Windows
C:\apache-tomcat-9.x.x\bin\startup.bat

# Mac/Linux
/opt/tomcat/bin/startup.sh
```

**Step 4 — Test:**
```
http://localhost:8080/smart-campus-api/api/v1
```

> **Prerequisites:** Java JDK 11, Apache Tomcat 9, Maven 3.x

---

## API Endpoints

### Discovery
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1` | Returns API metadata, version info, and resource links |

### Rooms
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Get a single room by ID |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors are assigned — 409) |

### Sensors
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/sensors` | List all sensors |
| GET | `/api/v1/sensors?type=CO2` | Filter sensors by type |
| GET | `/api/v1/sensors/{sensorId}` | Get a single sensor by ID |
| POST | `/api/v1/sensors` | Register a new sensor (validates roomId — 422 if not found) |

### Sensor Readings (Sub-resource)
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/sensors/{sensorId}/readings` | Get reading history for a sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a reading (updates sensor currentValue; 403 if MAINTENANCE) |

---

## Sample curl Commands

Set the base URL first:
```bash
BASE=http://localhost:8080/smart-campus-api/api/v1
```

**1. Discovery — GET /api/v1**
```bash
curl -i "$BASE"
```

**2. Create a room — POST /rooms**
```bash
curl -i -X POST "$BASE/rooms" \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":80}"
```

**3. Get all rooms — GET /rooms**
```bash
curl -i "$BASE/rooms"
```

**4. Get a single room by ID — GET /rooms/{roomId}**
```bash
curl -i "$BASE/rooms/LIB-301"
```

**5. Create a sensor linked to a room — POST /sensors**
```bash
curl -i -X POST "$BASE/sensors" \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":420.0,\"roomId\":\"LIB-301\"}"
```

**6. Get a single sensor by ID — GET /sensors/{sensorId}**
```bash
curl -i "$BASE/sensors/CO2-001"
```

**7. Filter sensors by type — GET /sensors?type=**
```bash
curl -i "$BASE/sensors?type=CO2"
```

**8. Add a reading to a sensor — POST /sensors/{sensorId}/readings**
```bash
curl -i -X POST "$BASE/sensors/CO2-001/readings" \
  -H "Content-Type: application/json" \
  -d "{\"value\":455.7}"
```

**9. Retrieve reading history — GET /sensors/{sensorId}/readings**
```bash
curl -i "$BASE/sensors/CO2-001/readings"
```

**10. Attempt to delete a room with sensors assigned (expect 409)**
```bash
curl -i -X DELETE "$BASE/rooms/LIB-301"
```

**11. Attempt to create a sensor with an invalid roomId (expect 422)**
```bash
curl -i -X POST "$BASE/sensors" \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"TEMP-404\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":21.0,\"roomId\":\"NO-ROOM\"}"
```

---

## Conceptual Report Answers

### Part 1.1 — JAX-RS Resource Lifecycle and Data Synchronization

**Question:** In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

In JAX-RS, the default lifecycle of a resource class is per-request, meaning the runtime creates a new instance of the resource class for every incoming HTTP request and discards it once the response is sent. This is intentional  it prevents shared mutable state inside resource objects and makes it easier to manage multiple requests safely.

However, this creates a challenge for data management. Since each request gets a completely fresh resource instance, any data stored inside the resource class itself would be lost between requests. To solve this, all persistent state in this API is held in a shared singleton called `InMemoryStore`, which exists independently of any resource instance lifecycle. Every resource class reads from and writes to this shared store.

Because multiple requests can arrive simultaneously and all access the same `InMemoryStore`, thread safety becomes critical. If two requests tried to add a sensor at the same time using a standard `HashMap`, a race condition could corrupt the map's internal structure and lead to data loss or inconsistent state. To prevent this, the API uses `ConcurrentHashMap`, which allows safe concurrent reads and writes without requiring explicit `synchronized` blocks. This ensures data integrity even under heavy load, which is important for a campus-wide system managing thousands of rooms and sensors.

---

### Part 1.2 — Why Hypermedia (HATEOAS) Matters

**Question:** Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

HATEOAS, which stands for Hypermedia as the Engine of Application State, is considered a hallmark of advanced RESTful design because it allows clients to navigate an API dynamically without needing to know URLs in advance. Rather than a client having to hardcode that sensors live at `/api/v1/sensors`, the Discovery endpoint returns that link directly in the response body. The client simply follows the links it receives.

This approach benefits client developers in three main ways. First, it reduces tight coupling between the client and the API. If a URL path changes in a future version, clients that follow links from responses will adapt without breaking, rather than requiring updates across every client application. Second, it improves discoverability — a new developer can explore the entire API starting from a single root endpoint, much like navigating a website by following links rather than memorising every page address. Third, it makes the API partially self-documenting, meaning clients are less dependent on external documentation that may go out of date. Overall, HATEOAS makes APIs more resilient to change and significantly easier for client developers to work with over time.

---

### Part 2.1 — Returning IDs vs Full Room Objects

**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

Returning only IDs minimises the payload size and reduces bandwidth consumption, which can be useful for very large collections or low-bandwidth environments. However, this approach forces the client to make an additional GET request for each room ID to retrieve its details — a problem commonly known as the N+1 problem. As the number of rooms grows, this significantly increases the total number of network round-trips, adds latency, and puts more load on the server.

Returning full room objects, on the other hand, delivers all necessary information in a single response and removes the need for those extra requests. The trade-off is a slightly larger payload per response. For this Smart Campus API, returning full room objects is the more practical choice. The data per room — id, name, capacity, and sensorIds — is relatively lightweight, and the benefit of avoiding repeated round trips clearly outweighs the small increase in payload size. For clients like facilities management systems that need to display room details immediately, this approach also simplifies the client-side logic considerably.

---

### Part 2.2 — DELETE Idempotency

**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

Yes, the DELETE operation is idempotent in this implementation. Idempotency means that making the same request multiple times produces the same server state as making it once. After the first successful DELETE of a room, the resource is permanently removed from the `InMemoryStore`. Any subsequent DELETE request for the same room ID will return a 404 Not Found response, because the resource no longer exists , but crucially, the server state is not changed by those repeated calls. Nothing extra happens and no side effects are introduced.

It is worth noting that the HTTP status code may differ between calls , the first might return 200 or 204, while subsequent ones return 404. This does not violate idempotency, because idempotency is about server state, not the response code. The server state remains consistently unchanged after the first deletion regardless of how many times the request is repeated.

There is also a business logic constraint in place: a room with active sensors assigned to it cannot be deleted and will return a 409 Conflict response. This also preserves idempotent behaviour, since the server state remains consistently unchanged until the sensors are removed from the room first.

---

### Part 3.1 — @Consumes(MediaType.APPLICATION_JSON) Mismatch

**Question:** We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that this endpoint will only accept requests with a `Content-Type: application/json` header. If a client attempts to send data in a different format  such as `text/plain` or `application/xml` — JAX-RS performs content negotiation and finds no method that matches the incoming content type. It then automatically rejects the request and returns **HTTP 415 Unsupported Media Type**, without the request ever reaching the resource method or the deserialization layer.

This is beneficial for two reasons. First, it enforces a strict API contract and makes the expected input format explicit. Clients know exactly what format is required, and violations are caught immediately with a meaningful error code rather than causing unexpected behaviour deeper in the system. Second, it protects against deserialization errors that would occur if Jackson attempted to parse non-JSON content, which could trigger exceptions or produce corrupted data. By declaring `@Consumes` explicitly, the API fails fast and cleanly when its contract is not followed.

---

### Part 3.2 — Query Parameter vs Path Segment for Filtering

**Question:** You implemented this filtering using `@QueryParam`. Contrast this with an alternative design where the type is part of the URL path (e.g., `/api/v1/sensors/type/CO2`). Why is the query parameter approach generally considered superior for filtering and searching collections?

Using `@QueryParam` for filtering — such as `/api/v1/sensors?type=CO2` — is considered superior to embedding the filter in the path for several reasons.

First, it follows REST semantics correctly. A path segment is meant to identify a specific resource. The path `/sensors/type/CO2` implies that `type/CO2` is a distinct resource, which is semantically incorrect , it is a filter criterion, not a resource. Query parameters are specifically designed for optional, non-hierarchical modifiers that refine a collection.

Second, query parameters are naturally optional. Without `?type=CO2`, a `GET /sensors` request still returns all sensors , the same endpoint handles both the filtered and unfiltered use cases. A path-based approach would require a separate endpoint entirely for unfiltered access, which adds unnecessary complexity.

Third, multiple filters can be combined easily with query parameters, for example `?type=CO2&status=ACTIVE`. Achieving the same with path segments quickly becomes inconsistent and hard to read. For these reasons, the `@QueryParam` approach keeps the API clean, flexible, and aligned with RESTful principles.

---

### Part 4.1 — Benefits of Sub-Resource Locator Pattern

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?

The Sub-Resource Locator pattern allows a resource class to delegate the handling of a nested path to a dedicated, separate class. In this API, `SensorResource` manages the `/api/v1/sensors` path and delegates `/api/v1/sensors/{sensorId}/readings` to a dedicated `SensorReadingResource` class by returning an instance of it from a locator method. This delegation happens cleanly without `SensorResource` needing to know anything about how readings are managed internally.

The primary benefit of this pattern is separation of concerns. `SensorResource` is focused entirely on sensor management  creating sensors, filtering by type, and validating room references. `SensorReadingResource` is focused entirely on reading history , appending new readings and keeping the parent sensor's `currentValue` up to date. Neither class needs to know the internal implementation details of the other, which makes each class easier to read, understand, and modify independently.

This is a significant improvement over a monolithic approach, where every nested path — including `sensors/{id}/readings`, `sensors/{id}/readings/{rid}`, and any future additions , would all be handled inside a single controller class. As the API grows, such a class would become increasingly difficult to maintain and test. With the sub-resource locator pattern, each class can also be unit tested in isolation, which improves overall code quality. The pattern also allows JAX-RS to manage the lifecycle and inject context into sub-resource classes cleanly, keeping the framework's dependency management working correctly across all nested resource levels.

---

### Part 5.2 — Why 422 Is More Accurate Than 404 for Missing Linked IDs

**Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

HTTP 404 Not Found is designed for situations where the target URI itself does not exist  for example, when a client requests a room at `/api/v1/rooms/FAKE-ROOM` and no such room exists at that path. The problem in that case is the URI itself.

HTTP 422 Unprocessable Entity is the more appropriate status code when the URI is valid and the request body is syntactically correct JSON, but the content contains a business-level reference that cannot be resolved. In this API, when a client sends a valid POST request to `/api/v1/sensors` with a `roomId` that does not exist in the system, the endpoint and the JSON format are both perfectly valid , the issue is that the `roomId` value inside the payload points to something that does not exist. Using 422 communicates this distinction clearly to the client: the request was understood, but it cannot be processed because of a broken internal reference. A 404 in this context would be misleading, as it suggests the URL itself is wrong rather than the content of the request body.

---

### Part 5.4 — Security Risk of Exposed Stack Traces

**Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Exposing raw Java stack traces to external API consumers is a significant security risk. A stack trace typically reveals several pieces of information that an attacker can exploit:

- **Package and class names** — for example, `smartcampus.store.InMemoryStore` reveals the internal architecture and naming conventions of the application, making it easier to target specific components.
- **Framework and library versions** — details such as Jersey 2.41 or Jackson allow an attacker to search for known CVEs (Common Vulnerabilities and Exposures) specific to those exact versions and craft targeted exploits.
- **File paths and line numbers** — these reveal the server's directory structure and pinpoint exactly where specific logic lives in the codebase.
- **Internal code flow** — the method call chain shows the order in which code executes, which can help an attacker understand how to craft malicious inputs designed to trigger specific code paths or bypass validation logic.

With this information, an attacker can perform reconnaissance, map dependencies with known vulnerabilities, and design targeted attacks without ever gaining direct access to the server. This API addresses this risk through a global `ExceptionMapper<Throwable>` that intercepts all unexpected errors and returns only a generic HTTP 500 response with a safe, non-revealing JSON message — ensuring that no internal details are ever exposed to external consumers.

---

### Part 5.5 — Why Logging Filters for Cross-Cutting Concerns

**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting `Logger.info()` statements inside every single resource method?

Logging is a cross-cutting concern — it needs to apply consistently across every endpoint in the API, regardless of what business logic that endpoint performs. Using a JAX-RS `ContainerRequestFilter` and `ContainerResponseFilter` centralises this responsibility in a single class, `ApiLoggingFilter`, which the framework automatically applies to every incoming request and every outgoing response. No individual resource method needs to be aware of it.

Manually inserting `Logger.info()` statements into every resource method has several clear disadvantages. First, it creates significant code duplication every method across every resource class needs the same logging calls, which adds noise and increases the volume of code to maintain. Second, it is error-prone  a developer adding a new endpoint might simply forget to add the logging statement, creating silent gaps in observability that are difficult to detect. Third, it violates the Single Responsibility Principle by mixing logging concerns with business logic inside the same method, making the code harder to read and reason about.

The filter approach eliminates all of these problems. It guarantees consistent, complete logging coverage with zero impact on resource method code. It also makes future changes straightforward  for example, adding request timing, correlation IDs, or log levels only requires modifying the filter class in one place rather than updating dozens of methods across the codebase.

---
