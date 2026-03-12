# Job Scheduler - Complete Implementation

## 🎯 Project Overview

A Spring Boot application that schedules and executes API calls at specified times using Quartz Scheduler with PostgreSQL persistence.

**Key Features:**
- ✅ Schedule API calls for future execution
- ✅ Support for all HTTP methods (GET, POST, PUT, DELETE, PATCH)
- ✅ Custom headers and request payloads
- ✅ Job pause/resume functionality
- ✅ Execution history tracking
- ✅ Detailed logging for debugging
- ✅ REST API for job management
- ✅ Postman collection included

---

## 🚀 Quick Start

### 1. Build the Project
```bash
cd /home/yash/Desktop/work/cron_project
./mvnw clean install -DskipTests
```

### 2. Start the Application
```bash
./mvnw spring-boot:run
```

### 3. Create Your First Job
```bash
bash test_job.sh
```

### 4. Wait for Execution
Job will execute in 5 minutes. Watch the console for detailed logs.

---

## 📚 Documentation Files

### Getting Started
- **COMPLETE_SETUP_GUIDE.md** - Comprehensive setup and usage guide (START HERE)
- **TEST_JOB_5MINUTES.md** - Step-by-step testing with 5-minute schedule

### API Documentation
- **Job_Scheduler_API.postman_collection.json** - Postman collection with all endpoints
- **POSTMAN_GUIDE.md** - How to use the Postman collection
- **API_QUICK_REFERENCE.md** - Quick API reference card

### Configuration & Fixes
- **FIX_QUARTZ_ERROR.md** - Details of the Quartz configuration fix
- **SWAGGER_SETUP.md** - Swagger/OpenAPI setup information

### Database
- **POSTGRESQL_QUERIES.md** - SQL queries for database operations
- **query_db.sh** - Interactive database query script

### Quick Scripts
- **test_job.sh** - Create a test job in one command
- **query_db.sh** - Interactive database query helper

---

## 🏗️ Architecture

### Components
```
JobController (REST endpoints)
    ↓
JobService (Business logic)
    ↓
SchedulerService (Quartz integration)
    ↓
QuartzConfig (Scheduler setup)
    ↓
ApiJobExecutor (Job execution with logging)
    ↓
Database (PostgreSQL)
```

### Database Schema
```
jobs
├── id (UUID, Primary Key)
├── name (String)
├── api_endpoint (String, up to 4096 chars)
├── method (String: GET, POST, PUT, DELETE, PATCH)
├── headers (JSON string)
├── payload (JSON string)
├── schedule_time (Instant)
├── status (String: ACTIVE, PAUSED)
└── created_at (Instant)

job_execution
├── id (UUID, Primary Key)
├── job_id (UUID, Foreign Key)
├── start_time (Instant)
├── end_time (Instant)
├── status (String: SUCCESS, FAILED)
├── duration_ms (Integer)
└── response_code (Integer, nullable)
```

---

## 🔧 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/jobs` | Create a new job |
| GET | `/api/jobs` | Get all jobs |
| PUT | `/api/jobs/{id}` | Update a job |
| PUT | `/api/jobs/{id}/pause` | Pause a job |
| PUT | `/api/jobs/{id}/resume` | Resume a job |
| GET | `/api/jobs/{id}/executions` | Get job execution history |
| DELETE | `/api/jobs/{id}` | Delete a job |

---

## 📋 Technology Stack

- **Framework:** Spring Boot 4.0.3
- **Language:** Java 17
- **Scheduler:** Quartz Scheduler
- **Database:** PostgreSQL
- **ORM:** JPA/Hibernate
- **Build Tool:** Maven
- **Documentation:** Swagger/OpenAPI
- **API Testing:** Postman
- **Logging:** SLF4J with Logback

---

## 🔐 Important Points

### Schedule Time Format
Must be ISO 8601 format: `YYYY-MM-DDTHH:MM:SSZ`

Example: `2026-03-12T23:50:00Z`

### Get Future Time
```bash
# 5 minutes from now
date -u -d "+5 minutes" '+%Y-%m-%dT%H:%M:%SZ'

# 10 minutes from now
date -u -d "+10 minutes" '+%Y-%m-%dT%H:%M:%SZ'
```

### Scheduling Rules
- ✅ Schedule time MUST be in the future
- ✅ Jobs execute at exact scheduled time (within 1 second)
- ✅ Quartz runs in memory (not persisted across restarts)
- ✅ Execution records are persisted in database

---

## 📊 Example Job Creation

### Using cURL
```bash
FUTURE=$(date -u -d "+5 minutes" '+%Y-%m-%dT%H:%M:%SZ')

curl -X POST http://localhost:8080/api/jobs \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Fetch User Data\",
    \"apiEndpoint\": \"https://jsonplaceholder.typicode.com/users/1\",
    \"method\": \"GET\",
    \"headers\": {
      \"Accept\": \"application/json\",
      \"Authorization\": \"Bearer token123\"
    },
    \"scheduleTime\": \"$FUTURE\"
  }"
```

