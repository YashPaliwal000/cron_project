package com.scheduler.service;

import com.scheduler.executor.ApiJobExecutor;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final Scheduler scheduler;

    public void scheduleJob(UUID jobId, Instant scheduleTime) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(ApiJobExecutor.class)
                .withIdentity(jobId.toString())
                .usingJobData(ApiJobExecutor.JOB_ID_KEY, jobId.toString())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobId.toString())
                .startAt(java.util.Date.from(scheduleTime))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void pauseJob(UUID jobId) throws SchedulerException {
        scheduler.pauseJob(JobKey.jobKey(jobId.toString()));
    }

    public void resumeJob(UUID jobId) throws SchedulerException {
        scheduler.resumeJob(JobKey.jobKey(jobId.toString()));
    }

    public void deleteJob(UUID jobId) throws SchedulerException {
        scheduler.deleteJob(JobKey.jobKey(jobId.toString()));
    }

    public void scheduleRecurringJob(UUID jobId, String cronExpression, String timeZone) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(ApiJobExecutor.class)
                .withIdentity(jobId.toString())
                .usingJobData(ApiJobExecutor.JOB_ID_KEY, jobId.toString())
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobId.toString())
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression).inTimeZone(java.util.TimeZone.getTimeZone(timeZone != null ? timeZone : "UTC")))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * Returns the next scheduled run time for a given job based on Quartz triggers.
     * For recurring jobs this reflects the next fire time from the cron trigger.
     */
    public Instant getNextRunTime(UUID jobId) throws SchedulerException {
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(JobKey.jobKey(jobId.toString()));
        if (triggers == null || triggers.isEmpty()) {
            return null;
        }

        return triggers.stream()
                .map(Trigger::getNextFireTime)
                .filter(Objects::nonNull)
                .map(java.util.Date::toInstant)
                .sorted()
                .findFirst()
                .orElse(null);
    }
}
