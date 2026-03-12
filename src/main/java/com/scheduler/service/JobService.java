package com.scheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scheduler.dto.CreateJobRequest;
import com.scheduler.dto.JobExecutionResponse;
import com.scheduler.dto.JobResponse;
import com.scheduler.dto.UpdateJobRequest;
import com.scheduler.entity.Job;
import com.scheduler.entity.JobExecution;
import com.scheduler.repository.JobExecutionRepository;
import com.scheduler.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobExecutionRepository jobExecutionRepository;
    private final SchedulerService schedulerService;
    private final ObjectMapper objectMapper;

    public JobResponse createJob(CreateJobRequest request) throws SchedulerException, JsonProcessingException {
        Job job = Job.builder()
                .name(request.getName())
                .apiEndpoint(request.getApiEndpoint())
                .method(request.getMethod())
                .headers(request.getHeaders() != null ? objectMapper.writeValueAsString(request.getHeaders()) : null)
                .payload(request.getPayload() != null ? objectMapper.writeValueAsString(request.getPayload()) : null)
                .scheduleTime(request.getScheduleTime())
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        job = jobRepository.save(job);
        schedulerService.scheduleJob(job.getId(), job.getScheduleTime());

        return toResponse(job);
    }

    public JobResponse updateJob(UUID id, UpdateJobRequest request) throws SchedulerException, JsonProcessingException {
        Job job = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));

        if (request.getName() != null) job.setName(request.getName());
        if (request.getApiEndpoint() != null) job.setApiEndpoint(request.getApiEndpoint());
        if (request.getMethod() != null) job.setMethod(request.getMethod());
        if (request.getHeaders() != null) job.setHeaders(objectMapper.writeValueAsString(request.getHeaders()));
        if (request.getPayload() != null) job.setPayload(objectMapper.writeValueAsString(request.getPayload()));
        if (request.getScheduleTime() != null) job.setScheduleTime(request.getScheduleTime());
        if (request.getStatus() != null) job.setStatus(request.getStatus());

        job = jobRepository.save(job);

        // reschedule if needed
        schedulerService.deleteJob(job.getId());
        if ("ACTIVE".equalsIgnoreCase(job.getStatus())) {
            schedulerService.scheduleJob(job.getId(), job.getScheduleTime());
        }

        return toResponse(job);
    }

    public void deleteJob(UUID id) throws SchedulerException {
        schedulerService.deleteJob(id);
        jobRepository.deleteById(id);
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

    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<JobExecutionResponse> getJobExecutions(UUID jobId) {
        List<JobExecution> executions = jobExecutionRepository.findByJobId(jobId);
        return executions.stream()
                .map(exec -> JobExecutionResponse.builder()
                        .startTime(exec.getStartTime())
                        .endTime(exec.getEndTime())
                        .status(exec.getStatus())
                        .durationMs(exec.getDurationMs())
                        .build())
                .collect(Collectors.toList());
    }

    private JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .name(job.getName())
                .status(job.getStatus())
                .scheduleTime(job.getScheduleTime())
                .apiEndpoint(job.getApiEndpoint())
                .build();
    }
}
