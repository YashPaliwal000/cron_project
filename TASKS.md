## Job Scheduler Backend - Task Tracker

- [x] Scaffold Spring Boot project with required dependencies
- [x] Configure `application.yml` with PostgreSQL and Quartz in-memory store
- [ ] - [x] Define entities: `Job`, `JobExecution`
- [ ] - [x] Create repositories: `JobRepository`, `JobExecutionRepository`
- [x] Implement DTOs for job create/update/response and executions
- [x] Implement `QuartzConfig` for scheduler configuration
- [x] Implement `SchedulerService` (schedule, pause, resume, delete)
- [x] Implement `ApiJobExecutor` for calling external APIs and saving execution logs
- [x] Implement `JobService` business logic
- [x] Implement `JobController` REST endpoints
- [x] Implement `GlobalExceptionHandler`
- [ ] Run basic integration tests with Postman
