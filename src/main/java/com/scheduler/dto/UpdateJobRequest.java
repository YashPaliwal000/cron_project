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

    private String scheduleType; // ONE_TIME, RECURRING

    private String cronExpression;

    private String timeZone;

    private ScheduleConfig scheduleConfig;
}
