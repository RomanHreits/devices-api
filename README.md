# devices-api — quick test & run guide

This short README explains the minimal requirements and commands to run unit tests, 
hot-start functional tests, start the application with Docker Compose (building from the repository Dockerfile), 
and use Swagger UI to test the API.

## Prerequisites
- JDK 21
- ./gradlew (Gradle wrapper included)
- Docker Engine & docker-compose (optional, required for Docker-based start)

## Test requirements
- Unit tests: use Gradle `test` task (JUnit + Spring test libs are provided by the project)
- Functional tests: the project defines a `functionalTest` source set (run with Gradle). 

## Quick commands

1) **Run unit tests**

```bash
# runs unit tests
./gradlew test
```

2) **Run functional tests (hot start by Gradle)**

```bash
# runs the functionalTest source set; ensure functional test resources are available
./gradlew functionalTest
```

Run a single functional test class

```bash
./gradlew functionalTest --tests "com.example.devicesapi.controller.DeviceControllerFunctionalTests"
```

3) **Build and run the app (Docker Compose using the repository Dockerfile)**

- Build the Spring Boot jar (optional; Compose can build image from Dockerfile)

```bash
./gradlew clean build
```

- Start services with docker-compose (build app image from Dockerfile)

```bash
# from repository root
docker compose up --build -d
```

- Stop and remove

```bash
docker compose down -v
```

**Notes:**
- If a host port conflict occurs, change the host-side port mapping in `docker-compose.yml` or stop the process/container that occupies the port.
- If Compose is configured to wait for the DB, the app will start after the DB healthcheck passes.

4) **Use Swagger UI to test the API**

- When the app is running locally (default port 8081 in this README), open the Swagger UI:

  http://localhost:8081/swagger-ui.html
  or
  http://localhost:8081/swagger-ui/index.html

- OpenAPI JSON is available at:

  http://localhost:8081/v3/api-docs

**Tips for testing with Swagger**
- Use the `Try it out` button to send requests and view responses.
- For endpoints that expect enums or specific strings, provide the exact value (case-sensitive) unless your controller accepts case-insensitive values.
- For secured endpoints, ensure you set authorization headers in the Swagger UI (if security is configured).

## API request & response examples

Base URL: http://localhost:8081
Common state values: available, in-use, inactive

1) Create device (POST /devices)

Request:

```bash
curl -i -X POST "http://localhost:8081/devices" \
  -H "Content-Type: application/json" \
  -d '{"name":"Device A","brand":"newBrand"}'
```

Success response (201 Created):

```json
{
  "id": 1,
  "name": "Device A",
  "brand": "newBrand",
  "state": "inactive",
  "createdAt": "2025-11-24T12:00:00Z"
}
```

Validation failure (400 Bad Request)

Request that triggers validation (missing required `name`):

```bash
curl -i -X POST "http://localhost:8081/devices" \
  -H "Content-Type: application/json" \
  -d '{}'
```

Response example:

```json
{
  "message": "Validation failed",
  "details": "name: must not be blank"
}
```

Duplicate (409 Conflict)

Curl to reproduce (try creating device when one with same name and brand exists):

```bash
curl -i -X POST "http://localhost:8081/devices" \
  -H "Content-Type: application/json" \
  -d '{"name":"Device A","brand":"newBrand"}'
```

Response example:

```json
{
  "message": "Database constraint violation",
  "details": "Device with the same name and brand already exists"
}
```

2) Get all devices (GET /devices)

Request:

```bash
curl -i "http://localhost:8081/devices"
```

Success response (200 OK):

```json
[
  {
    "id": 1,
    "name": "Device A",
    "brand": "newBrand",
    "state": "inactive",
    "createdAt": "2025-11-24T12:00:00Z"
  }
]
```

3) Get device by id (GET /devices/{id})

Request (existing id):

```bash
curl -i "http://localhost:8081/devices/1"
```

Success response (200 OK): single object as above.

Not found (404) — reproduce by requesting non-existing id:

```bash
curl -i "http://localhost:8081/devices/99999"
```

Response example:

```json
{
  "message": "Resource not found",
  "details": "Device not found with id: 99999"
}
```

4) Update device (PUT /devices/{id})

Request (full update):

```bash
curl -i -X PUT "http://localhost:8081/devices/1" \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Device","brand":"Updated Brand","state":"in-use"}'
```

Success response (200 OK) — controller now returns the updated DeviceResponse:

```json
{
  "id": 1,
  "name": "Updated Device",
  "brand": "Updated Brand",
  "state": "in-use",
  "createdAt": null
}
```

Error responses follow the structured error bodies shown above (400/404/409/423 as applicable).

5) Partial update (PATCH /devices/{id})

Request (change only state):

```bash
curl -i -X PATCH "http://localhost:8081/devices/1" \
  -H "Content-Type: application/json" \
  -d '{"brand":"updatedBrand","state":"available"}'
```

Blocked / locked when device is IN_USE (423 Locked) — reproduce by attempting to partially update a device whose state is IN_USE:

Response example:

```json
{
  "message": "Resource is blocked",
  "details": "Cannot update a device that is currently IN_USE"
}
```

6) Update (PUT /devices/{id})

```bash
curl -i -X PUT "http://localhost:8081/devices/1" \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Device","brand":"Updated Brand","state":"inactive"}'
```

Blocked / locked when device is IN_USE (423 Locked) — reproduce by attempting to update a device whose state is IN_USE:

Response example:

```json
{
  "message": "Resource is blocked",
  "details": "Cannot update a device that is currently IN_USE"
}
```

7) Partial update (PATCH /devices/{id})

Invalid enum value (400) — reproduce with an invalid state string:

```bash
curl -i -X PATCH "http://localhost:8081/devices/1" \
  -H "Content-Type: application/json" \
  -d '{"state":"invalid-state"}'
```

Response example:

```json
{
  "message": "Validation failed",
  "details": "Unknown state value: invalid-state"
}
```

8) Delete device (DELETE /devices/{id})

Request:

```bash
curl -i -X DELETE "http://localhost:8081/devices/1"
```

Blocked / locked when device is IN_USE (423 Locked) — reproduce by attempting to delete a device whose state is IN_USE:

Response example:

```json
{
  "message": "Resource is blocked",
  "details": "Cannot update a device that is currently IN_USE"
}
```

9) Partial update (PATCH /devices/{id})

```bash
curl -i -X PATCH "http://localhost:8081/devices/1" \
  -H "Content-Type: application/json" \
  -d '{"state":"available"}'
```

Success response (200 OK):

```json
{
  "id": 1,
  "name": "Updated Device",
  "brand": "Updated Brand",
  "state": "available",
  "createdAt": null
}
```

10) Delete device (DELETE /devices/{id})

Request:

```bash
curl -i -X DELETE "http://localhost:8081/devices/1"
```

Success response (200 OK) — controller returns the deleted DeviceResponse (the resource representation at deletion time):

```json
{
  "id": 1,
  "name": "Updated Device",
  "brand": "Updated Brand",
  "state": "availavle",
  "createdAt": "2025-11-24T12:00:00Z"
}
```

11) Get all devices to confirm deletion (GET /devices)

Request:

```bash
curl -i "http://localhost:8081/devices"
```

Success response (200 OK):

```json
[]
```

Notes:
- Some APIs prefer `204 No Content` for successful DELETE requests; 
this service returns the deleted resource for convenience and clarity.

