package servlet;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Date;

@WebServlet("/send-payment-email")
public class SendPaymentEmailServlet extends HttpServlet {
    
    // Sử dụng cấu hình email đã hoạt động từ dự án cũ
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_USERNAME = "ccc123cxqo@gmail.com"; // Email đã hoạt động
    private static final String EMAIL_PASSWORD = "kxbv ivla rxrt zdbv";   // App password đã hoạt động
    private static final String FROM_EMAIL = "ccc123cxqo@gmail.com";
    private static final String FROM_NAME = "Hotel Booking System";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Lấy email từ request
            String userEmail = request.getParameter("email");
            
            // Debug logs
            System.out.println("🔍 DEBUG - Received email: " + userEmail);
            System.out.println("🔍 DEBUG - Email config: " + EMAIL_USERNAME);
            
            // Kiểm tra email có tồn tại không
            if (userEmail == null || userEmail.trim().isEmpty()) {
                System.err.println("❌ Email is null or empty");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"status\":\"error\",\"message\":\"Email is required\"}");
                return;
            }
            
            // Log thông tin
            System.out.println("📧 Attempting to send email to: " + userEmail);
            
            // Gửi email
            boolean emailSent = sendConfirmationEmail(userEmail);
            
            if (emailSent) {
                System.out.println("✅ Email sent successfully to: " + userEmail);
                response.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"status\":\"success\",\"message\":\"Email sent successfully\"}");
            } else {
                System.out.println("❌ Failed to send email to: " + userEmail);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"status\":\"error\",\"message\":\"Failed to send email\"}");
            }
            
        } catch (Exception e) {
            System.err.println("📧 Error in SendPaymentEmailServlet: " + e.getMessage());
            e.printStackTrace();
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"status\":\"error\",\"message\":\"Server error: " + e.getMessage() + "\"}");
        } finally {
            out.close();
        }
    }
    
    private boolean sendConfirmationEmail(String userEmail) {
        try {
            System.out.println("🔍 DEBUG - Starting email send process");
            System.out.println("🔍 DEBUG - SMTP Host: " + SMTP_HOST);
            System.out.println("🔍 DEBUG - SMTP Port: " + SMTP_PORT);
            System.out.println("🔍 DEBUG - From email: " + FROM_EMAIL);
            
            // Cấu hình properties cho SMTP (giống dự án cũ)
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            
            // Thêm debug
            props.put("mail.debug", "true");
            
            System.out.println("🔍 DEBUG - Properties configured");
            
            // Tạo session với authentication (giống dự án cũ)
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    System.out.println("🔍 DEBUG - Authentication called");
                    return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                }
            });
            
            System.out.println("🔍 DEBUG - Session created");
            
            // Tạo message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
            message.setSubject("Xác nhận thanh toán thành công - Hotel Booking");
            message.setSentDate(new Date());
            
            System.out.println("🔍 DEBUG - Message created");
            
            // Nội dung email HTML
            String htmlContent = createEmailContent(userEmail);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            System.out.println("🔍 DEBUG - Content set, attempting to send...");
            
            // Gửi email
            Transport.send(message);
            
            System.out.println("✅ Email confirmation sent successfully to: " + userEmail);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error sending email: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private String createEmailContent(String userEmail) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <title>Xác nhận thanh toán</title>" +
                "</head>" +
                "<body style='font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f9f9f9;'>" +
                "    <div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>" +
                "        <div style='text-align: center; margin-bottom: 30px;'>" +
                "            <h1 style='color: #28a745; margin-bottom: 10px;'>✅ Thanh toán thành công!</h1>" +
                "            <p style='color: #666; font-size: 16px;'>Hotel Booking System</p>" +
                "        </div>" +
                "        " +
                "        <div style='background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>" +
                "            <h3 style='color: #333; margin-top: 0;'>Chi tiết giao dịch:</h3>" +
                "            <p><strong>Email khách hàng:</strong> " + userEmail + "</p>" +
                "            <p><strong>Thời gian:</strong> " + new Date().toString() + "</p>" +
                "            <p><strong>Trạng thái:</strong> <span style='color: #28a745; font-weight: bold;'>Thành công</span></p>" +
                "        </div>" +
                "        " +
                "        <div style='margin: 20px 0;'>" +
                "            <p style='font-size: 16px; line-height: 1.6;'>Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!</p>" +
                "            <p style='font-size: 16px; line-height: 1.6;'>Chúng tôi đã nhận được thanh toán của bạn và đang xử lý đơn hàng.</p>" +
                "        </div>" +
                "        " +
                "        <div style='text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>" +
                "            <p style='color: #666; font-size: 14px; margin: 0;'>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>" +
                "            <p style='color: #666; font-size: 14px; margin: 5px 0 0 0;'>Email: support@hotelbooking.com | Hotline: 1900-1234</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"error\",\"message\":\"GET method not allowed\"}");
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}