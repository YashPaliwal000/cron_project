package com.scheduler.service;

import com.scheduler.executor.ApiJobExecutor;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
}
