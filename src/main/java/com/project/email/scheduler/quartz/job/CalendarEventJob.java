package com.project.email.scheduler.quartz.job;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.project.email.scheduler.audit.audit_log.dao.AuditLog;
import com.project.email.scheduler.audit.audit_log.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CalendarEventJob implements Job {

    private final Calendar googleCalendar;

    @Autowired
    AuditLogService auditLogService;

    @Value("${google.calendar.id}")
    private String calendarId;

    public CalendarEventJob(Calendar googleCalendar) {
        this.googleCalendar = googleCalendar;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String eventTitle = jobDataMap.getString("eventTitle");
        try {
            storeLogsInCalendar(eventTitle);
            log.info("✅ Event '{}' stored successfully in Google Calendar", eventTitle);
        } catch (Exception e) {
            log.error("❌ Error storing event '{}': {}", eventTitle, e.getMessage());
            throw new JobExecutionException("Failed to store event in Google Calendar", e);
        }
    }

    private void storeLogsInCalendar(String eventTitle) {

        List<AuditLog> todayLogs = auditLogService.getAll().stream()
                .filter(log -> log.getCreatedOn() != null &&
                        log.getCreatedOn().equals(LocalDate.now()))
                .toList();


        if (todayLogs.isEmpty()) {
            log.info("No audit logs found for today.");
            return;
        }

        List<String> description = new ArrayList<>();

        if (!todayLogs.isEmpty()) {
            AuditLog firstLog = todayLogs.get(0); // Use the first log as base time
            for (AuditLog logEntry : todayLogs) {
                description.add(logEntry.getObjectGroup());
            }

            try {
                // Format datetime in RFC 3339 (ISO 8601) format
                ZonedDateTime startZdt = firstLog.getCreatedOn().atTime(9, 0).atZone(ZoneId.of("Asia/Kolkata"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
                String startDateTime = startZdt.toOffsetDateTime().format(formatter);
                String endDateTime = startZdt.plusMinutes(30).toOffsetDateTime().format(formatter);


                Event event = new Event()
                        .setSummary(eventTitle + " - " + LocalDate.now())
                        .setDescription("Tables updated: " + description)
                        .setStart(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(startDateTime)).setTimeZone("Asia/Kolkata"))
                        .setEnd(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(endDateTime)).setTimeZone("Asia/Kolkata"));

//                googleCalendar.events().insert(calendarId, event).execute();

                Event createdEvent = googleCalendar.events().insert(calendarId, event).execute();
                log.info("📅 Event link: {}", createdEvent.getHtmlLink());

                log.info("✅ Successfully stored log in Google Calendar: {}", description);

            } catch (Exception e) {
                log.error("❌ Failed to store audit log in Google Calendar: {}", description, e);
            }
        } else {
            log.info("No logs found for today.");
        }


    }


}
