
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>결제 완료 - Flyway</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/base.css"/>
    <style>
        body { font-family: Arial, sans-serif; }
        .container {
            width: 100%;
            max-width: 600px;
            margin: 0 auto;
            padding: 60px 20px;
            box-sizing: border-box;
            text-align: center;
        }

        /* 성공/실패 아이콘 */
        .result-icon {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 24px;
            font-size: 40px;
        }
        .result-icon.success {
            background: #e8f5e9;
            color: #4caf50;
        }
        .result-icon.fail {
            background: #ffebee;
            color: #f44336;
        }

        .result-title {
            font-size: 28px;
            font-weight: 800;
            color: #111;
            margin-bottom: 12px;
        }
        .result-message {
            font-size: 16px;
            color: #666;
            margin-bottom: 32px;
        }

        /* 결제 정보 카드 */
        .payment-card {
            background: #fff;
            border: 1px solid #e5e5e5;
            border-radius: 12px;
            padding: 24px;
            margin-bottom: 24px;
            text-align: left;
        }
        .payment-row {
            display: flex;
            justify-content: space-between;
            padding: 12px 0;
            border-bottom: 1px solid #f0f0f0;
        }
        .payment-row:last-child {
            border-bottom: none;
        }
        .payment-label {
            color: #666;
            font-size: 14px;
        }
        .payment-value {
            color: #111;
            font-weight: 600;
            font-size: 14px;
        }
        .payment-amount {
            font-size: 20px;
            font-weight: 800;
            color: #1f6feb;
        }

        /* 버튼 */
        .btn-group {
            display: flex;
            gap: 12px;
        }
        .btn {
            flex: 1;
            padding: 16px;
            font-size: 16px;
            font-weight: 700;
            border-radius: 8px;
            cursor: pointer;
            text-decoration: none;
            text-align: center;
        }
        .btn-primary {
            background: #1f6feb;
            color: #fff;
            border: none;
        }
        .btn-secondary {
            background: #fff;
            color: #333;
            border: 1px solid #ddd;
        }

        /* 에러 박스 */
        .error-box {
            background: #fff5f5;
            border: 1px solid #ffcdd2;
            border-radius: 8px;
            padding: 16px;
            margin-bottom: 24px;
            text-align: left;
        }
        .error-code {
            font-size: 12px;
            color: #999;
            margin-bottom: 4px;
        }
        .error-message {
            font-size: 14px;
            color: #c62828;
        }
    </style>
    <!-- Confetti 라이브러리 -->
    <script src="https://cdn.jsdelivr.net/npm/canvas-confetti@1.6.0/dist/confetti.browser.min.js"></script>
</head>
</head>
<body>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="container">

    <c:choose>
        <%-- 결제 성공 --%>
        <c:when test="${success}">
            <div class="result-icon success">✓</div>
            <h1 class="result-title">결제가 완료되었습니다</h1>
            <p class="result-message">예약이 확정되었습니다. 감사합니다!</p>

            <div class="payment-card">
                <div class="payment-row">
                    <span class="payment-label">주문번호</span>
                    <span class="payment-value">${payment.orderId}</span>
                </div>
                <div class="payment-row">
                    <span class="payment-label">결제수단</span>
                    <span class="payment-value">${payment.method}</span>
                </div>
                <div class="payment-row">
                    <span class="payment-label">결제일시</span>
                    <span class="payment-value">
  <c:choose>
      <c:when test="${not empty payment.paidAt}">
          <!-- LocalDateTime.toString() = 2026-01-28T20:26:41 → T를 공백으로 바꾸고, 분까지만 자름 -->
          <c:out value="${fn:substring(fn:replace(payment.paidAt, 'T', ' '), 0, 16)}"/>
      </c:when>
      <c:otherwise>-</c:otherwise>
  </c:choose>
</span>
                </div>
                <div class="payment-row">
                    <span class="payment-label">결제금액</span>
                    <span class="payment-value payment-amount">
                          <fmt:formatNumber value="${payment.amount}" type="number"/>원
                      </span>
                </div>
            </div>

            <div class="btn-group">
                <a href="/" class="btn btn-secondary">홈으로</a>
                <a href="/mypage/reservations" class="btn btn-primary">예약 내역 보기</a>
            </div>
        </c:when>

        <%-- 결제 실패 --%>
        <c:otherwise>
            <div class="result-icon fail">✕</div>
            <h1 class="result-title">결제에 실패했습니다</h1>
            <p class="result-message">다시 시도해주세요.</p>

            <c:if test="${not empty errorCode}">
                <div class="error-box">
                    <div class="error-code">에러 코드: ${errorCode}</div>
                    <div class="error-message">${errorMessage}</div>
                </div>
            </c:if>

            <c:if test="${not empty errorMessage and empty errorCode}">
                <div class="error-box">
                    <div class="error-message">${errorMessage}</div>
                </div>
            </c:if>

            <div class="btn-group">
                <a href="javascript:history.back()" class="btn btn-secondary">돌아가기</a>
                <a href="/" class="btn btn-primary">홈으로</a>
            </div>
        </c:otherwise>
    </c:choose>

</div>
<c:if test="${success}">
    <script>
        // 페이지 로드 시 폭죽 효과
        document.addEventListener('DOMContentLoaded', function() {
            // 첫 번째 폭죽 - 왼쪽
            confetti({
                particleCount: 100,
                spread: 70,
                origin: { x: 0.2, y: 0.6 }
            });

            // 두 번째 폭죽 - 오른쪽
            confetti({
                particleCount: 100,
                spread: 70,
                origin: { x: 0.8, y: 0.6 }
            });

            // 0.3초 후 중앙에서 한 번 더
            setTimeout(function() {
                confetti({
                    particleCount: 150,
                    spread: 100,
                    origin: { x: 0.5, y: 0.5 }
                });
            }, 300);
        });
    </script>
</c:if>
</body>
</html>