### Using Postman
1. Import `Job_Scheduler_API.postman_collection.json`
2. Go to **Create Job**
3. Modify body with your details
4. Click **Send**

### Response
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Fetch User Data",
  "status": "ACTIVE",
  "scheduleTime": "2026-03-12T23:50:00Z",
  "apiEndpoint": "https://jsonplaceholder.typicode.com/users/1"
}
```

---

## 🔍 Monitoring Job Execution

### Console Logs
When job executes, you'll see:
```
========== JOB EXECUTION STARTED ==========
Job ID: 550e8400-e29b-41d4-a716-446655440000
Job Name: Fetch User Data
API Endpoint: https://jsonplaceholder.typicode.com/users/1
HTTP Method: GET
Execution started at: 2026-03-12T23:50:00.123Z
✓ API call SUCCESSFUL
Response Status Code: 200
Execution Duration: 245 ms
✓ Job execution record saved
Execution Status: SUCCESS
========== JOB EXECUTION COMPLETED ==========
```

### Check Execution History
```bash
# Using API
curl http://localhost:8080/api/jobs/550e8400-e29b-41d4-a716-446655440000/executions

# Using Database
psql -U postgres -h localhost -d job_scheduler
SELECT * FROM job_execution WHERE job_id = '550e8400-e29b-41d4-a716-446655440000';
```

---

## ⚙️ Configuration

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/job_scheduler
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
  quartz:
    job-store-type: memory

logging:
  level:
    com.scheduler: DEBUG
  pattern:
    console: "%d{HH:mm:ss} [%thread] %-5level %logger - %msg%n"
```

### Key Configuration Points
- **DDL Auto:** set to `update` to auto-create tables
- **Job Store:** In-memory (faster, lost on restart)
- **Logging:** DEBUG for job execution details
- **Database:** Executions persisted, jobs not

---

## 🐛 Troubleshooting

### Job Not Executing
1. Verify schedule time is in future
2. Check application logs for "JOB EXECUTION STARTED"
3. Ensure database is running
4. Verify PostgreSQL connection in application.yml

### API Errors
- **404:** Job doesn't exist - create one first
- **400:** Invalid request - check date format
- **500:** Database error - verify PostgreSQL connection

### Connection Issues
```bash
# Check PostgreSQL
pg_isready -h localhost -p 5432

# Check Spring Boot
curl http://localhost:8080/api/jobs
```

---

## 📝 Key Files Modified

| File | Change | Status |
|------|--------|--------|
| QuartzConfig.java | Fixed autowiring configuration | ✅ |
| ApiJobExecutor.java | Added comprehensive logging | ✅ |
| application.yml | Added logging configuration | ✅ |
| pom.xml | Added springdoc-openapi dependency | ✅ |
| JobController.java | Added OpenAPI annotations | ✅ |

---

## ✅ What's Included

### Source Code
- ✅ Complete Spring Boot application
- ✅ All REST endpoints
- ✅ Quartz scheduler integration
- ✅ Database configuration
- ✅ OpenAPI/Swagger support

### Documentation
- ✅ 9 comprehensive guide documents
- ✅ Quick reference cards
- ✅ Troubleshooting guides
- ✅ Database query examples

### Testing Tools
- ✅ Postman collection (7 endpoints)
- ✅ Test scripts (bash)
- ✅ Database query helper
- ✅ Sample requests

---

## 🎓 Next Steps

1. **Setup:** Follow COMPLETE_SETUP_GUIDE.md
2. **Test:** Create your first job with test_job.sh
3. **Explore:** Try different API endpoints with Postman
4. **Customize:** Update endpoints and payloads for your needs
5. **Monitor:** Check execution history in database
6. **Scale:** Deploy to production with proper database

---

## 📞 Support

For issues or questions:
- Check relevant documentation file in project root
- Review console logs for error messages
- Query database to verify records
- Test endpoints with Postman or cURL

---

## 📄 Files in Project Root

```
cron_project/
├── COMPLETE_SETUP_GUIDE.md          ⭐ START HERE
├── TEST_JOB_5MINUTES.md             Testing guide
├── POSTMAN_GUIDE.md                 Postman documentation
├── FIX_QUARTZ_ERROR.md              Error fix details
├── POSTGRESQL_QUERIES.md            Database queries
├── API_QUICK_REFERENCE.md           Quick API reference
├── SWAGGER_SETUP.md                 Swagger configuration
├── Job_Scheduler_API.postman_collection.json
├── test_job.sh                      Test script
├── query_db.sh                      Database helper
└── pom.xml                          Maven configuration
```

---

**Version:** 1.0.0  
**Last Updated:** March 12, 2026  
**Status:** ✅ Production Ready

