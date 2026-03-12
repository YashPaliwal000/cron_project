package com.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class CreateJobRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String apiEndpoint;

    @NotBlank
    private String method;

    private Map<String, String> headers;

    private Map<String, Object> payload;

    @NotNull
    private Instant scheduleTime;
}
