<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>환불 테스트 페이지</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f4f4f4; color: #333; }
        .container { max-width: 900px; margin: 0 auto; background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        h2 { color: #0056b3; margin-bottom: 20px; }
        .info-message { background-color: #e7f3ff; border: 1px solid #d0e7ff; padding: 10px; border-radius: 5px; margin-bottom: 20px; color: #0056b3; }
        .warning-message { background-color: #fff3e0; border: 1px solid #ffe0b2; padding: 10px; border-radius: 5px; margin-bottom: 20px; color: #e65100; }
        table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
        th { background-color: #e9e9e9; font-weight: bold; }
        .payment-status-PAID { color: green; font-weight: bold; }
        .payment-status-CANCELLED, .payment-status-REFUNDED { color: red; font-weight: bold; }
        .payment-status-PENDING, .payment-status-FAILED { color: orange; }
        .action-button {
            background-color: #dc3545; /* Red for refund */
            color: white;
            padding: 8px 12px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            text-decoration: none;
        }
        .action-button:hover {
            background-color: #c82333;
        }
        .action-button:disabled {
            background-color: #6c757d; /* Gray for disabled */
            cursor: not-allowed;
        }
    </style>
</head>
<body>
<div class="container">
    <h2>환불 테스트 페이지</h2>

    <div class="info-message">
        로그인한 사용자의 결제 내역입니다. "PAID" 상태의 결제만 환불할 수 있습니다.
    </div>

    <c:choose>
        <c:when test="${not empty userPayments}">
            <table>
                <thead>
                <tr>
                    <th>결제 ID</th>
                    <th>예약 ID</th>
                    <th>금액</th>
                    <th>결제 수단</th>
                    <th>상태</th>
                    <th>결제일</th>
                    <th>생성일</th>
                    <th>액션</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="payment" items="${userPayments}">
                    <tr>
                        <td>${payment.paymentId}</td>
                        <td>${payment.reservationId}</td>
                        <td><fmt:formatNumber value="${payment.amount}" type="currency" currencySymbol="₩"/></td>
                        <td>${payment.method}</td>
                        <td class="payment-status-${payment.status}">${payment.status}</td>
                        <td><fmt:parseDate value="${payment.paidAt}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="paidDate" />
                            <fmt:formatDate value="${paidDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                        <td><fmt:parseDate value="${payment.createdAt}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="createdDate" />
                            <fmt:formatDate value="${createdDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                        <td>
                            <c:if test="${payment.status == 'PAID'}">
                                <button class="action-button"
                                        onclick="requestRefund('${payment.paymentId}')">환불 요청</button>
                            </c:if>
                            <c:if test="${payment.status != 'PAID'}">
                                <button class="action-button" disabled>환불 불가</button>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <div class="warning-message">
                아직 결제 내역이 없습니다.
            </div>
        </c:otherwise>
    </c:choose>
</div>

<script>
    var contextPath = '${pageContext.request.contextPath}';
    function requestRefund(paymentId) {
        const reason = prompt("환불 사유를 입력해주세요:");
        if (reason === null || reason.trim() === "") {
            alert("환불 사유를 입력해야 합니다.");
            return;
        }

        if (confirm('결제 ID: ' + paymentId + '\n환불 사유: ' + reason + '\n\n정말로 환불을 요청하시겠습니까?')) {
            fetch(contextPath + '/api/payments/' + paymentId + '/refund',{
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({ cancelReason: reason })
            })
                .then(response => {
                    if (!response.ok) {
                        return response.json().then(err => { throw err; });
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.success) {
                        alert("환불 요청이 성공적으로 처리되었습니다.");
                        location.reload(); // 페이지 새로고침
                    } else {
                        alert("환불 요청 실패: " + (data.message || "알 수 없는 오류"));
                    }
                })
                .catch(error => {
                    let errorMessage = "오류가 발생했습니다.";
                    if (error.message) {
                        errorMessage = error.message;
                    }
                    alert("환불 요청 중 오류 발생: " + errorMessage);
                    console.error("Refund error:", error);
                });
        }
    }
</script>
</body>
</html>

