<%@page import="model.UserAccount"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>



<%
    // Tạo mã ngẫu nhiên cho nội dung chuyển khoản
    String randomCode = "TP" + (int)(Math.random() * 90000 + 10000);
    request.setAttribute("paymentCode", randomCode);
%>
<%
    UserAccount user = (UserAccount) session.getAttribute("user");
    model.Room room = (model.Room) session.getAttribute("room");
    java.math.BigDecimal totalPrice = (java.math.BigDecimal) session.getAttribute("totalPrice");

    if (user != null) {
        request.setAttribute("user", user); // Bổ sung dòng này để JSTL dùng được
    }
    
    // Chuyển totalPrice từ session sang request attribute để JSTL sử dụng
    if (totalPrice != null) {
        request.setAttribute("totalPrice", totalPrice);
    }
%>

<!DOCTYPE html>
<html>
    <head>
        <title>Thanh toán bằng VietQR</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                text-align: center;
                padding: 50px;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                min-height: 100vh;
                margin: 0;
            }

            .qr-container {
                background: white;
                border-radius: 15px;
                box-shadow: 0 10px 30px rgba(0,0,0,0.2);
                padding: 30px;
                display: inline-block;
                max-width: 400px;
            }

            .header {
                margin-bottom: 25px;
            }

            .header h2 {
                color: #333;
                margin: 0 0 10px 0;
                font-size: 24px;
            }

            .header p {
                color: #666;
                margin: 0;
                font-size: 14px;
            }

            img {
                width: 250px;
                height: auto;
                margin: 20px 0;
                border-radius: 10px;
                border: 2px solid #f0f0f0;
            }

            .info {
                text-align: left;
                margin: 20px 0;
                background: #f8f9fa;
                padding: 20px;
                border-radius: 10px;
                border: 1px solid #e9ecef;
            }

            .info p {
                margin: 8px 0;
                font-size: 14px;
            }

            .info strong {
                color: #495057;
                display: inline-block;
                width: 140px;
            }

            .amount {
                color: #e74c3c !important;
                font-weight: bold !important;
                font-size: 16px !important;
            }

            .payment-code {
                background: #fff3cd;
                border: 1px solid #ffeaa7;
                padding: 8px;
                border-radius: 5px;
                font-family: monospace;
                color: #856404;
            }

            .instruction {
                margin: 20px 0;
                padding: 15px;
                background: #e3f2fd;
                border-radius: 8px;
                color: #1565c0;
                font-size: 14px;
            }

            .back-btn {
                display: inline-block;
                margin-top: 25px;
                padding: 12px 24px;
                background: #6c757d;
                color: white;
                text-decoration: none;
                border-radius: 25px;
                font-size: 14px;
                transition: all 0.3s ease;
            }

            .back-btn:hover {
                background: #495057;
                transform: translateY(-2px);
                box-shadow: 0 4px 8px rgba(0,0,0,0.2);
            }

            #countdown {
                color: #dc3545;
                font-size: 16px;
                margin: 20px 0;
                font-weight: bold;
                background: #f8d7da;
                padding: 10px;
                border-radius: 5px;
                border: 1px solid #f5c6cb;
            }

            .status-indicator {
                display: inline-block;
                width: 10px;
                height: 10px;
                border-radius: 50%;
                background: #28a745;
                animation: pulse 2s infinite;
                margin-right: 8px;
            }

            @keyframes pulse {
                0% { opacity: 1; }
                50% { opacity: 0.3; }
                100% { opacity: 1; }
            }
        </style>
    </head>
    <body>
        <div class="qr-container">
            <div class="header">
                <h2>🏨 Thanh toán VietQR</h2>
                <p>Staytion Hotel - Đặt phòng trực tuyến</p>
            </div>

            <div class="info">
                <p><strong>👤 Người nhận:</strong> DO VAN THANH CONG</p>
                <p><strong>🏦 Ngân hàng:</strong> MB Bank</p>
                <p><strong>💳 STK:</strong> 0775578504</p>

                <c:choose>
                    <c:when test="${totalPrice < 1000000}">
                        <c:set var="convertedPrice" value="${(totalPrice - (totalPrice % 1000))}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="convertedPrice" value="${(totalPrice - (totalPrice % 1000000))}" />
                    </c:otherwise>
                </c:choose>

                <p><strong>💰 Số tiền:</strong> 
                    <span class="amount">
                        <fmt:formatNumber value="${convertedPrice}" type="number" maxFractionDigits="0" groupingUsed="true"/> VNĐ
                    </span>
                </p>

                <p><strong>📝 Nội dung CK:</strong></p>
                <div class="payment-code">ThanhToanPhong${paymentCode}</div>
            </div>

            <img src="https://img.vietqr.io/image/MB-0775578504-compact2.png?amount=${totalPrice}&addInfo=ThanhToanPhong${paymentCode}&accountName=DO%20VAN%20THANH%20CONG"
                 alt="QR Code VietQR">

            <div class="instruction">
                📱 Mở app ngân hàng và quét mã QR để thanh toán
            </div>

            <div id="countdown">
                <span class="status-indicator"></span>
                Đang kiểm tra thanh toán...
            </div>

            <a href="<c:url value='/home' />" class="back-btn">← Quay lại trang chủ</a>
        </div>

        <script>
            let countdown = 180;
            let paymentChecked = false;
            const amount = ${totalPrice};
            const keyword = "<c:out value='ThanhToanPhong${paymentCode}' />";
            const checkUrl = "https://script.google.com/macros/s/AKfycbxwQ3Czl0IQ0KHvI5-7yY9YE-Zx7FuyHVrLrg64A8h6JDAluz9rN9dFyuZ5Aj56PjyLAA/exec";

            function updateTimer() {
                const timerDisplay = document.getElementById("countdown");
                
                if (paymentChecked) {
                    timerDisplay.innerHTML = '<span class="status-indicator"></span>✅ Thanh toán thành công! Đang chuyển hướng...';
                    timerDisplay.style.background = '#d4edda';
                    timerDisplay.style.color = '#155724';
                    timerDisplay.style.borderColor = '#c3e6cb';
                    return;
                }

                timerDisplay.innerText = "Tự động kiểm tra thanh toán trong: " + countdown + " giây";

                if (countdown > 0) {
                    countdown--;
                    setTimeout(updateTimer, 1000);
                } else {
                    timerDisplay.innerHTML = '⏰ Hết thời gian chờ. Chuyển về trang chủ...';
                    timerDisplay.style.background = '#f8d7da';
                    timerDisplay.style.color = '#721c24';
                    setTimeout(() => {
                        window.location.href = "<c:url value='/home' />";
                    }, 3000);
                }
            }

            async function checkPayment() {
                if (paymentChecked) return;
                
                try {
                    const res = await fetch(checkUrl);
                    const json = await res.json();

                    for (const tx of json.data) {
                        const txAmount = parseInt(tx["PRICE"]);
                        const txContent = (tx["CONTENT"] || "").trim();

                        if (txContent.includes(keyword) && txAmount === amount) {
                            paymentChecked = true;
                            console.log("💰 Payment detected!");
                            
                            // *** ĐÃ BỎ PHẦN GỬI EMAIL Ở ĐÂY ***
                            // Chỉ cập nhật booking trong database và truyền paymentCode
                            const formData = new FormData();
                            formData.append("roomId", "${room.roomId}");
                            formData.append("totalPrice", "<c:out value='${convertedPrice}' />");
                            formData.append("email", "<c:out value='${user.email}' />");
                            formData.append("paymentCode", "${paymentCode}"); // ← THÊM PAYMENTCODE

                            const response = await fetch("<c:url value='/payment-success'/>", {
                                method: "POST",
                                body: formData
                            });

                            // Redirect sau 2 giây để user thấy thông báo
                            setTimeout(() => {
                                window.location.href = "<c:url value='/thankyou' />";
                            }, 2000);
                            
                            return;
                        }
                    }

                    // Nếu chưa có payment, thử lại sau 3s
                    if (!paymentChecked) {
                        setTimeout(checkPayment, 3000);
                    }

                } catch (e) {
                    console.error("🔍 Error checking payment:", e);
                    if (!paymentChecked) {  
                        setTimeout(checkPayment, 3000);
                    }
                }
            }

            window.onload = function () {
                console.log("🚀 Payment checker started");
                console.log("💰 Expected amount:", amount, "VNĐ");
                console.log("🔑 Payment keyword:", keyword);
                
                updateTimer();
                checkPayment();
            };

            // Prevent accidental page refresh
            window.addEventListener('beforeunload', function (e) {
                if (!paymentChecked && countdown > 0) {
                    e.preventDefault();
                    e.returnValue = 'Bạn có chắc muốn rời khỏi trang? Quá trình thanh toán đang được kiểm tra.';
                }
            });
        </script>
    </body>
</html>