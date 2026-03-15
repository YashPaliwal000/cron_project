package com.scheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scheduler.dto.CreateJobRequest;
import com.scheduler.dto.JobDetailsResponse;
import com.scheduler.dto.JobExecutionResponse;
import com.scheduler.dto.JobSummaryResponse;
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
        Job job = Job.builder()
                .name(request.getName())
                .endpoint(request.getEndpoint())
                .method(request.getMethod())
                .headers(request.getHeaders())
                .payload(request.getPayload())
                .scheduleTime(parseScheduleTime(request.getScheduleTime()))
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        job = jobRepository.save(job);
        schedulerService.scheduleJob(job.getId(), job.getScheduleTime());

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
        if (request.getStatus() != null) job.setStatus(request.getStatus());

        job = jobRepository.save(job);

        // reschedule if needed
        schedulerService.deleteJob(job.getId());
        if ("ACTIVE".equalsIgnoreCase(job.getStatus())) {
            schedulerService.scheduleJob(job.getId(), job.getScheduleTime());
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
        return JobSummaryResponse.builder()
                .id(job.getId())
                .name(job.getName())
                .endpoint(job.getEndpoint())
                .method(job.getMethod())
                .status(job.getStatus())
                .nextRun(job.getScheduleTime()) // assuming scheduleTime is nextRun
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
}
