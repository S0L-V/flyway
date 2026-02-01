<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>결제 - Flyway</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/base.css"/>
    <style>
        body { font-family: Arial, sans-serif; }
        .container {
            width: 100%;
            max-width: 600px;
            margin: 0 auto;
            padding: 40px 20px;
            box-sizing: border-box;
        }
        .payment-card {
            background: #fff;
            border: 1px solid #e5e5e5;
            border-radius: 12px;
            padding: 32px;
            margin-bottom: 24px;
        }
        .payment-title {
            font-size: 24px;
            font-weight: 800;
            color: #111;
            margin: 0 0 24px 0;
        }
        .payment-info {
            margin-bottom: 24px;
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
        .payment-total {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px 0;
            border-top: 2px solid #111;
            margin-top: 16px;
        }
        .payment-total-label {
            font-size: 18px;
            font-weight: 800;
            color: #111;
        }
        .payment-total-value {
            font-size: 24px;
            font-weight: 800;
            color: #1f6feb;
        }

        /* 결제 수단 선택 */
        .payment-method {
            margin-bottom: 24px;
        }
        .payment-method-title {
            font-size: 16px;
            font-weight: 700;
            color: #111;
            margin-bottom: 12px;
        }
        .method-buttons {
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
        }
        .method-btn {
            flex: 1;
            min-width: 100px;
            padding: 16px;
            border: 2px solid #e5e5e5;
            border-radius: 8px;
            background: #fff;
            cursor: pointer;
            text-align: center;
            transition: all 0.2s;
        }
        .method-btn:hover {
            border-color: #1f6feb;
        }
        .method-btn.active {
            border-color: #1f6feb;
            background: #f0f7ff;
        }
        .method-btn-icon {
            font-size: 24px;
            margin-bottom: 8px;
        }
        .method-btn-label {
            font-size: 13px;
            font-weight: 600;
            color: #333;
        }

        /* 버튼 */
        .btn-pay {
            width: 100%;
            padding: 18px;
            font-size: 18px;
            font-weight: 700;
            color: #fff;
            background: #1f6feb;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            transition: background 0.2s;
        }
        .btn-pay:hover {
            background: #1a5fd1;
        }
        .btn-pay:disabled {
            background: #ccc;
            cursor: not-allowed;
        }
        .btn-cancel {
            width: 100%;
            padding: 14px;
            font-size: 14px;
            font-weight: 600;
            color: #666;
            background: #fff;
            border: 1px solid #ddd;
            border-radius: 8px;
            cursor: pointer;
            margin-top: 12px;
        }

        /* 로딩 */
        .loading {
            display: none;
            text-align: center;
            padding: 40px;
        }
        .loading.show {
            display: block;
        }
        .spinner {
            width: 40px;
            height: 40px;
            border: 4px solid #f0f0f0;
            border-top: 4px solid #1f6feb;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin: 0 auto 16px;
        }
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="container">
    <div class="payment-card">
        <h1 class="payment-title">결제하기</h1>

        <!-- 결제 정보 -->
        <div class="payment-info">
            <div class="payment-row">
                <span class="payment-label">주문번호</span>
                <span class="payment-value"><c:out value="${orderId}"/></span>
            </div>
            <div class="payment-row">
                <span class="payment-label">상품명</span>
                <span class="payment-value"><c:out value="${orderName}"/></span>
            </div>
            <div class="payment-row">
                <span class="payment-label">결제자</span>
                <span class="payment-value"><c:out value="${customerName}"/></span>
            </div>
        </div>

        <!-- 총 결제 금액 -->
        <div class="payment-total">
            <span class="payment-total-label">총 결제 금액</span>
            <span class="payment-total-value">
                  <fmt:formatNumber value="${amount}" type="number"/>원
              </span>
        </div>
    </div>

    <!-- 결제 수단 선택 -->
    <div class="payment-card">
        <div class="payment-method">
            <div class="payment-method-title">결제 수단 선택</div>
            <div class="method-buttons">
                <button type="button" class="method-btn active" data-method="CARD">
                    <div class="method-btn-icon">💳</div>
                    <div class="method-btn-label">카드</div>
                </button>
                <button type="button" class="method-btn" data-method="TRANSFER">
                    <div class="method-btn-icon">🏦</div>
                    <div class="method-btn-label">계좌이체</div>
                </button>
                <button type="button" class="method-btn" data-method="EASY_PAY">
                    <div class="method-btn-icon">📱</div>
                    <div class="method-btn-label">간편결제</div>
                </button>
            </div>
        </div>

        <button type="button" class="btn-pay" id="btnPay">
            <fmt:formatNumber value="${amount}" type="number"/>원 결제하기
        </button>
        <button type="button" class="btn-cancel" onclick="history.back()">
            취소
        </button>
    </div>

    <!-- 로딩 -->
    <div class="loading" id="loading">
        <div class="spinner"></div>
        <div>결제를 진행하고 있습니다...</div>
    </div>
</div>

<!-- 토스 페이먼츠 SDK -->
<script src="https://js.tosspayments.com/v1/payment"></script>

<script>
    // 토스 SDK 초기화
    var clientKey = '${clientKey}';
    var tossPayments = TossPayments(clientKey);

    // 결제 정보
    var paymentData = {
        orderId: '${orderId}',
        orderName: '${orderName}',
        amount: ${amount},
        customerName: '${customerName}',
        customerEmail: '${customerEmail}',
        successUrl: '${successUrl}',
        failUrl: '${failUrl}'
    };

    // 선택된 결제 수단
    var selectedMethod = 'CARD';

    // 결제 수단 버튼 클릭
    document.querySelectorAll('.method-btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            document.querySelectorAll('.method-btn').forEach(function(b) {
                b.classList.remove('active');
            });
            this.classList.add('active');
            selectedMethod = this.dataset.method;
        });
    });

    // 결제 버튼 클릭
    document.getElementById('btnPay').addEventListener('click', function() {
        // 로딩 표시
        document.getElementById('loading').classList.add('show');
        this.disabled = true;

        // 결제 수단에 따라 토스 SDK 호출
        var methodMap = {
            'CARD': '카드',
            'TRANSFER': '계좌이체',
            'EASY_PAY': '간편결제'
        };

        tossPayments.requestPayment(methodMap[selectedMethod], {
            amount: paymentData.amount,
            orderId: paymentData.orderId,
            orderName: paymentData.orderName,
            customerName: paymentData.customerName,
            customerEmail: paymentData.customerEmail,
            successUrl: paymentData.successUrl,
            failUrl: paymentData.failUrl
        }).catch(function(error) {
            // 사용자가 결제창 닫음 또는 에러
            document.getElementById('loading').classList.remove('show');
            document.getElementById('btnPay').disabled = false;

            if (error.code === 'USER_CANCEL') {
                // 사용자가 취소한 경우 - 아무것도 안 함
            } else {
                alert('결제 중 오류가 발생했습니다: ' + error.message);
            }
        });
    });
</script>

</body>
</html>
