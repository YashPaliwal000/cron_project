package com.scheduler.dto;

import lombok.Data;

@Data
public class UpdateJobRequest {

    private String name;

    private String endpoint;

    private String method;

    private String headers;

    private String payload;

    private String scheduleTime;

    private String status; // ACTIVE / PAUSED
}
