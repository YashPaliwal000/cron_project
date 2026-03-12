package com.scheduler.dto;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class UpdateJobRequest {

    private String name;

    private String apiEndpoint;

    private String method;

    private Map<String, String> headers;

    private Map<String, Object> payload;

    private Instant scheduleTime;

    private String status; // ACTIVE / PAUSED
}
