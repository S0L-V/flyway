1 <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
2 <!DOCTYPE html>
3 <html>
4 <head>
    5 <meta charset="UTF-8">
    6 <title>Toss Payments 환불 테스트</title>
    7 <style>
    8     body { font-family: sans-serif; padding: 20px; }
    9     #refundForm div { margin-bottom: 10px; }
    10     label { display: inline-block; width: 150px; }
    11     input { width: 350px; padding: 5px; }
    12     button { padding: 8px 15px; }
    13     pre { background-color: #f4f4f4; padding: 15px; border: 1px solid #ddd; }
    14 </style>
    15 </head>
16 <body>
17     <h1>Toss Payments 환불 테스트</h1>
18     <p>이 페이지는 이미 구현된 <code>POST /api/payments/{paymentId}/refund</code> API를 테스트하기 위한
    화면입니다.</p>
19
20     <form id="refundForm">
    21         <div>
    22             <label for="paymentId">paymentId (필수):</label>
    23             <input type="text" id="paymentId" name="paymentId" required>
    24         </div>
    25         <div>
    26             <label for="cancelReason">환불 사유 (필수):</label>
    27             <input type="text" id="cancelReason" name="cancelReason" required>
    28         </div>
    29         <div>
    30             <label for="cancelAmount">환불 금액 (부분 환불):</label>
    31             <input type="number" id="cancelAmount" name="cancelAmount" placeholder="전체 환불 시 비워두세요">
    32         </div>
    33         <br>
    34         <button type="submit">환불 요청</button>
    35     </form>
36
37     <hr>
38
39     <h2>API 응답 결과</h2>
40     <pre id="result"></pre>
41
42     <script>
    43         document.getElementById('refundForm').addEventListener('submit', function(e) {
        44             e.preventDefault();
        45
        46             const paymentId = document.getElementById('paymentId').value;
        47             const cancelReason = document.getElementById('cancelReason').value;
        48             const cancelAmountInput = document.getElementById('cancelAmount').value;
        49
        50             // API 요청 본문 구성
        51             const requestBody = {
            52                 cancelReason: cancelReason
        53             };
        54
        55             if (cancelAmountInput) {
            56                 requestBody.cancelAmount = Number(cancelAmountInput);
            57             }
        58
        59             const resultElement = document.getElementById('result');
        60             resultElement.textContent = '환불 요청 중...';
        61
        62             const apiUrl = '${pageContext.request.contextPath}/api/payments/' + paymentId + '/refund';
        63
        64             fetch(apiUrl, {
            65                 method: 'POST',
            66                 headers: {
            67                     'Content-Type': 'application/json'
            68                 },
        69                 body: JSON.stringify(requestBody)
        70             })
        71             .then(async response => {
            72                 const responseData = await response.json();
            73                 if (!response.ok) {
                74                     // 서버가 보낸 에러 메시지를 포함하여 에러 객체 생성
                75                     const error = new Error('API 요청 실패');
                76                     error.status = response.status;
                77                     error.data = responseData;
                78                     throw error;
                79                 }
            80                 return responseData;
            81             })
        82             .then(data => {
            83                 resultElement.textContent = JSON.stringify(data, null, 2);
            84             })
        85             .catch(error => {
            86                 console.error('Fetch Error:', error);
            87                 const errorMessage = error.data ? JSON.stringify(error.data, null, 2) : error.message;
            88                 resultElement.textContent = '오류 발생\\nStatus: ' + (error.status || 'N/A') + '\\n\\n' +
                errorMessage;
            89             });
    });
</script>
</body>
</html>