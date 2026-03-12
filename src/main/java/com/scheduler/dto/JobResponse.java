package com.scheduler.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class JobResponse {

    private UUID id;
    private String name;
    private String status;
    private Instant scheduleTime;
    private String apiEndpoint;
}
