## Job Scheduler API - Quick Reference

### Import Collection
- File: `Job_Scheduler_API.postman_collection.json`
- In Postman: Import → File → Select JSON file

---

### Endpoints Quick Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/jobs` | Create new job |
| GET | `/api/jobs` | Get all jobs |
| PUT | `/api/jobs/{id}` | Update job |
| PUT | `/api/jobs/{id}/pause` | Pause job |
| PUT | `/api/jobs/{id}/resume` | Resume job |
| GET | `/api/jobs/{id}/executions` | Get executions |
| DELETE | `/api/jobs/{id}` | Delete job |

---

### Create Job Request
```json
{
  "name": "Job Name",
  "apiEndpoint": "https://api.example.com/endpoint",
  "method": "GET",
  "headers": {
    "Authorization": "Bearer token"
  },
  "payload": {
    "key": "value"
  },
  "scheduleTime": "2026-03-15T10:30:00Z"
}
```

---

### Environment Variables
- `baseUrl`: http://localhost:8080
- `jobId`: Auto-set after Create Job (use as {{jobId}})

---

### Status Values
- **Job Status**: ACTIVE, PAUSED
- **Execution Status**: SUCCESS, FAILED

---

### Date Format
ISO 8601: `YYYY-MM-DDTHH:mm:ssZ`

Example: `2026-03-15T10:30:00Z`

---

### Test Sequence
1. POST Create Job → jobId saved automatically
2. GET All Jobs
3. PUT Pause Job ({{jobId}})
4. PUT Resume Job ({{jobId}})
5. GET Executions ({{jobId}})
6. PUT Update Job ({{jobId}})
7. DELETE Job ({{jobId}})

---

### Response Codes
- 200 OK - Success
- 201 Created - Job created
- 204 No Content - Success with no body
- 400 Bad Request - Invalid input
- 404 Not Found - Job doesn't exist
- 500 Internal Error - Server error

---

### Useful Links
- Swagger UI: http://localhost:8080/swagger-ui/
- API Docs: http://localhost:8080/v3/api-docs

