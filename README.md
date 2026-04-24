# Smart Campus API

## Overview

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

#  API Endpoints

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

# Error Handling

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

## 
Question 1: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a
new instance instantiated for every incoming request, or does the runtime treat it as a
singleton? Elaborate on how this architectural decision impacts the way you manage and
synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

Answer:By default, a JAX-RS resource class is usually request-scoped, meaning a new instance of the resource class is created for each incoming HTTP request. 
The runtime does not normally treat a standard resource class as a singleton unless it is explicitly configured that way.
This means resource classes should not store important shared application data in instance variables, because each request may receive a different resource instance. 
In my implementation, classes such as RoomResource and SensorResource mainly handle HTTP requests and responses, while shared data is stored separately in the DataService class.
Since the coursework uses in-memory data structures instead of a database, shared maps and lists must be managed carefully. If multiple clients send requests at the same time, normal collections such as HashMap could lead to race conditions or inconsistent data.
 Therefore, using a centralized service layer with thread-aware structures such as ConcurrentHashMap helps reduce the risk of data loss and keeps rooms, sensors, and readings consistent across requests.
---

## 
Question 2: Why is the provision of ”Hypermedia” (links and navigation within responses)
considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach
benefit client developers compared to static documentation?

Answer: Hypermedia, also known as HATEOAS, is considered an important part of advanced RESTful design because the API response can guide the client by providing links to available resources and possible next actions.
In my implementation, the discovery endpoint returns API information and links such as /api/v1/rooms and /api/v1/sensors.
 This means the client can discover the main resources of the API from the response instead of needing to know every endpoint in advance.
Compared to static documentation, hypermedia reduces hardcoding and makes the API easier to navigate. If the API grows later, new resource links can be added to the response, helping client developers understand available operations more easily. 
This improves discoverability, flexibility, and client-server independence.





##  
Question 3: When returning a list of rooms, what are the implications of returning only
IDs versus returning the full room objects? Consider network bandwidth and client side
processing.
Answer: Returning only room IDs reduces the response size, so it uses less network bandwidth. This is useful when there are many rooms and the client only needs a summary. However, the client would then need to send extra requests such as GET /rooms/{roomId} to get full details for each room. 
This increases client-side processing and may create more API calls.
Returning full room objects uses more bandwidth because each response includes details such as room ID, name, capacity, and sensor IDs. 
However, it is easier for the client because all room information is available in one response.
In my implementation, GET /rooms returns full room objects. This is suitable because the dataset is small, and it makes the API easier to test and use in Postman without requiring multiple follow-up requests.

## 
Question 4: Is the DELETE operation idempotent in your implementation? Provide a detailed
justification by describing what happens if a client mistakenly sends the exact same DELETE
request for a room multiple times.

Answer: Yes, the DELETE operation is idempotent in my implementation.
If a room exists and has no sensors, the first DELETE request removes the room successfully. 
If the same DELETE request is sent again, the room is already deleted, so the API returns 404 Not Found. Even though the response is different, the final server state is still the same: the room does not exist.
If the room contains sensors, the API blocks the deletion and returns 409 Conflict. If the same DELETE request is repeated, it will still return 409 Conflict as long as sensors are assigned to that room.
Therefore, repeated DELETE requests do not create additional side effects or keep changing the system state. This satisfies the REST principle of idempotency.

## 
Question 5: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on
the POST method. Explain the technical consequences if a client attempts to send data in
a different format, such as text/plain or application/xml. How does JAX-RS handle this
mismatch?

Answer: The annotation @Consumes(MediaType.APPLICATION_JSON) tells JAX-RS that the POST method only accepts requests with a Content-Type of application/json.
If a client sends data in a different format such as text/plain or application/xml, JAX-RS will not attempt to process the request body. 
Instead, it will automatically reject the request and return an HTTP 415 Unsupported Media Type response.
Technically, this happens because the JAX-RS runtime looks for a suitable message body reader that can convert the incoming data into the Java object (e.g., Sensor or Room). 
If no matching reader is found for the given Content-Type, the request is considered invalid.
This behavior is important because it:

i.  enforces a strict contract between client and server
ii. prevents incorrect or unexpected data formats from being processed
iii.improves reliability and consistency of the API

Therefore, clients must send properly formatted JSON when interacting with POST endpoints in this API


