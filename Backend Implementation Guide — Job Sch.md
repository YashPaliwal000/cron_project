# Backend Implementation Guide — Job Scheduler MVP

This document describes **step-by-step instructions to build the backend** for the Job Scheduler MVP.

Goal:
Allow users to create jobs from UI that trigger an API at a specific time and track execution results.

Core Features:

* Create job
* Update job
* Delete job
* Pause / Resume job
* Trigger API endpoint at scheduled time
* Track job execution history
* Provide dashboard APIs

Tech Stack:

* Backend Framework: Spring Boot
* Scheduler: Quartz Scheduler
* Database: PostgreSQL
* HTTP Client: RestTemplate / WebClient
* ORM: Spring Data JPA
* Build Tool: Maven

---

# 1. Create Spring Boot Project

Use Spring Initializr.

Project Details

Group: `com.scheduler`
Artifact: `job-scheduler`
Name: `job-scheduler`

Dependencies to add:

* Spring Web
* Spring Data JPA
* PostgreSQL Driver
* Quartz Scheduler
* Lombok
* Validation

Folder Structure

```
src/main/java/com/scheduler

config
controller
dto
entity
repository
service
scheduler
executor
util
```

---

# 2. Configure Database

File: `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/job_scheduler
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

  quartz:
    job-store-type: memory
```

For MVP we use **in-memory Quartz storage**.

---

# 3. Database Tables

## Jobs Table

Stores job configuration.

Table: `jobs`

Columns

| column        | type      |
| ------------- | --------- |
| id            | UUID      |
| name          | VARCHAR   |
| api_endpoint  | TEXT      |
| method        | VARCHAR   |
| headers       | JSON      |
| payload       | JSON      |
| schedule_time | TIMESTAMP |
| status        | VARCHAR   |
| created_at    | TIMESTAMP |

---

## Job Execution Table

Table: `job_executions`

| column        | type      |
| ------------- | --------- |
| id            | UUID      |
| job_id        | UUID      |
| start_time    | TIMESTAMP |
| end_time      | TIMESTAMP |
| status        | VARCHAR   |
| response_code | INT       |
| duration_ms   | INT       |
| error_message | TEXT      |

---

# 4. Create Entities

## Job Entity

File: `entity/Job.java`

Fields

```
id
name
apiEndpoint
method
headers
payload
scheduleTime
status
createdAt
```

Status values

```
ACTIVE
PAUSED
```

---

## JobExecution Entity

File: `entity/JobExecution.java`

Fields

```
id
jobId
startTime
endTime
status
responseCode
durationMs
errorMessage
```

Execution Status

```
SUCCESS
FAILED
```

---

# 5. Create Repositories

Use Spring Data JPA.

## JobRepository

File: `repository/JobRepository.java`

Methods

```
findByStatus()
findById()
save()
deleteById()
```

---

## JobExecutionRepository

File: `repository/JobExecutionRepository.java`

Methods

```
findByJobId()
save()
```

---

# 6. Create DTOs

DTOs separate API layer from entity layer.

Folder: `dto`

---

## CreateJobRequest

```
name
apiEndpoint
method
headers
payload
scheduleTime
```

Example JSON

```json
{
  "name": "Daily Report",
  "apiEndpoint": "https://api.company.com/report",
  "method": "POST",
  "headers": {
    "Authorization": "Bearer token"
  },
  "payload": {
    "type": "daily"
  },
  "scheduleTime": "2026-03-15T10:00:00"
}
```

---

## UpdateJobRequest

Fields

```
name
apiEndpoint
method
headers
payload
scheduleTime
status
```

---

## JobResponse

Fields

```
id
name
status
scheduleTime
apiEndpoint
```

---

# 7. Scheduler Configuration

Create Quartz configuration.

File: `config/QuartzConfig.java`

Responsibilities

* Create SchedulerFactoryBean
* Register jobs dynamically
* Manage job triggers

---

# 8. Job Executor

Responsible for calling the external API.

File: `executor/ApiJobExecutor.java`

Responsibilities

1. Fetch job details
2. Call API endpoint
3. Capture response
4. Save execution result

Execution Steps

```
startTime = now
call HTTP API
capture response
endTime = now
duration = end - start
save execution record
```

Use:

```
RestTemplate
or
WebClient
```

---

# 9. Scheduler Service

File: `service/SchedulerService.java`

Responsibilities

* Schedule job
* Pause job
* Resume job
* Delete job

Methods

```
scheduleJob(job)
pauseJob(jobId)
resumeJob(jobId)
deleteJob(jobId)
```

Flow

```
User creates job
↓
Save in DB
↓
Register job in Quartz
```

---

# 10. Job Service

File: `service/JobService.java`

Responsibilities

* Business logic for job management

Methods

```
createJob()
updateJob()
deleteJob()
pauseJob()
resumeJob()
getAllJobs()
getJobExecutions()
```

Flow for Create Job

```
validate request
↓
save job
↓
schedule job
↓
return response
```

---

# 11. Controllers

Folder: `controller`

---

## JobController

Base Path

```
/api/jobs
```

---

### Create Job

Endpoint

```
POST /api/jobs
```

Request

```json
{
  "name": "Email Trigger",
  "apiEndpoint": "https://service/email",
  "method": "POST",
  "scheduleTime": "2026-03-15T09:00:00"
}
```

Response

```
201 CREATED
```

---

### Get All Jobs

```
GET /api/jobs
```

Response

```json
[
  {
    "id": "123",
    "name": "Email Trigger",
    "status": "ACTIVE",
    "scheduleTime": "2026-03-15T09:00:00"
  }
]
```

---

### Update Job

```
PUT /api/jobs/{id}
```

---

### Pause Job

```
PUT /api/jobs/{id}/pause
```

---

### Resume Job

```
PUT /api/jobs/{id}/resume
```

---

### Delete Job

```
DELETE /api/jobs/{id}
```

---

### Job Execution History

```
GET /api/jobs/{id}/executions
```

Response

```json
[
  {
    "startTime": "2026-03-15T09:00:00",
    "endTime": "2026-03-15T09:00:02",
    "status": "SUCCESS",
    "durationMs": 2000
  }
]
```

---

# 12. Job Lifecycle

Job States

```
ACTIVE
PAUSED
RUNNING
SUCCESS
FAILED
```

Lifecycle

```
CREATE
↓
SCHEDULED
↓
RUNNING
↓
SUCCESS / FAILED
```

---

# 13. Dashboard APIs Needed

Frontend will call:

```
GET /jobs
GET /jobs/{id}
GET /jobs/{id}/executions
POST /jobs
PUT /jobs/{id}
PUT /jobs/{id}/pause
PUT /jobs/{id}/resume
DELETE /jobs/{id}
```

---

# 14. Basic Error Handling

Create global exception handler.

File

```
GlobalExceptionHandler.java
```

Handle

```
JobNotFoundException
SchedulerException
ValidationException
```

---

# 15. MVP Limitations

Current MVP supports

* single scheduler instance
* no distributed execution
* no retry mechanism
* no rate limiting

Future improvements may include:

* distributed workers
* queue system
* retry policies
* alerting system
* cron expression support

---

# 16. Development Order

Recommended implementation order

1. Create Spring Boot project
2. Setup PostgreSQL
3. Create entities
4. Create repositories
5. Create DTOs
6. Implement JobService
7. Implement SchedulerService
8. Implement ApiJobExecutor
9. Implement controllers
10. Test with Postman
11. Integrate with frontend
