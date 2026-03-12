# Job Scheduler API - Postman Collection Guide

## Overview
This guide explains how to import and use the Postman collection for the Job Scheduler API.

## Files
- **Job_Scheduler_API.postman_collection.json** - Complete Postman collection with all API endpoints

## How to Import the Collection

### Step 1: Open Postman
- Launch Postman application

### Step 2: Import Collection
1. Click the **Import** button (top left corner)
2. Select the **File** tab
3. Click **Upload Files**
4. Navigate to and select `Job_Scheduler_API.postman_collection.json`
5. Click **Import**

## Environment Variables

The collection uses the following variables (auto-configured):
- `baseUrl`: `http://localhost:8080`
- `jobId`: Auto-populated when creating a job

## API Endpoints Overview

### 1. **Create Job** - POST `/api/jobs`
Creates a new scheduled job.

**Parameters:**
- `name` (required): Job name
- `apiEndpoint` (required): API URL to call
- `method` (required): HTTP method (GET, POST, PUT, DELETE, PATCH)
- `headers` (optional): Request headers as JSON
- `payload` (optional): Request body as JSON
- `scheduleTime` (required): ISO 8601 format timestamp

**Example Request:**
```json
{
  "name": "Fetch User Data",
  "apiEndpoint": "https://jsonplaceholder.typicode.com/users/1",
  "method": "GET",
  "headers": {
    "Authorization": "Bearer token123"
  },
  "scheduleTime": "2026-03-15T10:30:00Z"
}
```

**Success Response (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Fetch User Data",
  "status": "ACTIVE",
  "scheduleTime": "2026-03-15T10:30:00Z",
  "apiEndpoint": "https://jsonplaceholder.typicode.com/users/1"
}
```

---

### 2. **Get All Jobs** - GET `/api/jobs`
Retrieves a list of all scheduled jobs.

**Response (200):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Fetch User Data",
    "status": "ACTIVE",
    "scheduleTime": "2026-03-15T10:30:00Z",
    "apiEndpoint": "https://jsonplaceholder.typicode.com/users/1"
  }
]
```

---

### 3. **Update Job** - PUT `/api/jobs/{id}`
Updates an existing job configuration.

**Parameters:** Same as Create Job (all optional)

**Note:** Use the auto-populated `{{jobId}}` variable in the URL.

---

### 4. **Pause Job** - PUT `/api/jobs/{id}/pause`
Pauses an active job (stops scheduled execution).

**Response (200):** No content

---

### 5. **Resume Job** - PUT `/api/jobs/{id}/resume`
Resumes a paused job.

**Response (200):** No content

---

### 6. **Get Job Executions** - GET `/api/jobs/{id}/executions`
Retrieves execution history for a specific job.

**Response (200):**
```json
[
  {
    "startTime": "2026-03-15T10:30:00.000Z",
    "endTime": "2026-03-15T10:30:05.123Z",
    "status": "SUCCESS",
    "durationMs": 5123
  },
  {
    "startTime": "2026-03-15T11:30:00.000Z",
    "endTime": "2026-03-15T11:30:02.456Z",
    "status": "FAILED",
    "durationMs": 2456
  }
]
```

**Fields:**
- `startTime`: When execution started
- `endTime`: When execution completed
- `status`: SUCCESS or FAILED
- `durationMs`: Execution duration in milliseconds

---

### 7. **Delete Job** - DELETE `/api/jobs/{id}`
Permanently deletes a job.

**Response (204):** No content

---

## Recommended Test Workflow

Follow this sequence to test all features:

1. **Create Job** - Create a new job
   - Job ID is automatically saved to `{{jobId}}`
2. **Get All Jobs** - Verify job appears in list
3. **Pause Job** - Pause the job using `{{jobId}}`
4. **Resume Job** - Resume execution
5. **Get Job Executions** - Check execution history
6. **Update Job** - Modify job details
7. **Delete Job** - Clean up

## Important Notes

### Date/Time Format
- Use ISO 8601 format: `YYYY-MM-DDTHH:mm:ssZ`
- Example: `2026-03-15T10:30:00Z`

### HTTP Methods
Valid methods:
- `GET` - Retrieve data
- `POST` - Create data
- `PUT` - Update data
- `DELETE` - Delete data
- `PATCH` - Partial update

### Job Status Values
- `ACTIVE` - Job is scheduled and will execute
- `PAUSED` - Job won't execute until resumed

### Execution Status
- `SUCCESS` - Job executed without errors
- `FAILED` - Job encountered an error

## Troubleshooting

### Issue: "Job not found" (404)
**Solution:** Ensure you created a job first and the correct ID is being used

### Issue: "Invalid schedule time" (400)
**Solution:** Use ISO 8601 format: `2026-03-15T10:30:00Z`

### Issue: "Connection refused"
**Solution:** Ensure Spring Boot application is running:
```bash
./mvnw spring-boot:run
```

### Issue: Database errors (500)
**Solution:** Verify PostgreSQL is running and database `job_scheduler` exists

## Additional Resources

- **Swagger UI**: http://localhost:8080/swagger-ui/
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

