# Leave Management REST API

A production-grade, layered **Leave Management REST API** built with **Spring Boot 4.1.1** and **Java 21**, using an in-memory, thread-safe data store. No database or ORM required.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Package Structure](#package-structure)
- [Build & Run](#build--run)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)

---

## Project Overview

The Leave Management API provides a RESTful interface for managing employee leave requests. It supports creating, retrieving, updating, and deleting leave requests with full validation and structured error handling.

**Key Features:**

- Full CRUD operations on leave requests
- Six leave types: `ANNUAL`, `SICK`, `CASUAL`, `MATERNITY`, `PATERNITY`, `UNPAID`
- Four statuses: `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`
- Bean Validation with field-level error messages
- Structured JSON error responses
- Thread-safe in-memory store (`ConcurrentHashMap` + `AtomicLong`)
- Spring profile support (`dev` profile with DEBUG logging)

---

## Technology Stack

| Component          | Version                   |
| ------------------ | ------------------------- |
| Java               | 21                        |
| Spring Boot        | 4.1.1                     |
| Spring Framework   | 7.0.9                     |
| Jakarta Validation | (via starter)             |
| Jackson            | 3.1.5 (tools.jackson)     |
| JUnit              | 6.0.3                     |
| Mockito            | 5.23.0                    |
| Build Tool         | Gradle (Groovy DSL) 9.7.1 |

---

## Architecture

The application follows a strict **clean layered architecture**:

```
HTTP Request
     │
     ▼
┌─────────────────┐
│   Controller    │  ← @RestController, input validation (@Valid)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Service      │  ← Business logic, date validation, exception handling
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Repository    │  ← ConcurrentHashMap storage, AtomicLong ID generation
└─────────────────┘
```

Cross-cutting concerns are handled by:

- **`GlobalExceptionHandler`** (`@RestControllerAdvice`) — maps exceptions to HTTP responses
- **`ErrorResponse`** DTO — consistent JSON error shape across all errors

---

## Package Structure

```
src/main/java/com/hr/leave_management/
│
├── LeaveManagementApplication.java          # Spring Boot entry point
│
├── model/
│   ├── LeaveRequest.java                    # Entity with full getters/setters/equals/hashCode
│   ├── LeaveType.java                       # Enum: ANNUAL, SICK, CASUAL, MATERNITY, PATERNITY, UNPAID
│   └── LeaveStatus.java                     # Enum: PENDING, APPROVED, REJECTED, CANCELLED
│
├── dto/
│   ├── CreateLeaveRequestDto.java           # Input DTO with Bean Validation annotations
│   ├── UpdateLeaveRequestDto.java           # Update DTO (all fields optional for PUT)
│   └── LeaveResponseDto.java               # Safe outbound response representation
│
├── repository/
│   ├── LeaveRepository.java                 # Interface: save, findById, findAll, deleteById, existsById
│   └── InMemoryLeaveRepository.java         # @Repository: ConcurrentHashMap + AtomicLong
│
├── service/
│   ├── LeaveService.java                    # Interface: CRUD operations
│   └── LeaveServiceImpl.java               # @Service: constructor injection, business logic
│
├── controller/
│   └── LeaveController.java                 # @RestController on /leaves
│
└── exception/
    ├── ResourceNotFoundException.java        # 404 trigger
    ├── BadRequestException.java             # 400 trigger (business rule violations)
    ├── ErrorResponse.java                   # Structured error response DTO
    └── GlobalExceptionHandler.java          # @RestControllerAdvice

src/main/resources/
├── application.properties                   # Base config (name, active profile)
└── application-dev.properties               # Dev profile (DEBUG logging, port 8080)

src/test/java/com/hr/leave_management/
├── LeaveManagementApplicationTests.java     # Spring context load test
├── service/
│   └── LeaveServiceTest.java                # Unit tests: JUnit 5 + Mockito
└── controller/
    └── LeaveControllerTest.java             # Slice tests: @WebMvcTest + MockMvc
```

---

## Build & Run

### Prerequisites

- Java 21+
- Internet connection (for Gradle dependency download on first run)

### Build

```bash
./gradlew build
```

### Run Tests

```bash
./gradlew test
```

### Run Application

```bash
./gradlew bootRun
```

The application starts on **port 8080** with the `dev` profile active (DEBUG logging for `com.hr.leave_management`).

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::  (v4.1.1)

Started LeaveManagementApplication in ~1.3 seconds
```

---

## API Endpoints

Base URL: `http://localhost:8080`

### Data Models

**Leave Types:** `ANNUAL`, `SICK`, `CASUAL`, `MATERNITY`, `PATERNITY`, `UNPAID`

**Leave Statuses:** `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`

---

### `GET /leaves` — List All Leave Requests

Returns all leave requests stored in memory.

```bash
curl -X GET http://localhost:8080/leaves \
  -H "Accept: application/json"
```

**Response `200 OK`:**

```json
[
  {
    "id": 1,
    "employeeId": "EMP001",
    "leaveType": "ANNUAL",
    "startDate": "2026-10-01",
    "endDate": "2026-10-05",
    "reason": "Family vacation",
    "status": "PENDING",
    "createdAt": "2026-09-28T10:00:00",
    "updatedAt": "2026-09-28T10:00:00"
  }
]
```

---

### `GET /leaves/{id}` — Get Leave Request by ID

```bash
curl -X GET http://localhost:8080/leaves/1 \
  -H "Accept: application/json"
```

**Response `200 OK`:**

```json
{
  "id": 1,
  "employeeId": "EMP001",
  "leaveType": "ANNUAL",
  "startDate": "2026-10-01",
  "endDate": "2026-10-05",
  "reason": "Family vacation",
  "status": "PENDING",
  "createdAt": "2026-09-28T10:00:00",
  "updatedAt": "2026-09-28T10:00:00"
}
```

**Response `404 Not Found`:**

```json
{
  "timestamp": "2026-09-28T10:05:00",
  "status": 404,
  "error": "Not Found",
  "message": "LeaveRequest not found with id: 99",
  "path": "/leaves/99"
}
```

---

### `POST /leaves` — Create a Leave Request

```bash
curl -X POST http://localhost:8080/leaves \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "EMP001",
    "leaveType": "ANNUAL",
    "startDate": "2026-10-01",
    "endDate": "2026-10-05",
    "reason": "Family vacation"
  }'
```

**Validation Rules:**

- `employeeId` — required, non-blank
- `leaveType` — required, one of the `LeaveType` enum values
- `startDate` — required, today or in the future
- `endDate` — required, must be on or after `startDate`
- `reason` — required, non-blank, max 255 characters

**Response `201 Created`:**

```json
{
  "id": 1,
  "employeeId": "EMP001",
  "leaveType": "ANNUAL",
  "startDate": "2026-10-01",
  "endDate": "2026-10-05",
  "reason": "Family vacation",
  "status": "PENDING",
  "createdAt": "2026-09-28T10:00:00",
  "updatedAt": "2026-09-28T10:00:00"
}
```

**Response `400 Bad Request` (validation failure):**

```json
{
  "timestamp": "2026-09-28T10:05:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/leaves",
  "fieldErrors": {
    "employeeId": ["Employee ID must not be blank"],
    "startDate": ["Start date must be today or in the future"]
  }
}
```

---

### `PUT /leaves/{id}` — Update a Leave Request

All fields are optional. Only provided (non-null) fields will be updated.

```bash
curl -X PUT http://localhost:8080/leaves/1 \
  -H "Content-Type: application/json" \
  -d '{
    "leaveType": "SICK",
    "reason": "Medical appointment",
    "status": "APPROVED"
  }'
```

**Response `200 OK`:**

```json
{
  "id": 1,
  "employeeId": "EMP001",
  "leaveType": "SICK",
  "startDate": "2026-10-01",
  "endDate": "2026-10-05",
  "reason": "Medical appointment",
  "status": "APPROVED",
  "createdAt": "2026-09-28T10:00:00",
  "updatedAt": "2026-09-28T10:10:00"
}
```

---

### `DELETE /leaves/{id}` — Delete a Leave Request

```bash
curl -X DELETE http://localhost:8080/leaves/1
```

**Response `204 No Content`** (empty body on success)

**Response `404 Not Found`** if the ID does not exist.

---

## Testing

### Run All Tests

```bash
./gradlew test
```

Test reports are generated at:

```
build/reports/tests/test/index.html
```

### Test Coverage

#### Service Unit Tests (`LeaveServiceTest`)

Uses **JUnit 5** + **Mockito** (`@ExtendWith(MockitoExtension.class)`) with a mocked `LeaveRepository`.

| Test                                  | Scenario                                             |
| ------------------------------------- | ---------------------------------------------------- |
| `createLeave_success`                 | Saves leave and returns response DTO                 |
| `createLeave_invalidDateRange_throws` | `endDate` before `startDate` → `BadRequestException` |
| `getLeaveById_found`                  | Returns DTO for existing ID                          |
| `getLeaveById_notFound`               | Missing ID → `ResourceNotFoundException`             |
| `updateLeave_success`                 | Updates existing leave, returns updated DTO          |
| `updateLeave_notFound`                | Missing ID → `ResourceNotFoundException`             |
| `deleteLeave_success`                 | Removes existing leave                               |
| `deleteLeave_notFound`                | Missing ID → `ResourceNotFoundException`             |
| `getAllLeaves_returnsList`            | Returns list of all DTOs                             |

#### Controller Slice Tests (`LeaveControllerTest`)

Uses **`@WebMvcTest`** + **`MockMvc`** + **`@MockitoBean`** for the service layer.

| Test                                       | Endpoint                 | Expected Status   |
| ------------------------------------------ | ------------------------ | ----------------- |
| `getAllLeaves_returns200`                  | `GET /leaves`            | `200 OK`          |
| `getLeaveById_returns200`                  | `GET /leaves/1`          | `200 OK`          |
| `getLeaveById_returns404`                  | `GET /leaves/99`         | `404 Not Found`   |
| `createLeave_returns201`                   | `POST /leaves` (valid)   | `201 Created`     |
| `createLeave_missingEmployeeId_returns400` | `POST /leaves` (invalid) | `400 Bad Request` |
| `updateLeave_returns200`                   | `PUT /leaves/1`          | `200 OK`          |
| `deleteLeave_returns204`                   | `DELETE /leaves/1`       | `204 No Content`  |
| `deleteLeave_returns404`                   | `DELETE /leaves/99`      | `404 Not Found`   |

### Clean Build

```bash
./gradlew clean test build
```
