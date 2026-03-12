package com.scheduler.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class JobExecutionResponse {

    private Instant startTime;
    private Instant endTime;
    private String status;
    private Integer durationMs;
}
