<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>예매 - Flyway</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 900px; margin: 0 auto; padding: 20px; }
        .box { border: 1px solid #ddd; padding: 16px; margin: 16px 0; border-radius: 8px; }
        .muted { color: #666; }
        .btn { padding: 10px 16px; border: 1px solid #333; background: #fff; cursor: pointer; border-radius: 6px; margin-right: 8px; }
        .btn.primary { background: #1f6feb; color: #fff; border-color: #1f6feb; }
        .btn[disabled] { opacity: 0.4; cursor: not-allowed; }
        .row { display: flex; gap: 12px; flex-wrap: wrap; }
        .field { display: flex; flex-direction: column; gap: 4px; min-width: 180px; }
        input, select { padding: 8px; border: 1px solid #ccc; border-radius: 6px; }
        .ok { color: #0a7; font-weight: bold; }

        /* 결제 금액 섹션 */
        .price-section { background: #f9f9f9; }
        .price-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #eee; }
        .price-row:last-child { border-bottom: none; }
        .price-row.total { font-weight: bold; font-size: 1.2em; color: #1f6feb; border-top: 2px solid #1f6feb; margin-top: 8px; padding-top: 12px; }
        .price-label { color: #555; }
        .price-value { text-align: right; }

        /* 버튼 그룹 */
        .btn-group { display: flex; gap: 12px; margin-top: 16px; }
        .btn-group .btn { flex: 1; text-align: center; padding: 12px; }
    </style>
</head>
<body>

<h2>예매 페이지</h2>

<c:if test="${param.saved == '1'}">
    <div class="box ok">탑승자 정보 저장 완료</div>
</c:if>

<!-- 예약 정보 -->
<div class="box">
    <div><b>예약번호</b>: ${vm.reservationId}</div>
    <div><b>상태</b>: ${vm.status} / <b>여정</b>: ${vm.tripType == '1' ? '왕복' : '편도'}</div>
    <div><b>탑승 인원</b>: ${vm.passengerCount}명</div>
    <div class="muted"><b>예약 만료</b>: ${vm.expiredAt}</div>
</div>

<!-- 예약 항공편 -->
<div class="box">
    <h3>예약 항공편</h3>
    <c:forEach var="s" items="${vm.segments}">
        <div class="box">
            <div><b>구간 ${s.segmentOrder}</b>: ${s.segmentOrder == 1 ? '가는편' : '오는편'}</div>
            <div style="font-size: 1.1em;">${s.snapDepartureAirport} → ${s.snapArrivalAirport}</div>
            <div class="muted">${s.snapDepartureTime} ~ ${s.snapArrivalTime}</div>
            <div>편명: ${s.snapFlightNumber} / 좌석등급: ${s.snapCabinClassCode}</div>
        </div>
    </c:forEach>
    <c:if test="${empty vm.segments}">
        <div class="muted">segment가 없습니다.</div>
    </c:if>
</div>

<!-- 탑승자 정보 입력 -->
<div class="box">
    <h3>탑승자 정보 입력</h3>

    <c:url var="saveUrl" value="/reservations/${vm.reservationId}/passengers"/>

    <form id="passengerForm" onsubmit="return savePassengers(event)">
        <c:forEach var="p" items="${vm.passengers}" varStatus="st">
            <div class="box">
                <h4>탑승자 ${st.index + 1}</h4>

                <input type="hidden" name="passengers[${st.index}].passengerId" value="${p.passengerId}"/>

                <div class="row">
                    <div class="field">
                        <label>한글 성</label>
                        <input name="passengers[${st.index}].krLastName" value="${p.krLastName}" required />
                    </div>
                    <div class="field">
                        <label>한글 이름</label>
                        <input name="passengers[${st.index}].krFirstName" value="${p.krFirstName}" required />
                    </div>
                    <div class="field">
                        <label>영문 성</label>
                        <input name="passengers[${st.index}].lastName" value="${p.lastName}" required />
                    </div>
                    <div class="field">
                        <label>영문 이름</label>
                        <input name="passengers[${st.index}].firstName" value="${p.firstName}" required />
                    </div>
                    <div class="field">
                        <label>생년월일</label>
                        <input type="date" name="passengers[${st.index}].birth" value="${p.birth}" required />
                    </div>
                    <div class="field">
                        <label>성별</label>
                        <select name="passengers[${st.index}].gender" required>
                            <option value="">선택</option>
                            <option value="M" <c:if test="${p.gender == 'M'}">selected</c:if>>남</option>
                            <option value="F" <c:if test="${p.gender == 'F'}">selected</c:if>>여</option>
                        </select>
                    </div>
                    <div class="field">
                        <label>이메일</label>
                        <input type="email" name="passengers[${st.index}].email" value="${p.email}" required />
                    </div>
                    <div class="field">
                        <label>휴대폰</label>
                        <input name="passengers[${st.index}].phoneNumber" value="${p.phoneNumber}" required />
                    </div>
                </div>

                <div class="row" style="margin-top:10px;">
                    <div class="field">
                        <label>여권번호(선택)</label>
                        <input name="passengers[${st.index}].passportNo" value="${p.passportNo}" />
                    </div>
                    <div class="field">
                        <label>국적(선택)</label>
                        <input name="passengers[${st.index}].country" value="${p.country}" />
                    </div>
                    <div class="field">
                        <label>여권만료일(선택)</label>
                        <input type="date" name="passengers[${st.index}].passportExpiryDate" value="${p.passportExpiryDate}" />
                    </div>
                    <div class="field">
                        <label>발급국(선택)</label>
                        <input name="passengers[${st.index}].passportIssueCountry" value="${p.passportIssueCountry}" />
                    </div>
                </div>
            </div>
        </c:forEach>

        <button class="btn primary" type="submit">탑승자 정보 저장</button>
    </form>
</div>

<!-- 좌석/부가서비스 선택 -->
<div class="box">
    <h3>사전 좌석 지정 / 부가 서비스</h3>
    <div class="muted" style="margin-bottom: 12px;">
        탑승자 정보 저장 후 선택 가능합니다. (현재: ${vm.passengerSaved ? '저장 완료' : '미저장'})
    </div>

    <button class="btn" ${vm.passengerSaved ? "" : "disabled"}
            onclick="openSeatPopup()">
        좌석 선택
    </button>

    <button class="btn" ${vm.passengerSaved ? "" : "disabled"}
            onclick="openServicePopup()">
        수하물 / 기내식 추가
    </button>
</div>

<!-- 결제 금액 -->
<div class="box price-section">
    <h3>결제 금액</h3>

    <div class="price-row">
        <span class="price-label">항공 운임</span>
        <span class="price-value" id="flightPrice">₩0</span>
    </div>
    <div class="price-row">
        <span class="price-label">좌석 추가금</span>
        <span class="price-value" id="seatPrice">₩0</span>
    </div>
    <div class="price-row">
        <span class="price-label">부가서비스</span>
        <span class="price-value" id="servicePrice">₩0</span>
    </div>
    <div class="price-row total">
        <span class="price-label">총 결제 금액</span>
        <span class="price-value" id="totalPrice">₩0</span>
    </div>

    <div class="btn-group">
        <button class="btn" onclick="history.back()">취소</button>
        <button class="btn primary" ${vm.passengerSaved ? "" : "disabled"}
                onclick="goPayment()" id="payBtn">
            결제하기
        </button>
    </div>
</div>

<script type="module">
    import { fetchWithRefresh } from '/resources/common/authFetch.js';
    var reservationId = '${vm.reservationId}';
    var passengerSaved = ${vm.passengerSaved};

    // 좌석 팝업 (팀원 담당)
    function openSeatPopup() {
        if (!passengerSaved) {
            alert('탑승자 정보를 먼저 저장해주세요.');
            return;
        }
        // TODO: 좌석 팝업 URL (팀원이 구현)
        alert('좌석 팝업 - 팀원 담당');
    }

    // 부가서비스 팝업 열기
    function openServicePopup() {
        if (!passengerSaved) {
            alert('탑승자 정보를 먼저 저장해주세요.');
            return;
        }

        var popupUrl = '/reservations/' + reservationId + '/services';
        var popupOption = 'width=800,height=700,scrollbars=yes,resizable=yes';
        window.open(popupUrl, 'servicePopup', popupOption);
    }

    // 부가서비스 총액 갱신 (팝업에서 호출)
    async function refreshServiceTotal() {
        try {
            const res = await fetchWithRefresh('/reservations/' + reservationId + '/services/total');
            const data = await res.json();
            if (data.success) {
                document.getElementById('servicePrice').textContent = '₩' + numberWithCommas(data.total);
                updateTotalPrice();
            }
        } catch (err) {
            console.error('Failed to refresh service total', err);
        }
    }
    // 총 금액 업데이트
    function updateTotalPrice() {
        var flight = parsePrice(document.getElementById('flightPrice').textContent);
        var seat = parsePrice(document.getElementById('seatPrice').textContent);
        var service = parsePrice(document.getElementById('servicePrice').textContent);
        var total = flight + seat + service;
        document.getElementById('totalPrice').textContent = '₩' + numberWithCommas(total);
    }

    // 가격 문자열 파싱
    function parsePrice(str) {
        return parseInt(str.replace(/[₩,]/g, '')) || 0;
    }

    // 숫자 콤마 포맷
    function numberWithCommas(x) {
        return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    }

    // 결제 페이지 이동
    function goPayment() {
        if (!passengerSaved) {
            alert('탑승자 정보를 먼저 저장해주세요.');
            return;
        }
        location.href = '/reservations/' + reservationId + '/payment';
    }

    // 페이지 로드 시 부가서비스 총액 조회
    document.addEventListener('DOMContentLoaded', function() {
        if (passengerSaved) {
            refreshServiceTotal();
        }
    });
    window.openSeatPopup = openSeatPopup;
    window.openServicePopup = openServicePopup;
    window.refreshServiceTotal = refreshServiceTotal;
    window.goPayment = goPayment;

    async function savePassengers(event) {
        event.preventDefault();

        const passengers = [];
        let index = 0;

        while (true) {
            const passengerIdEl = document.querySelector('input[name="passengers[' + index + '].passengerId"]');
            if (!passengerIdEl) break;

            passengers.push({
                passengerId: passengerIdEl.value,
                krLastName: document.querySelector('input[name="passengers[' + index + '].krLastName"]').value,
                krFirstName: document.querySelector('input[name="passengers[' + index + '].krFirstName"]').value,
                lastName: document.querySelector('input[name="passengers[' + index + '].lastName"]').value,
                firstName: document.querySelector('input[name="passengers[' + index + '].firstName"]').value,
                birth: document.querySelector('input[name="passengers[' + index + '].birth"]').value,
                gender: document.querySelector('select[name="passengers[' + index + '].gender"]').value,
                email: document.querySelector('input[name="passengers[' + index + '].email"]').value,
                phoneNumber: document.querySelector('input[name="passengers[' + index + '].phoneNumber"]').value,
                passportNo: document.querySelector('input[name="passengers[' + index + '].passportNo"]').value,
                country: document.querySelector('input[name="passengers[' + index + '].country"]').value,
                passportExpiryDate: document.querySelector('input[name="passengers[' + index +
                    '].passportExpiryDate"]').value,
                passportIssueCountry: document.querySelector('input[name="passengers[' + index +
                    '].passportIssueCountry"]').value
            });
            index++;
        }

        try {
            const res = await fetchWithRefresh('/reservations/' + reservationId + '/passengers/api', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ passengers: passengers })
            });

            const data = await res.json();

            if (data.success) {
                location.href = '/reservations/' + reservationId + '/booking?saved=1';
            } else {
                alert('저장 실패: ' + (data.message || '오류가 발생했습니다.'));
            }
        } catch (err) {
            alert('저장 실패: ' + err.message);
        }

        return false;
    }
    window.savePassengers = savePassengers;
</script>

</body>
</html>