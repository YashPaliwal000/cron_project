package com.scheduler.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scheduler.entity.Job;
import com.scheduler.entity.JobExecution;
import com.scheduler.repository.JobExecutionRepository;
import com.scheduler.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiJobExecutor implements org.quartz.Job {

    public static final String JOB_ID_KEY = "jobId";

    private final JobRepository jobRepository;
    private final JobExecutionRepository jobExecutionRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String jobIdStr = dataMap.getString(JOB_ID_KEY);
        if (jobIdStr == null) {
            log.error("Job ID is missing from JobDataMap");
            return;
        }

        UUID jobId = UUID.fromString(jobIdStr);
        log.info("========== JOB EXECUTION STARTED ==========");
        log.info("Job ID: {}", jobId);
        
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            log.error("Job not found in database. Job ID: {}", jobId);
            return;
        }

        Job job = jobOpt.get();
        log.info("Job Name: {}", job.getName());
        log.info("API Endpoint: {}", job.getApiEndpoint());
        log.info("HTTP Method: {}", job.getMethod());
        
        Instant start = Instant.now();
        log.info("Execution started at: {}", start);
        
        JobExecution.JobExecutionBuilder builder = JobExecution.builder()
                .jobId(job.getId())
                .startTime(start);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (job.getHeaders() != null) {
                Map<String, String> headerMap = objectMapper.readValue(job.getHeaders(), Map.class);
                log.debug("Request headers: {}", headerMap.keySet());
                headerMap.forEach(headers::add);
            } else {
                log.debug("No custom headers provided");
            }

            Object body = null;
            if (job.getPayload() != null) {
                body = objectMapper.readValue(job.getPayload(), Map.class);
                log.debug("Request payload: {}", job.getPayload());
            } else {
                log.debug("No request payload");
            }

            HttpMethod method = HttpMethod.valueOf(job.getMethod().toUpperCase());
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            log.info("Calling API endpoint: {} with method: {}", job.getApiEndpoint(), method);
            ResponseEntity<String> response = restTemplate.exchange(job.getApiEndpoint(), method, entity, String.class);

            Instant end = Instant.now();
            int durationMs = (int) (end.toEpochMilli() - start.toEpochMilli());

            log.info("✓ API call SUCCESSFUL");
            log.info("Response Status Code: {}", response.getStatusCode().value());
            log.info("Response Status: {}", response.getStatusCode());
            log.info("Execution Duration: {} ms", durationMs);

            builder
                    .endTime(end)
                    .status("SUCCESS")
                    .responseCode(response.getStatusCode().value())
                    .durationMs(durationMs);
                    
        } catch (Exception ex) {
            Instant end = Instant.now();
            int durationMs = (int) (end.toEpochMilli() - start.toEpochMilli());

            log.error("✗ API call FAILED");
            log.error("Error Type: {}", ex.getClass().getSimpleName());
            log.error("Error Message: {}", ex.getMessage());
            log.error("Execution Duration: {} ms", durationMs);
            log.debug("Full Stack Trace:", ex);

            builder
                    .endTime(end)
                    .status("FAILED")
                    .responseCode(null)
                    .durationMs(durationMs)
                    .errorMessage(ex.getMessage());
        }

        JobExecution execution = builder.build();
        jobExecutionRepository.save(execution);
        
        // Update job's last run info
        job.setLastRunTime(start);
        job.setLastRunStatus(execution.getStatus());
        jobRepository.save(job);
        
        log.info("✓ Job execution record saved");
        log.info("Execution Status: {}", execution.getStatus());
        log.info("========== JOB EXECUTION COMPLETED ==========");
        log.info("");
    }
}
