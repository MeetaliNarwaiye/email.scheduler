package com.project.email.scheduler.controller;

import com.project.email.scheduler.payload.CalendarLogRequest;
import com.project.email.scheduler.payload.CalendarLogResponse;
import com.project.email.scheduler.quartz.job.CalendarEventJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@Slf4j
public class CalendarLogSchedulerController {
    @Autowired
    private Scheduler scheduler;

    @PostMapping("/schedule/storing")
    public ResponseEntity<CalendarLogResponse> scheduleLogStoring(@Valid @RequestBody CalendarLogRequest request) {
        try {
            // Build JobDetail and Cron Trigger
            JobDetail jobDetail = buildJobDetail(request);
            Trigger trigger = buildCronTrigger(jobDetail, request.getCronExpression());

            // Schedule the job
            scheduler.scheduleJob(jobDetail, trigger);

            return ResponseEntity.ok(CalendarLogResponse.builder()
                    .success(true)
                    .jobId(jobDetail.getKey().getName())
                    .jobGroup(jobDetail.getKey().getGroup())
                    .message("Audit log storing scheduled successfully at " + request.getCronExpression())
                    .build());
        } catch (SchedulerException e) {
            log.error("Error occurred while scheduling log storing job.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CalendarLogResponse.builder()
                            .success(false)
                            .message("An error occurred while scheduling. Please try again.")
                            .build());
        }
    }

    private JobDetail buildJobDetail(CalendarLogRequest request) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("eventTitle", request.getEventTitle());

        String jobName = "log_job_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        return JobBuilder.newJob(CalendarEventJob.class)
                .withIdentity(jobName, "log-jobs")
                .withDescription("Google Calendar Log Storing Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildCronTrigger(JobDetail jobDetail, String cronExpression) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "log-triggers")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
    }


    @DeleteMapping("/delete/{jobName}")
    public ResponseEntity<String> deleteJob(@PathVariable String jobName) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, "log-jobs");
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
            log.info("🗑️ Job deleted: {}", jobName);
            return ResponseEntity.ok("Job deleted: " + jobName);
        } else {
            return ResponseEntity.badRequest().body("Job not found: " + jobName);
        }
    }

    @PutMapping("/pause/{jobName}")
    public ResponseEntity<String> pauseJob(@PathVariable String jobName) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, "log-jobs");
        if (scheduler.checkExists(jobKey)) {
            scheduler.pauseJob(jobKey);
            log.info("⏸️ Job paused: {}", jobName);
            return ResponseEntity.ok("Job paused: " + jobName);
        } else {
            return ResponseEntity.badRequest().body("Job not found: " + jobName);
        }
    }

    @PutMapping("/resume/{jobName}")
    public ResponseEntity<String> resumeJob(@PathVariable String jobName) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, "log-jobs");
        if (scheduler.checkExists(jobKey)) {
            scheduler.resumeJob(jobKey);
            log.info("▶️ Job resumed: {}", jobName);
            return ResponseEntity.ok("Job resumed: " + jobName);
        } else {
            return ResponseEntity.badRequest().body("Job not found: " + jobName);
        }
    }

    @GetMapping("/all")
    public List<String> getAllJobs() throws SchedulerException {
        List<String> jobList = new ArrayList<>();

        for (String groupName : scheduler.getJobGroupNames()) {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));

            for (JobKey jobKey : jobKeys) {
                jobList.add("Job Name: " + jobKey.getName() + ", Group: " + jobKey.getGroup());
            }
        }

        return jobList;
    }
}
