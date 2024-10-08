package com.project.email.scheduler.controller;

import com.project.email.scheduler.payload.EmailRequest;
import com.project.email.scheduler.payload.EmailResponse;
import com.project.email.scheduler.quartz.job.EmailJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RestController
public class EmailSchedulerController {
    @Autowired
    private Scheduler scheduler;

    @PostMapping("/schedule/email")
    public ResponseEntity<EmailResponse> scheduleEmail(@Valid @RequestBody EmailRequest emailRequest){

       try {
           ZonedDateTime dateTime=ZonedDateTime.of(emailRequest.getDateTime(),emailRequest.getTimeZone());
           if(dateTime.isBefore(ZonedDateTime.now())){
               return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                       .body(EmailResponse.builder()
                               .success(false)
                               .message("error while scheduling email.Please try again!")
                               .build());
           }
           JobDetail jobDetail=buildJobDetail(emailRequest);
           Trigger trigger= buildTrigger(jobDetail,dateTime);
           scheduler.scheduleJob(jobDetail,trigger);

           return ResponseEntity.ok(EmailResponse.builder()
                           .success(true)
                           .jobId(jobDetail.getKey().getName())
                           .jobGroup(jobDetail.getKey().getGroup())
                           .message("Email scheduled Successfully!!!")
                           .build());

       }catch (SchedulerException sc){
           log.error("error while scheduling", sc);
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(EmailResponse.builder()
                           .success(false)
                           .message("error while scheduling email.Please try again!")
                           .build());
       }
    }
//    @GetMapping("/get")
//    public ResponseEntity<String> gettest(){
//        return ResponseEntity.ok("working");
//    }
    private JobDetail buildJobDetail(EmailRequest request) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("email", request.getEmail());
        jobDataMap.put("subject", request.getSubject());
        jobDataMap.put("body", request.getBody());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString())
                .withDescription("send email job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }
    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt){
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(),"email-triggers")
                .withDescription("send email trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}
