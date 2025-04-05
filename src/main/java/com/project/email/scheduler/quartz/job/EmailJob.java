//package com.project.email.scheduler.quartz.job;
//
//import com.project.email.scheduler.audit.audit_log_i.dao.AuditLogI;
//import com.project.email.scheduler.audit.audit_log_i.service.AuditLogIService;
//import com.sendgrid.Method;
//import com.sendgrid.Request;
//import com.sendgrid.Response;
//import com.sendgrid.SendGrid;
//import com.sendgrid.helpers.mail.Mail;
//import com.sendgrid.helpers.mail.objects.Content;
//import com.sendgrid.helpers.mail.objects.Email;
//import lombok.extern.slf4j.Slf4j;
//import org.quartz.JobDataMap;
//import org.quartz.JobExecutionContext;
//import org.quartz.JobExecutionException;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.quartz.QuartzJobBean;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.Cipher;
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//import java.io.IOException;
//import java.time.LocalDate;
//import java.util.Base64;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Component
//@Slf4j
//public class EmailJob extends QuartzJobBean {
//
//    private final AuditLogIService auditLogIService;
//
//    @Value("${spring.mail.username}")
//    private String senderEmail;
//
//    @Value("${spring.mail.password}")
//    private String encryptedPassword;
//
//    @Value("${encryption.secret.key}")
//    private String secretKeyBase64;
//
//    @Value("${sendgrid.api.key}")
//    private String sendGridApiKey;
//
//    public EmailJob(AuditLogIService auditLogIService) {
//        this.auditLogIService = auditLogIService;
//    }
//
//    @Override
//    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
//        JobDataMap jobDataMap = context.getMergedJobDataMap();
//        String recipientEmail = jobDataMap.getString("email");
//        String emailSubject = jobDataMap.getString("subject");
//
//        try {
//            sendEmail(recipientEmail, emailSubject);
//        } catch (Exception e) {
//            log.error("Error occurred while sending email to {}: {}", recipientEmail, e.getMessage());
//            throw new JobExecutionException("Failed to execute email job", e);
//        }
//    }
//
//    private void sendEmail(String to, String subject) throws Exception {
//        log.info("Preparing to send email to {}", to);
//
//        // Decrypt password for usage if necessary
//        String decryptedPassword = decryptPassword();
//        log.info("Decrypted mail password successfully.");
//
//        // Fetch email body from logs
//        String emailBody = fetchLogsForEmailBody();
//
//        // Set up SendGrid email details
//        Email from = new Email(senderEmail);
//        Email toEmail = new Email(to);
//        Content content = new Content("text/html", emailBody);
//        Mail mail = new Mail(from, subject, toEmail, content);
//
//        SendGrid sendGrid = new SendGrid(sendGridApiKey);
//        Request request = new Request();
//
//        try {
//            request.setMethod(Method.POST);
//            request.setEndpoint("mail/send");
//            request.setBody(mail.build());
//
//            Response response = sendGrid.api(request);
//
//            log.info("Email sent to {}. Status Code: {}, Response Body: {}", to, response.getStatusCode(), response.getBody());
//        } catch (IOException e) {
//            log.error("Failed to send email: {}", e.getMessage());
//            throw new Exception("Error while sending email using SendGrid", e);
//        }
//    }
//
//    private String fetchLogsForEmailBody() {
//        LocalDate now = LocalDate.now();
//        LocalDate oneDayBefore = now.minusDays(1);
//
//        List<AuditLogI> filteredLogs = auditLogIService.getAll().stream()
//                .filter(log -> !log.getCreatedOn().isBefore(oneDayBefore) && !log.getCreatedOn().isAfter(now))
//                .collect(Collectors.toList());
//
//        return filteredLogs.stream()
//                .map(log -> "<p>Change: " + log.toString() + "</p>")
//                .collect(Collectors.joining());
//    }
//
//    public String decryptPassword(String encryptedPassword) {
//        // Clean up the Base64 string if needed (e.g., remove unwanted characters or add padding)
//        String cleanedPassword = encryptedPassword.trim();
//        int paddingLength = 4 - (cleanedPassword.length() % 4);
//        if (paddingLength != 4) {
//            cleanedPassword += "=".repeat(paddingLength);
//        }
//        return new String(Base64.getDecoder().decode(cleanedPassword));
//    }
//
//    public String decryptPassword() throws Exception {
//        // Decode the Base64-encoded AES key
//        byte[] decodedKey = Base64.getDecoder().decode(secretKeyBase64);
//        SecretKey secretKey = new SecretKeySpec(decodedKey, "AES");
//
//        // Decode the Base64-encrypted password
//        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPassword);
//
//        // Initialize the Cipher for AES decryption
//        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//        cipher.init(Cipher.DECRYPT_MODE, secretKey);
//
//        // Perform the decryption
//        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
//
//        return new String(decryptedBytes).trim();
//    }
//
//}
