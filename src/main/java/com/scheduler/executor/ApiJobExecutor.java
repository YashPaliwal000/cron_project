package com.scheduler.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scheduler.entity.Job;
import com.scheduler.entity.JobExecution;
import com.scheduler.repository.JobExecutionRepository;
import com.scheduler.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
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
public class ApiJobExecutor implements Job {

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
            return;
        }

        UUID jobId = UUID.fromString(jobIdStr);
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            return;
        }

        Job job = jobOpt.get();
        Instant start = Instant.now();
        JobExecution.JobExecutionBuilder builder = JobExecution.builder()
                .jobId(job.getId())
                .startTime(start);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (job.getHeaders() != null) {
                Map<String, String> headerMap = objectMapper.readValue(job.getHeaders(), Map.class);
                headerMap.forEach(headers::add);
            }

            Object body = null;
            if (job.getPayload() != null) {
                body = objectMapper.readValue(job.getPayload(), Map.class);
            }

            HttpMethod method = HttpMethod.valueOf(job.getMethod().toUpperCase());
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(job.getApiEndpoint(), method, entity, String.class);

            Instant end = Instant.now();
            int durationMs = (int) (end.toEpochMilli() - start.toEpochMilli());

            builder
                    .endTime(end)
                    .status("SUCCESS")
                    .responseCode(response.getStatusCode().value())
                    .durationMs(durationMs);
        } catch (Exception ex) {
            Instant end = Instant.now();
            int durationMs = (int) (end.toEpochMilli() - start.toEpochMilli());

            builder
                    .endTime(end)
                    .status("FAILED")
                    .responseCode(null)
                    .durationMs(durationMs)
                    .errorMessage(ex.getMessage());
        }

        jobExecutionRepository.save(builder.build());
    }
}
