package com.scheduler.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scheduler.dto.CreateJobRequest;
import com.scheduler.dto.JobExecutionResponse;
import com.scheduler.dto.JobResponse;
import com.scheduler.dto.UpdateJobRequest;
import com.scheduler.service.JobService;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Validated @RequestBody CreateJobRequest request)
            throws SchedulerException, JsonProcessingException {
        JobResponse response = jobService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<JobResponse> getAllJobs() {
        return jobService.getAllJobs();
    }

    @PutMapping("/{id}")
    public JobResponse updateJob(@PathVariable UUID id, @RequestBody UpdateJobRequest request)
            throws SchedulerException, JsonProcessingException {
        return jobService.updateJob(id, request);
    }

    @PutMapping("/{id}/pause")
    public void pauseJob(@PathVariable UUID id) throws SchedulerException {
        jobService.pauseJob(id);
    }

    @PutMapping("/{id}/resume")
    public void resumeJob(@PathVariable UUID id) throws SchedulerException {
        jobService.resumeJob(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable UUID id) throws SchedulerException {
        jobService.deleteJob(id);
    }

    @GetMapping("/{id}/executions")
    public List<JobExecutionResponse> getJobExecutions(@PathVariable UUID id) {
        return jobService.getJobExecutions(id);
    }
}
