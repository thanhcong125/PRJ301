package util;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Date;
import java.io.*;
import java.text.SimpleDateFormat;

public class MailUtils {

    private static final String FROM_EMAIL = "ccc123cxqo@gmail.com";
    private static final String APP_PASSWORD = "kxbv ivla rxrt zdbv";
    private static final String FROM_NAME = "Staytion Hotel";
    
    // Log file path - sẽ tạo trong thư mục project
    private static final String LOG_FILE = "mail_log.txt";
    
    private static void writeLog(String message) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = sdf.format(new Date());
            
            FileWriter fw = new FileWriter(LOG_FILE, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("[" + timestamp + "] " + message);
            bw.newLine();
            bw.close();
            fw.close();
            
            // Vẫn in ra console
            System.out.println(message);
        } catch (IOException e) {
            System.err.println("Cannot write to log file: " + e.getMessage());
            System.out.println(message); // Fallback to console
        }
    }

    public static boolean sendMail(String toEmail, String subject, String content) {
        writeLog("=== EMAIL SEND ATTEMPT START ===");
        writeLog("To: " + toEmail);
        writeLog("Subject: " + subject);
        
        // Validate input
        if (toEmail == null || toEmail.trim().isEmpty()) {
            writeLog("❌ ERROR: Email address is null or empty");
            return false;
        }

        if (!isValidEmail(toEmail)) {
            writeLog("❌ ERROR: Invalid email format: " + toEmail);
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.connectiontimeout", "30000");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.writetimeout", "30000");
        props.put("mail.mime.charset", "UTF-8");
        props.put("mail.transport.protocol", "smtp");
        
        writeLog("SMTP Configuration set");

        try {
            writeLog("Creating mail session...");
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    writeLog("Authenticating with Gmail...");
                    return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                }
            });

            writeLog("Creating email message...");
            MimeMessage message = new MimeMessage(session);
            
            // Set sender
            try {
                message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME, "UTF-8"));
                writeLog("Sender set: " + FROM_NAME + " <" + FROM_EMAIL + ">");
            } catch (UnsupportedEncodingException e) {
                message.setFrom(new InternetAddress(FROM_EMAIL));
                writeLog("Sender set (fallback): " + FROM_EMAIL);
            }
            
            // Set recipient
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail.trim()));
            writeLog("Recipient set: " + toEmail);
            
            // Set subject
            message.setSubject(subject, "UTF-8");
            writeLog("Subject set");
            
            // Set content
            message.setContent(content, "text/html; charset=UTF-8");
            writeLog("Content set (HTML with UTF-8)");
            
            // Set additional headers
            message.setSentDate(new Date());
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            writeLog("Headers set");

            writeLog("Attempting to send email...");
            Transport.send(message);
            
            writeLog("✅ SUCCESS: Email sent successfully!");
            writeLog("=== EMAIL SEND ATTEMPT END (SUCCESS) ===");
            return true;
            
        } catch (AuthenticationFailedException e) {
            writeLog("❌ AUTHENTICATION FAILED: " + e.getMessage());
            writeLog("Check email credentials and app password");
            writeLog("=== EMAIL SEND ATTEMPT END (AUTH FAILED) ===");
            return false;
            
        } catch (SendFailedException e) {
            writeLog("❌ SEND FAILED: " + e.getMessage());
            if (e.getInvalidAddresses() != null) {
                writeLog("Invalid addresses: " + java.util.Arrays.toString(e.getInvalidAddresses()));
            }
            writeLog("=== EMAIL SEND ATTEMPT END (SEND FAILED) ===");
            return false;
            
        } catch (MessagingException e) {
            writeLog("❌ MESSAGING EXCEPTION: " + e.getMessage());
            
            String errorMsg = e.getMessage().toLowerCase();
            if (errorMsg.contains("could not connect")) {
                writeLog("DIAGNOSIS: Connection issue - Check internet/firewall");
            } else if (errorMsg.contains("authentication")) {
                writeLog("DIAGNOSIS: Authentication issue - Check credentials");
            } else if (errorMsg.contains("timeout")) {
                writeLog("DIAGNOSIS: Timeout issue - Server may be slow");
            } else if (errorMsg.contains("smtp")) {
                writeLog("DIAGNOSIS: SMTP server issue");
            }
            
            writeLog("FULL ERROR STACK:");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            writeLog(sw.toString());
            
            writeLog("=== EMAIL SEND ATTEMPT END (MESSAGING ERROR) ===");
            return false;
            
        } catch (Exception e) {
            writeLog("❌ UNEXPECTED ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            writeLog("FULL ERROR STACK:");
            writeLog(sw.toString());
            
            writeLog("=== EMAIL SEND ATTEMPT END (UNEXPECTED ERROR) ===");
            return false;
        }
    }
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        try {
            InternetAddress emailAddr = new InternetAddress(email.trim());
            emailAddr.validate();
            return true;
        } catch (AddressException e) {
            writeLog("Email validation failed: " + e.getMessage());
            return false;
        }
    }
    
    // Test method riêng
    public static void testEmailSystem() {
        writeLog("======================================");
        writeLog("STARTING EMAIL SYSTEM TEST");
        writeLog("======================================");
        
        String testEmail = "congdvt119214@gmail.com"; // Thay bằng email của bạn
        String testSubject = "Test Email - " + new Date();
        String testContent = createSimpleTestEmail();
        
        writeLog("Test parameters:");
        writeLog("- Test email: " + testEmail);
        writeLog("- From email: " + FROM_EMAIL);
        writeLog("- App password length: " + APP_PASSWORD.length() + " characters");
        
        boolean result = sendMail(testEmail, testSubject, testContent);
        
        writeLog("======================================");
        writeLog("EMAIL SYSTEM TEST RESULT: " + (result ? "SUCCESS ✅" : "FAILED ❌"));
        writeLog("======================================");
        
        if (!result) {
            writeLog("TROUBLESHOOTING STEPS:");
            writeLog("1. Check if Gmail 2FA is enabled");
            writeLog("2. Verify app password is correct");
            writeLog("3. Check internet connection");
            writeLog("4. Try different email address");
            writeLog("5. Check firewall/antivirus blocking");
        }
    }
    
    private static String createSimpleTestEmail() {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head><meta charset='UTF-8'></head>" +
               "<body style='font-family: Arial, sans-serif; padding: 20px;'>" +
               "<h2 style='color: #4CAF50;'>✅ Email Test Success</h2>" +
               "<p>This is a test email from your Java application.</p>" +
               "<p><strong>Time:</strong> " + new Date() + "</p>" +
               "<p><strong>System:</strong> Staytion Hotel Booking</p>" +
               "<hr>" +
               "<p style='color: #666; font-size: 14px;'>If you see this email, the mail system is working correctly!</p>" +
               "</body>" +
               "</html>";
    }
    
    // Method để xem log gần nhất
    public static void showRecentLogs() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(LOG_FILE));
            String line;
            System.out.println("=== RECENT EMAIL LOGS ===");
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
        } catch (IOException e) {
            System.out.println("No log file found or cannot read: " + e.getMessage());
        }
    }
    
    // Method để clear log
    public static void clearLogs() {
        try {
            new FileWriter(LOG_FILE, false).close();
            writeLog("Log file cleared");
        } catch (IOException e) {
            System.out.println("Cannot clear log file: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        // Uncomment để test
        testEmailSystem();
        
        // Uncomment để xem log
        // showRecentLogs();
    }
}