## 
Question 6: You implemented this filtering using @QueryParam. Contrast this with an alternative
design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why
is the query parameter approach generally considered superior for filtering and searching
collections

Answer: Using @QueryParam is generally better for filtering because filtering is an optional condition applied to a collection, not a separate resource.
For example:
GET /api/v1/sensors?type=CO2
This means the client is still requesting the sensors collection, but only wants sensors where the type is CO2.
It makes the URL less flexible because it treats the filter like a fixed resource path. 
This can become difficult to extend if more filters are needed later, such as filtering by status, room ID, or sensor value.

In my implementation, @QueryParam("type") allows optional filtering while keeping the main endpoint simple:
GET /sensors
GET /sensors?type=CO2



##
Question 7: Discuss the architectural benefits of the Sub-Resource Locator pattern. How
does delegating logic to separate classes help manage complexity in large APIs compared
to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller
class?

Answer:The Sub-Resource Locator pattern is useful when an API has nested resources. In this coursework, sensor readings belong to a specific sensor, 
so the endpoint is naturally written as:
/sensors/{sensorId}/readings
Instead of placing all sensor and reading logic inside one large controller class, the logic is separated into different classes. In my implementation, SensorResource handles sensor-related operations, while SensorReadingResource handles reading history and adding new readings.
This improves the architecture because each class has a clear responsibility. It makes the code easier to read, test, maintain, and extend. 
For example, if more reading-related endpoints are added later, they can be added inside SensorReadingResource without making SensorResource too large.
Therefore, the Sub-Resource Locator pattern reduces complexity and avoids creating one massive resource/controller class with too many nested paths.



## 
Question 8: Why is HTTP 422 often considered more semantically accurate than a standard
404 when the issue is a missing reference inside a valid JSON payload?

Answer: HTTP 422 Unprocessable Entity is more semantically accurate because the request is valid in structure, but the data inside the request is logically incorrect.
In this API, when a client sends a POST request to create a sensor, the endpoint /sensors exists and the JSON payload may be correctly formatted.
 However, if the roomId inside the JSON refers to a room that does not exist, the server cannot process the request because the linked resource is invalid.
A 404 Not Found would suggest that the requested URL or endpoint does not exist. But in this case, the endpoint exists; only the reference inside the request body is wrong.
Therefore, 422 gives a clearer meaning: the server understood the request, but it cannot process it because the payload contains invalid linked data.


##
Question 9: From a cybersecurity standpoint, explain the risks associated with exposing
internal Java stack traces to external API consumers. What specific information could an
attacker gather from such a trace?

Answer: Exposing internal Java stack traces to external API consumers is a serious security risk because it reveals sensitive information about how the application is implemented and how it behaves internally.
A stack trace can expose details such as:
- package and class names  
- method names and execution flow  
- file names and exact line numbers  
- frameworks and libraries used  
An attacker can use this information to:
- identify technologies and search for known vulnerabilities  
- understand the internal structure of the system  
- locate weak points such as validation or error handling logic  
- craft targeted malicious requests to exploit the system  

From a cybersecurity perspective, this is known as **information leakage**, which makes it easier for attackers to analyze and attack the application.
In this implementation, a `GlobalExceptionMapper` using `ExceptionMapper<Throwable>` is used as a safety mechanism. Instead of exposing stack traces, it returns a generic `500 Internal Server Error` response with a simple JSON message.
This prevents sensitive internal details from being exposed while still informing the client that an unexpected error occurred.


## 
Question 10: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like
logging, rather than manually inserting Logger.info() statements inside every single resource
method?

Answer: It is advantageous to use JAX-RS filters for logging because logging is a cross-cutting concern, meaning it applies to all endpoints in the API rather than a single method.
If `Logger.info()` statements are manually added inside every resource method, the code becomes repetitive, harder to maintain, and increases the risk of missing logs in some methods.
In this implementation, logging is handled using `ContainerRequestFilter` and `ContainerResponseFilter`.
 This allows the API to log incoming request details such as the HTTP method and URI, and outgoing response details such as the final status code, from one central place.

This approach is beneficial because it:
- reduces code duplication  
- keeps resource classes focused on business logic  
- ensures consistent logging across all endpoints  
- makes future updates to logging easier  

Therefore, using JAX-RS filters provides a cleaner, more maintainable, and scalable solution compared to manually inserting logging statements in every resource method.

