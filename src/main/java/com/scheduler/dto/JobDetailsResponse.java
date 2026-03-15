package com.scheduler.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class JobDetailsResponse {

    private UUID id;
    private String name;
    private String endpoint;
    private String method;
    private String status;
    private Instant scheduleTime;
    private String scheduleType;
    private String cronExpression;
    private String timeZone;
    private Map<String, String> headers;
    private Map<String, Object> payload;
    private Instant createdAt;
    private Instant lastRunTime;
    private String lastRunStatus;
}
