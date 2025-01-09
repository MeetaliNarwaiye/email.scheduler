package com.project.email.scheduler.quartz.job;

import com.project.email.scheduler.audit.audit_log_i.dao.AuditLogI;
import com.project.email.scheduler.audit.audit_log_i.service.AuditLogIService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EmailJob extends QuartzJobBean {

    @Autowired
    MailProperties mailProperties;
    @Autowired
    AuditLogIService auditLogIService;
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.password}")
    private String encryptedPassword;

    @Value("${encryption.secret.key}")
    private String secretKeyBase64;

    //    @Override
//    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
//
//        JobDataMap jobDataMap = context.getMergedJobDataMap();
//        String email = jobDataMap.getString("email");
//        String subject = jobDataMap.getString("subject");
//        String decryptedPassword = null;
//        try {
//            decryptedPassword = decryptPassword();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        // String body = jobDataMap.getString("body");
//        mailProperties.setPassword(decryptedPassword);
//        sendEmail(mailProperties.getUsername(), email, subject);
//    }
    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;  // Injected via environment variable

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();

        // Sample email details; replace with dynamic data as needed
        String toEmail = jobDataMap.getString("email");  // Replace with dynamic recipient email
        String subject = jobDataMap.getString("subject");

        try {
            sendEmail(toEmail, subject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendEmail(String to, String subject) throws Exception {
        // Initialize SendGrid with the API key from environment variables
        SendGrid sendGrid = new SendGrid(sendGridApiKey);

        // Use a verified email address from your SendGrid account as the sender's email
        Email from = new Email("meetalinarwaiye91@gmail.com");  // Replace with your verified email
        Email toEmail = new Email(to);

        // Generate the body from your existing logic (filtering logs from the previous day)
        LocalDate now = LocalDate.now();
        LocalDate oneDayBefore = now.minusDays(1);

        List<AuditLogI> filteredLogs = auditLogIService.getAll().stream()
                .filter(log -> !log.getCreatedOn().isBefore(oneDayBefore) && !log.getCreatedOn().isAfter(now))
                .collect(Collectors.toList());

        // Convert the filtered logs to a string (or a suitable HTML format for better email presentation)
        String body = filteredLogs.stream()
                .map(log -> "Change: " + log.toString())  // You can customize this formatting
                .collect(Collectors.joining("<br>"));  // Joining logs with line breaks for HTML formatting

        Content content = new Content("text/html", body);  // Use "text/html" for HTML content
        Mail mail = new Mail(from, subject, toEmail, content);

        // Send the email
        Request request = new Request();
        try {
            // The 'Method.POST' is directly available in the Request class from the SendGrid SDK
            request.setMethod(Method.POST);  // No need for Request.Method, just use Method.POST
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            System.out.println("Response Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            System.out.println("Response Headers: " + response.getHeaders());
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Failed to send email", e);  // Re-throw exception with more context
        }
    }


//    private void sendEmail(String from, String to, String subject) {
//        try {
//            log.info("Sending Email to {}", to);
//            MimeMessage message = mailSender.createMimeMessage();
//
//            MimeMessageHelper messageHelper = new MimeMessageHelper(message, StandardCharsets.UTF_8.toString());
//            messageHelper.setSubject(subject);
//
//            LocalDate now = LocalDate.now();
//            LocalDate oneDayBefore = now.minusDays(1);
//
//            List<AuditLogI> filteredLogs = auditLogIService.getAll().stream()
//                    .filter(log -> !log.getCreatedOn().isBefore(oneDayBefore) && !log.getCreatedOn().isAfter(now))
//                    .collect(Collectors.toList());
//
//            messageHelper.setText(filteredLogs.toString(), true);
//            messageHelper.setFrom(from);
//            messageHelper.setTo(to);
//
//            // Use the decrypted password to authenticate and send the email
//            mailSender.send(message);
//        } catch (MessagingException ex) {
//            log.error("Failed to send email to {}", to);
//        }
//    }

    public String decryptPassword() throws Exception {
        // Decode the Base64-encoded secret key from application properties
        byte[] decodedKey = Base64.getDecoder().decode(secretKeyBase64);
        SecretKey secretKey = new SecretKeySpec(decodedKey, "AES");

        // Initialize Cipher for AES decryption
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // Decode and decrypt the password
        byte[] decodedPassword = Base64.getDecoder().decode(encryptedPassword);
        byte[] decrypted = cipher.doFinal(decodedPassword);

        return new String(decrypted).trim();
    }
}
