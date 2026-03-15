package com.scheduler.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class JobSummaryResponse {

    private UUID id;
    private String name;
    private String endpoint;
    private String method;
    private String status;
    private Instant nextRun;
    private String lastStatus;
}
