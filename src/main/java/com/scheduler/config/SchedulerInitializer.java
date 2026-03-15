package com.scheduler.config;

import com.scheduler.entity.Job;
import com.scheduler.repository.JobRepository;
import com.scheduler.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerInitializer {

    private final JobRepository jobRepository;
    private final SchedulerService schedulerService;

    /**
     * On application startup, re-register ACTIVE jobs in Quartz so that
     * getNextRunTime() can return a valid nextRun even after restarts.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void rescheduleActiveJobs() {
        List<Job> activeJobs = jobRepository.findAll().stream()
                .filter(job -> "ACTIVE".equalsIgnoreCase(job.getStatus()))
                .toList();

        for (Job job : activeJobs) {
            try {
                String cron = job.getCronExpression();
                if ("RECURRING".equalsIgnoreCase(job.getScheduleType()) && cron != null && !cron.isBlank()) {
                    schedulerService.scheduleRecurringJob(job.getId(), cron, job.getTimeZone());
                } else if (job.getScheduleTime() != null) {
                    schedulerService.scheduleJob(job.getId(), job.getScheduleTime());
                }
            } catch (SchedulerException e) {
                log.error("Failed to reschedule job {} on startup", job.getId(), e);
            }
        }
    }
}


