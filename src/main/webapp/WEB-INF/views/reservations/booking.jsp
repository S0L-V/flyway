<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Booking</title>
    <style>
        body { font-family: Arial, sans-serif; }
        .box { border: 1px solid #ddd; padding: 12px; margin: 12px 0; border-radius: 8px; }
        .muted { color: #666; }
        .btn { padding: 8px 12px; border: 1px solid #333; background: #fff; cursor: pointer; border-radius: 6px; }
        .btn.primary { background: #1f6feb; color: #fff; border-color: #1f6feb; }
        .btn[disabled] { opacity: 0.4; cursor: not-allowed; }
        .row { display: flex; gap: 12px; flex-wrap: wrap; }
        .field { display: flex; flex-direction: column; gap: 4px; min-width: 180px; }
        input, select { padding: 8px; border: 1px solid #ccc; border-radius: 6px; }
        .ok { color: #0a7; font-weight: bold; }
    </style>
</head>
<body>

<h2>예매 페이지</h2>

<c:if test="${param.saved == '1'}">
    <div class="box ok">탑승자 정보 저장 완료</div>
</c:if>

<div class="box">
    <div><b>reservationId</b>: ${vm.reservationId}</div>
    <div><b>status</b>: ${vm.status} / <b>tripType</b>: ${vm.tripType}</div>
    <div><b>passengerCount</b>: ${vm.passengerCount}</div>
    <div class="muted"><b>expiredAt</b>: ${vm.expiredAt}</div>
</div>

<div class="box">
    <h3>예약 항공편(스냅샷)</h3>

    <c:forEach var="s" items="${vm.segments}">
        <div class="box">
            <div><b>구간</b>: ${s.segmentOrder}</div>
            <div>${s.snapDepartureAirport} → ${s.snapArrivalAirport}</div>
            <div class="muted">${s.snapDepartureTime} ~ ${s.snapArrivalTime}</div>
            <div>편명: ${s.snapFlightNumber} / 등급: ${s.snapCabinClassCode}</div>
        </div>
    </c:forEach>

    <c:if test="${empty vm.segments}">
        <div class="muted">segment가 없습니다. (draft 생성이 정상인지 확인)</div>
    </c:if>
</div>

<div class="box">
    <h3>탑승자 정보 입력 </h3>

    <c:url var="saveUrl" value="/reservations/${vm.reservationId}/passengers"/>

    <form method="post" action="${saveUrl}">
        <c:forEach var="p" items="${vm.passengers}" varStatus="st">
            <div class="box">
                <h4>탑승자 ${st.index + 1}</h4>

                <!-- hidden passengerId (있으면 update) -->
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

<div class="box">
    <div>passengerSaved = <b>${vm.passengerSaved}</b></div>

    <button class="btn" ${vm.passengerSaved ? "" : "disabled"}
            onclick="alert('좌석 팝업 미연결')">
        좌석 선택
    </button>

    <button class="btn" ${vm.passengerSaved ? "" : "disabled"}
            onclick="alert('부가서비스 팝업 미연결')">
        수하물/기내식
    </button>
</div>

</body>
</html>
