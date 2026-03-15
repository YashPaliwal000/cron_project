package com.scheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scheduler.dto.CreateJobRequest;
import com.scheduler.dto.JobDetailsResponse;
import com.scheduler.dto.JobExecutionResponse;
import com.scheduler.dto.JobSummaryResponse;
import com.scheduler.dto.ScheduleConfig;
import com.scheduler.dto.UpdateJobRequest;
import com.scheduler.entity.Job;
import com.scheduler.entity.JobExecution;
import com.scheduler.repository.JobExecutionRepository;
import com.scheduler.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobExecutionRepository jobExecutionRepository;
    private final SchedulerService schedulerService;
    private final ObjectMapper objectMapper;

    public JobDetailsResponse createJob(CreateJobRequest request) throws SchedulerException, JsonProcessingException {
        String cronExpression = request.getCronExpression();
        // Treat blank cronExpression as not provided so that scheduleConfig can be used
        if (cronExpression != null && cronExpression.isBlank()) {
            cronExpression = null;
        }
        if (cronExpression == null && request.getScheduleConfig() != null) {
            cronExpression = generateCronExpression(request.getScheduleConfig());
        }

        Job job = Job.builder()
                .name(request.getName())
                .endpoint(request.getEndpoint())
                .method(request.getMethod())
                .headers(request.getHeaders())
                .payload(request.getPayload())
                .scheduleTime(parseScheduleTime(request.getScheduleTime()))
                .scheduleType(request.getScheduleType() != null ? request.getScheduleType() : "ONE_TIME")
                .cronExpression(cronExpression)
                .timeZone(request.getTimeZone())
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        job = jobRepository.save(job);

        // Schedule based on type
        if ("RECURRING".equals(job.getScheduleType()) && job.getCronExpression() != null) {
            schedulerService.scheduleRecurringJob(job.getId(), job.getCronExpression(), job.getTimeZone());
        } else {
            schedulerService.scheduleJob(job.getId(), job.getScheduleTime());
        }

        return toDetailsResponse(job);
    }

    public JobDetailsResponse updateJob(UUID id, UpdateJobRequest request) throws SchedulerException, JsonProcessingException {
        Job job = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));

        if (request.getName() != null) job.setName(request.getName());
        if (request.getEndpoint() != null) job.setEndpoint(request.getEndpoint());
        if (request.getMethod() != null) job.setMethod(request.getMethod());
        if (request.getHeaders() != null) job.setHeaders(request.getHeaders());
        if (request.getPayload() != null) job.setPayload(request.getPayload());
        if (request.getScheduleTime() != null) job.setScheduleTime(parseScheduleTime(request.getScheduleTime()));
        if (request.getScheduleType() != null) job.setScheduleType(request.getScheduleType());
        if (request.getCronExpression() != null) {
            // Treat blank cronExpression in update as "clear and regenerate from scheduleConfig if provided"
            String cron = request.getCronExpression().isBlank() ? null : request.getCronExpression();
            job.setCronExpression(cron);
        }
        if (request.getTimeZone() != null) job.setTimeZone(request.getTimeZone());
        if (request.getStatus() != null) job.setStatus(request.getStatus());

        // Generate cron if needed
        if (job.getCronExpression() == null && request.getScheduleConfig() != null) {
            job.setCronExpression(generateCronExpression(request.getScheduleConfig()));
        }

        job = jobRepository.save(job);

        // reschedule if needed
        schedulerService.deleteJob(job.getId());
        if ("ACTIVE".equalsIgnoreCase(job.getStatus())) {
            if ("RECURRING".equals(job.getScheduleType()) && job.getCronExpression() != null) {
                schedulerService.scheduleRecurringJob(job.getId(), job.getCronExpression(), job.getTimeZone());
            } else {
                schedulerService.scheduleJob(job.getId(), job.getScheduleTime());
            }
        }

        return toDetailsResponse(job);
    }

    public JobDetailsResponse getJobDetails(UUID id) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
        return toDetailsResponse(job);
    }

    public List<JobExecutionResponse> getJobHistory(UUID jobId) {
        List<JobExecution> executions = jobExecutionRepository.findByJobId(jobId);
        return executions.stream()
                .map(exec -> JobExecutionResponse.builder()
                        .id(exec.getId())
                        .executedAt(exec.getStartTime())
                        .durationMs(exec.getDurationMs())
                        .responseCode(exec.getResponseCode())
                        .status(exec.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    public List<JobSummaryResponse> getAllJobs() {
        return jobRepository.findAll().stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    public void pauseJob(UUID id) throws SchedulerException {
        schedulerService.pauseJob(id);
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus("PAUSED");
            jobRepository.save(job);
        });
    }

    public void resumeJob(UUID id) throws SchedulerException {
        schedulerService.resumeJob(id);
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus("ACTIVE");
            jobRepository.save(job);
        });
    }

    public void deleteJob(UUID id) throws SchedulerException {
        schedulerService.deleteJob(id);
        jobRepository.deleteById(id);
    }

    private JobSummaryResponse toSummaryResponse(Job job) {
        // Default to stored scheduleTime
        Instant nextRun = job.getScheduleTime();
        // Try to use Quartz's computed next fire time if available
        try {
            Instant schedulerNext = schedulerService.getNextRunTime(job.getId());
            if (schedulerNext != null) {
                nextRun = schedulerNext;
            }
        } catch (SchedulerException e) {
            // If scheduler lookup fails, fall back to scheduleTime without breaking the response
        }

        return JobSummaryResponse.builder()
                .id(job.getId())
                .name(job.getName())
                .endpoint(job.getEndpoint())
                .method(job.getMethod())
                .status(job.getStatus())
                .nextRun(nextRun)
                .lastStatus(job.getLastRunStatus())
                .build();
    }

    private JobDetailsResponse toDetailsResponse(Job job) {
        try {
            return JobDetailsResponse.builder()
                    .id(job.getId())
                    .name(job.getName())
                    .endpoint(job.getEndpoint())
                    .method(job.getMethod())
                    .status(job.getStatus())
                    .scheduleTime(job.getScheduleTime())
                    .scheduleType(job.getScheduleType())
                    .cronExpression(job.getCronExpression())
                    .timeZone(job.getTimeZone())
                    .headers(job.getHeaders() != null && !job.getHeaders().isEmpty() ? objectMapper.readValue(job.getHeaders(), Map.class) : null)
                    .payload(job.getPayload() != null && !job.getPayload().isEmpty() ? objectMapper.readValue(job.getPayload(), Map.class) : null)
                    .createdAt(job.getCreatedAt())
                    .lastRunTime(job.getLastRunTime())
                    .lastRunStatus(job.getLastRunStatus())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
    }

    private Instant parseScheduleTime(String scheduleTimeStr) {
        if (scheduleTimeStr == null || scheduleTimeStr.isEmpty()) {
            return null;
        }
        if (scheduleTimeStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}")) {
            scheduleTimeStr += ":00";
        }
        LocalDateTime ldt = LocalDateTime.parse(scheduleTimeStr);
        return ldt.atZone(ZoneId.systemDefault()).toInstant();
    }

    private String generateCronExpression(ScheduleConfig config) {
        String frequency = config.getFrequency();
        List<String> times = config.getTimes();
        List<String> daysOfWeek = config.getDaysOfWeek();
        List<Integer> daysOfMonth = config.getDaysOfMonth();

        if ("DAILY".equals(frequency) && times != null && !times.isEmpty()) {
            // e.g., times: ["09:00", "13:00"] -> "0 0 9,13 * * ?"
            String hours = times.stream()
                    .map(t -> t.split(":")[0])
                    .collect(Collectors.joining(","));
            String minutes = times.stream()
                    .map(t -> t.split(":")[1])
                    .collect(Collectors.joining(","));
            return "0 " + minutes + " " + hours + " * * ?";
        } else if ("WEEKLY".equals(frequency) && times != null && !times.isEmpty() && daysOfWeek != null && !daysOfWeek.isEmpty()) {
            // e.g., times: ["10:00"], daysOfWeek: ["MON", "WED"] -> "0 0 10 ? * MON,WED"
            String hour = times.get(0).split(":")[0];
            String minute = times.get(0).split(":")[1];
            String days = String.join(",", daysOfWeek);
            return "0 " + minute + " " + hour + " ? * " + days;
        } else if ("MONTHLY".equals(frequency) && times != null && !times.isEmpty() && daysOfMonth != null && !daysOfMonth.isEmpty()) {
            // e.g., times: ["10:00"], daysOfMonth: [1, 15] -> "0 0 10 1,15 * ?"
            String hour = times.get(0).split(":")[0];
            String minute = times.get(0).split(":")[1];
            String days = daysOfMonth.stream().map(String::valueOf).collect(Collectors.joining(","));
            return "0 " + minute + " " + hour + " " + days + " * ?";
        }
        // Add more cases as needed
        return null;
    }
}
