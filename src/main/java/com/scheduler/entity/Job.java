package com.scheduler.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    @Column(name = "api_endpoint", columnDefinition = "TEXT")
    private String apiEndpoint;

    private String method;

    @Column(columnDefinition = "TEXT")
    private String headers; // JSON string

    @Column(columnDefinition = "TEXT")
    private String payload; // JSON string

    @Column(name = "schedule_time")
    private Instant scheduleTime;

    private String status; // ACTIVE, PAUSED

    @Column(name = "created_at")
    private Instant createdAt;

}
