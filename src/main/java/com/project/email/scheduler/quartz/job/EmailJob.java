package com.project.email.scheduler.quartz.job;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class EmailJob extends QuartzJobBean {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    MailProperties mailProperties;
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        JobDataMap jobDataMap=context.getMergedJobDataMap();
        String email= jobDataMap.getString("email");
        String subject= jobDataMap.getString("subject");
        String body=  jobDataMap.getString("body");
        sendEmail(mailProperties.getUsername(),email,subject,body);
    }

    private void sendEmail(String from,String to,String subject,String body) {
        try {
            log.info("Sending Email to {}", to);
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper messageHelper = new MimeMessageHelper(message, StandardCharsets.UTF_8.toString());
            messageHelper.setSubject(subject);
            messageHelper.setText(body, true);
            messageHelper.setFrom(from);
            messageHelper.setTo(to);

            mailSender.send(message);
        } catch (MessagingException ex) {
            log.error("Failed to send email to {}", to);
        }
    }
}
