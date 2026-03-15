package com.scheduler.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scheduler.dto.CreateJobRequest;
import com.scheduler.dto.JobDetailsResponse;
import com.scheduler.dto.JobExecutionResponse;
import com.scheduler.dto.JobSummaryResponse;
import com.scheduler.dto.UpdateJobRequest;
import com.scheduler.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Job Management", description = "APIs for managing scheduled jobs")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @Operation(summary = "Create a new job", description = "Creates a new scheduled job with the specified configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Job created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public ResponseEntity<JobDetailsResponse> createJob(@Validated @RequestBody CreateJobRequest request)
            throws SchedulerException, JsonProcessingException {
        JobDetailsResponse response = jobService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all jobs", description = "Retrieves a list of all scheduled jobs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of jobs retrieved successfully")
    })
    public List<JobSummaryResponse> getAllJobs() {
        return jobService.getAllJobs();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job details", description = "Retrieves full details of a specific job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public JobDetailsResponse getJobDetails(
            @Parameter(description = "Job ID", required = true) @PathVariable UUID id) {
        return jobService.getJobDetails(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a job", description = "Updates an existing job with the specified ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job updated successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public JobDetailsResponse updateJob(
            @Parameter(description = "Job ID", required = true) @PathVariable UUID id,
            @RequestBody UpdateJobRequest request)
            throws SchedulerException, JsonProcessingException {
        return jobService.updateJob(id, request);
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "Pause a job", description = "Pauses an active job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job paused successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public void pauseJob(
            @Parameter(description = "Job ID", required = true) @PathVariable UUID id)
            throws SchedulerException {
        jobService.pauseJob(id);
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "Resume a job", description = "Resumes a paused job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job resumed successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public void resumeJob(
            @Parameter(description = "Job ID", required = true) @PathVariable UUID id)
            throws SchedulerException {
        jobService.resumeJob(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a job", description = "Deletes a job and stops its execution")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Job deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public void deleteJob(
            @Parameter(description = "Job ID", required = true) @PathVariable UUID id)
            throws SchedulerException {
        jobService.deleteJob(id);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get job history", description = "Retrieves execution history of a specific job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job history retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public List<JobExecutionResponse> getJobHistory(
            @Parameter(description = "Job ID", required = true) @PathVariable UUID id) {
        return jobService.getJobHistory(id);
    }
}
