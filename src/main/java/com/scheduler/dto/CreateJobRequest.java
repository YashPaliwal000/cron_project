package com.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateJobRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String endpoint;

    @NotBlank
    private String method;

    private String headers;

    private String payload;

    @NotNull
    private String scheduleTime;
}
