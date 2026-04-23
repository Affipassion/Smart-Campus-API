# Smart Campus API

## ? Overview

The Smart Campus API is a RESTful web service built using **JAX-RS (Jersey)** and deployed on **Apache Tomcat**. It simulates a smart campus system that manages rooms, sensors, and sensor readings.

The API allows:

* Managing rooms
* Registering sensors
* Recording and retrieving sensor readings
* Filtering sensors
* Enforcing business rules

All data is stored using in-memory collections.

---

## Technologies Used

* Java (JDK 8)
* JAX-RS (Jersey 2.32)
* Apache Tomcat
* Maven
* HashMap, ArrayList, ConcurrentHashMap

---

## How to Run the Application

To run the Smart Campus API:

1. Open the project in NetBeans.
2. Make sure Apache Tomcat is configured correctly.
3. Right-click the project and choose **Clean and Build** to compile the application and resolve dependencies.
4. Once the build is successful, right-click the project and choose **Run**.
5. NetBeans will start Apache Tomcat and deploy the application automatically.
6. The API will then be accessible at:

http://localhost:8082/SmartCampusAPI/api/v1/
Base URL:

```text id="a8h0u6"
http://localhost:8082/SmartCampusAPI/api/v1/
```

---

# ? API Endpoints

### Discovery

```bash id="z5nh6y"
curl -X GET http://localhost:8082/SmartCampusAPI/api/v1/
```

### Rooms

```bash id="zzk1jr"
curl -X GET http://localhost:8082/SmartCampusAPI/api/v1/rooms
```

```bash id="3d54l3"
curl -X POST http://localhost:8082/SmartCampusAPI/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{"name":"Lab","capacity":30}'
```

### Sensors

```bash id="1dqvnl"
curl -X GET http://localhost:8082/SmartCampusAPI/api/v1/sensors
```

```bash id="3ex3ha"
curl -X GET "http://localhost:8082/SmartCampusAPI/api/v1/sensors?type=CO2"
```

### Readings

```bash id="u2w9cr"
curl -X GET http://localhost:8082/SmartCampusAPI/api/v1/sensors/TEMP-001/readings
```

```bash id="v0nru3"
curl -X POST http://localhost:8082/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
-H "Content-Type: application/json" \
-d '{"value":28.7}'
```

---

# ⚠️ Error Handling

| Code | Meaning              |
| ---- | -------------------- |
| 400  | Bad Request          |
| 403  | Forbidden            |
| 404  | Not Found            |
| 409  | Conflict             |
| 422  | Unprocessable Entity |
| 500  | Server Error         |

---

#  Business Rules

* A room cannot be deleted if it contains sensors
* A sensor cannot be created if the room does not exist
* Posting a reading updates the sensor’s current value
* Sensors under maintenance cannot accept readings

---

# THEORY QUESTIONS & ANSWERS

---

## Q1. What is the default lifecycle of a JAX-RS resource class?

The default lifecycle of a JAX-RS resource class is **request-scoped**, meaning a new instance of the resource is created for each incoming HTTP request.

In this implementation, resource classes such as `RoomResource` and `SensorResource` do not store shared state directly. Instead, shared data is maintained in a centralized service (`DataService`) using in-memory structures like `ConcurrentHashMap`.

This design ensures that:

* resource classes remain stateless
* shared data is safely managed
* concurrent requests do not cause data inconsistency

---

## Q2. Why is Hypermedia (HATEOAS) important?

Hypermedia improves API usability by allowing the server to provide navigation links within responses.

In this implementation, the **DiscoveryResource (`/api/v1/`)** returns available endpoints such as:

* `/api/v1/rooms`
* `/api/v1/sensors`

This helps clients discover available resources dynamically instead of hardcoding URLs.

Although this is a basic implementation of hypermedia, it improves:

* API discoverability
* flexibility
* ease of integration

---

## Q3. Should APIs return IDs or full objects?

Returning only IDs reduces response size but requires additional requests to fetch details. Returning full objects provides complete information in one response.

In this project, endpoints such as:

```text
GET /rooms
GET /sensors
```

return full objects (`Room`, `Sensor`).

This improves usability because clients can directly access all required data without making multiple API calls.

---

## Q4. Is DELETE idempotent?

Yes, DELETE is idempotent in this implementation.

In the `deleteRoom()` method:

* If the room exists and has no sensors → it is deleted
* If the request is repeated → the room is already removed → `404 Not Found`
* If the room has sensors → deletion is blocked → `409 Conflict`

In all cases, repeating the request does not change the system state further, which satisfies idempotency.

---

## Q5. What happens if a non-JSON request is sent?

The API uses:

```java
@Consumes(MediaType.APPLICATION_JSON)
```

If a client sends data in another format (e.g., XML or plain text), the server responds with:

```text
415 Unsupported Media Type
```

This ensures that:

* only valid JSON input is accepted
* request processing remains consistent

---

## Q6. Why use @QueryParam for filtering?

Filtering is implemented using:

```text
GET /sensors?type=CO2
```

This uses `@QueryParam` because:

* filtering is optional
* it does not represent a new resource
* it allows flexible queries

If filtering was done via path parameters, it would make the API less scalable and harder to extend.

---

## Q7. What are the benefits of sub-resources?

Sub-resources improve modularity and separation of concerns.

In this implementation:

```text
/sensors/{sensorId}/readings
```

is handled by `SensorReadingResource`, which is returned from `SensorResource`.

This design:

* keeps `SensorResource` focused on sensors
* moves reading logic to a separate class
* improves maintainability and clarity

---

## Q8. Why update currentValue after posting a reading?

In `DataService.addReading()`:

```java
sensor.setCurrentValue(reading.getValue());
```

This ensures that:

* the sensor always reflects the latest reading
* there is consistency between sensor summary and reading history

Without this, the API could return outdated sensor values, leading to incorrect data representation.

---

## Q9. Why use HTTP status codes?

HTTP status codes clearly communicate the result of API operations.

Examples in this implementation:

* `200 OK` → successful GET
* `201 Created` → successful POST
* `404 Not Found` → missing resource
* `409 Conflict` → room not empty

This improves:

* API usability
* debugging
* adherence to REST standards

---

## Q10. Why use 422 instead of 404?

In this implementation:

```text
POST /sensors
```

If a non-existent `roomId` is provided:

* the endpoint exists
* the request format is valid
* but the data is incorrect

Therefore:

* `422 Unprocessable Entity` is more appropriate
* `404` would incorrectly suggest the endpoint is missing

---

## Q11. Why not expose stack traces?

Exposing stack traces reveals:

* internal class names
* file structure
* system logic

This is a security risk.

In this project, `GlobalExceptionMapper` is used to:

* catch unexpected errors
* return clean JSON responses
* hide internal details

---

## Q12. Why use filters for logging?

Logging is implemented using:

* `ContainerRequestFilter`
* `ContainerResponseFilter`

This allows:

* centralized logging
* consistent logging across all endpoints

Compared to adding logging in every method, filters:

* reduce code duplication
* improve maintainability
* keep resource classes clean

---

## ⭐ Final Note

All answers above are directly linked to the implemented classes:

* `RoomResource`
* `SensorResource`
* `SensorReadingResource`
* `DataService`
* Exception Mappers and Filters

This demonstrates both theoretical understanding and practical implementation.
