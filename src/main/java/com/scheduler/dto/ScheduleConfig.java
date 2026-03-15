package com.scheduler.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScheduleConfig {
    private String frequency; // DAILY, WEEKLY, MONTHLY, CUSTOM
    private List<String> times; // HH:MM
    private List<String> daysOfWeek; // MON, TUE, etc.
    private List<Integer> daysOfMonth; // 1, 15, etc.
    private Integer interval; // for every N hours, etc.
}
