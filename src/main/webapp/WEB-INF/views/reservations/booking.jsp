<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>예매 - Flyway</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/base.css"/>
    <style>
        body { font-family: Arial, sans-serif; }
        .container {
            width: 100%;
            max-width: 900px;
            margin: 0 auto;
            padding: 20px;
            box-sizing: border-box;
        }
        .box { border: 1px solid #ddd; padding: 16px; margin: 16px 0; border-radius: 8px; }
        .muted { color: #666; }
        .btn { padding: 10px 16px; border: 1px solid #333; background: #fff; cursor: pointer; border-radius: 6px; margin-right: 8px; }
        .btn.primary { background: #1f6feb; color: #fff; border-color: #1f6feb; }
        .btn[disabled] { opacity: 0.4; cursor: not-allowed; }
        .row { display: flex; gap: 12px; flex-wrap: wrap; }
        .field { display: flex; flex-direction: column; gap: 4px; min-width: 180px; }
        input, select { padding: 8px; border: 1px solid #ccc; border-radius: 6px; }
        .ok { color: #0a7; font-weight: bold; }

        .page-header{
            display:flex;
            align-items:center;
            justify-content:space-between;
            padding: 18px 0 8px;
        }

        .page-title{
            font-size: 24px;
            font-weight: 800;
            color:#111;
        }

        /* stepper */
        .stepper{
            list-style:none;
            display:flex;
            gap: 14px;
            margin:0;
            padding:0;
            align-items:center;
        }

        .stepper__item{
            display:flex;
            flex-direction:column;
            align-items:center;
            gap: 6px;
            color:#9aa3ad;
            font-size:12px;
            font-weight:700;
        }

        .stepper__circle{
            width: 22px;
            height: 22px;
            border-radius: 999px;
            border: 1px solid #cfd6de;
            display:flex;
            align-items:center;
            justify-content:center;
            font-size: 12px;
            font-weight: 800;
            background:#fff;
            color:#9aa3ad;
        }

        .stepper__item.is-active{
            color:#1f6feb;
        }
        .stepper__item.is-active .stepper__circle{
            background:#1f6feb;
            border-color:#1f6feb;
            color:#fff;
        }
        .pre-flight-white{
            background:#fff;
        }

        /* 저장 완료 메시지: 1번 이미지의 긴 박스 대신 깔끔한 안내 */
        .toast-success{
            margin: 10px 0 0;
            padding: 12px 14px;
            border: 1px solid #e6f0ff;
            background: #f5f9ff;
            border-radius: 10px;
            color: #0a7;
            font-weight: 800;
            font-size: 13px;
        }

        /* 반응형: 모바일에서 스텝을 아래로 내림 */
        @media (max-width: 640px){
            .page-header{ flex-direction:column; align-items:flex-start; gap: 10px; }
            .stepper{ gap: 10px; }
        }
        .flight-section {
        background-color: #e8f4fc;
            margin: 0 -9999px;
            padding: 24px 9999px;
        }
        .flight-section__title {
        max-width: 900px;
            margin: 0 auto 16px auto;
            font-size: 16px;
            font-weight: bold;
            color: #111;
        }
        .flight-card {
        background: #fff;
            border-radius: 12px;
            padding: 20px 24px;
            max-width: 900px;
            margin: 0 auto;
        }
        .flight-card__title {
            font-size: 18px;
            font-weight: bold;
            margin-bottom: 16px;
        }
        .flight-row {
            display: grid;
            grid-template-columns: 110px 140px 220px 1fr 120px 1fr;
            align-items: center;
            column-gap: 18px;
            row-gap: 10px;
            padding: 18px 0;
            border-bottom: 1px solid #eee;
        }
        .flight-row:last-child {
            border-bottom: none;
        }
        .flight-row__date {
            min-width: 100px;
            font-weight: 500;
            font-size: 15px;
            font-weight: bold;
        color: #000;
        }
        .flight-row__info {
            min-width: 100px;
            font-weight: bold;
            color: #000;
        font-size: 14px;
        }
        .flight-row__airline {
            min-width: 140px;
            font-size: 14px;
            font-weight: bold;
            color: #000;
        }
        .flight-row__point {
            min-width: 80px;
            text-align: center;
        }
        .flight-row__time {
            font-size: 22px;
        font-weight: bold;
            color: #000;
        }
        .flight-row__airport {
            font-size: 14px;
            color: #333;
            margin-top: 4px;
        }
        .flight-row__duration {
            justify-self: center;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            background: #1f6feb;
            color: #fff;
            border-radius: 999px;
            padding: 6px 12px;
            font-size: 12px;
            font-weight: 700;
            line-height: 1;
            min-width: auto;
            text-align: center;
        }
        /* pill 앞에 비행기 아이콘 */
        .flight-row__duration::before {
            content: "✈";
            font-size: 12px;
            line-height: 1;
        }
        /* 구간(출발/가운데 pill/도착) 3개 블록이 grid 칼럼을 그대로 쓰도록 변경 */
        .flight-row__segment {
            display: contents;
        }
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

        @media (max-width: 1024px) {
            .flight-row {
                grid-template-columns: 90px 1fr;
                grid-template-areas:
                        "date airline"
                        "info info"
                        "segment segment";
                row-gap: 8px;
            }

            .flight-row__date { grid-area: date; }
            .flight-row__airline { grid-area: airline; justify-self: end; text-align: right; }
            .flight-row__info { grid-area: info; }
            .flight-row__segment { grid-area: segment; }

            /* 구간(출발/도착/가운데 pill)은 3칸 유지하되 폭이 좁으면 자연스럽게 줄바꿈 */
            .flight-row__segment {
                display: grid;
                grid-template-columns: 1fr auto 1fr;
                gap: 10px;
            }

            .flight-row__segment > div:nth-child(1) { justify-self: start; }
            .flight-row__segment > div:nth-child(2) { justify-self: center; }
            .flight-row__segment > div:nth-child(3) { justify-self: end; text-align: right; }

            .flight-row__airport { white-space: normal; }
        }

        /* 모바일 */
        @media (max-width: 640px) {
            .flight-row {
                /* 모바일에서는 완전 세로 스택 */
                display: block;
            }

            .flight-row__date,
            .flight-row__airline,
            .flight-row__info {
                margin-bottom: 8px;
            }

            .flight-row__airline { text-align: left; }

            .flight-row__segment {
                /* 출발/도착을 위아래로 분리 */
                display: block;
            }

            /* 출발 블록 / 도착 블록 */
            .flight-row__segment > div:nth-child(1),
            .flight-row__segment > div:nth-child(3) {
                display: flex;
                justify-content: space-between;
                align-items: baseline;
                gap: 10px;
            }

            /* 가운데 pill */
            .flight-row__segment > div:nth-child(2) {
                margin: 10px 0;
                text-align: center;
            }

            .flight-row__airport {
                text-align: right;
                word-break: keep-all;
            }
        }
        .passenger-section-title {
            font-size: 18px;
            font-weight: 800;
            margin: 0 0 10px 0;
        }

        .passenger-help {
            margin: 0 0 14px 0;
            padding-left: 18px;
            color: #666;
            font-size: 13px;
            line-height: 1.55;
        }
        .passenger-help li { margin: 6px 0; }

        #passengerForm .passenger-card {
            border: 1px solid #eee;
            border-radius: 12px;
            padding: 18px;
            margin-top: 14px;
            background: #fff;
        }

        #passengerForm .passenger-card__header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 14px;
        }

        #passengerForm .passenger-card__title {
            font-size: 16px;
            font-weight: 800;
            margin: 0;
        }

        #passengerForm .pgrid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 14px 18px;
        }

        #passengerForm .pfield { min-width: 0; }
        #passengerForm .plabel {
            display: block;
            font-size: 13px;
            font-weight: 700;
            color: #111;
            margin-bottom: 6px;
        }

        #passengerForm .pcontrol input,
        #passengerForm .pcontrol select {
            width: 100%;
            padding: 10px 12px;
            border: 1px solid #e5e5e5;
            border-radius: 6px;
            background: #fff !important;
            box-shadow: none !important;
        }
        #passengerForm input[type="date"] {
            background-color: #fff !important;
        }

        #passengerForm select {
            background-color: #fff !important;
        }

        #passengerForm .span-2 { grid-column: span 2; }

        /* 생년월일 + 성별(버튼형) 한 줄 */
        #passengerForm .birth-gender {
            display: flex;
            gap: 12px;
            align-items: flex-end;
        }
        #passengerForm .birth-gender .birth { flex: 1; }

        /* 버튼형 성별 */
        #passengerForm .segmented {
            display: inline-flex;
            border: 1px solid #ddd;
            border-radius: 10px;
            overflow: hidden;
            height: 40px;
        }
        #passengerForm .segmented input {
            position: absolute;
            opacity: 0;
            pointer-events: none;
        }
        #passengerForm .segmented label {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 0 18px;
            font-size: 13px;
            font-weight: 700;
            color: #666;
            cursor: pointer;
            background: #fff;
            border-right: 1px solid #ddd;
        }
        #passengerForm .segmented label:last-child { border-right: none; }
        #passengerForm .segmented input:checked + label {
            background: #1f6feb;
            color: #fff;
        }

        /* 여권 정보 헤더 */
        #passengerForm .passport-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin: 18px 0 10px;
        }
        #passengerForm .passport-title {
            font-size: 14px;
            font-weight: 800;
            color: #111;
        }
        #passengerForm .passport-later {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            color: #777;
            font-size: 12px;
            user-select: none;
        }
        #passengerForm .passport-later input { width: 14px; height: 14px; }
        .passenger-box {
            background: #fff !important;      /* 탑승자 정보 큰 네모칸 */
        }

        .passenger-box .passenger-card {
            background: #fff !important;      /* 탑승자 1 카드 네모칸 */
        }

        /* 혹시 base.css가 box에 그라데이션/그림자 넣으면 제거 */
        .passenger-box,
        .passenger-box .passenger-card {
            box-shadow: none !important;
        }

        /* 탑승자 섹션 내부의 모든 input/select wrapper까지 흰색으로 */
        .passenger-box .pcontrol {
            background: #fff !important;
        }
        /* 저장 버튼 위치/모양 */
        #passengerForm .save-btn {
            margin-top: 16px;
            padding: 12px 18px;
            border-radius: 8px;
        }

        /* 반응형: 모바일 1열 */
        @media (max-width: 640px) {
            #passengerForm .pgrid { grid-template-columns: 1fr; }
            #passengerForm .span-2 { grid-column: auto; }
            #passengerForm .birth-gender { flex-direction: column; align-items: stretch; }
            #passengerForm .segmented { width: 100%; }
            #passengerForm .segmented label { flex: 1; }
        }

        .notice-blue{
            margin: 10px 0 0;
            padding-left: 18px;
            color: #1f6feb;
            font-size: 12px;
            line-height: 1.55;
        }
        .notice-blue li { margin: 6px 0; }

        .page-card {
            max-width: 900px;
            margin-left: auto;
            margin-right: auto;
        }

        /* flight-section 내부에서 padding 트릭 영향 최소화 */
        .flight-card { box-sizing: border-box; }
        .container { --cp: 20px; } /* container padding 값과 맞춰야 함 */

        .container .flush-in-container{
            width: calc(100% + (var(--cp) * 2));
            margin-left: calc(var(--cp) * -1);
            margin-right: calc(var(--cp) * -1);
            box-sizing: border-box;
        }
    </style>
</head>
<body>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="pre-flight-white">
    <div class="container">
     <div class="page-header">
        <div class="page-header__left">
            <div class="page-title">예약하기</div>
        </div>

        <div class="page-header__right">
            <ol class="stepper">
                <li class="stepper__item">
                    <span class="stepper__circle">1</span>
                    <span class="stepper__label">약관동의</span>
                </li>
                <li class="stepper__item is-active">
                    <span class="stepper__circle">2</span>
                    <span class="stepper__label">정보입력/결제</span>
                </li>
                <li class="stepper__item">
                    <span class="stepper__circle">3</span>
                    <span class="stepper__label">예약결과</span>
                </li>
            </ol>
        </div>
    </div>
    </div>

<%--    <c:if test="${param.saved == '1'}">--%>
<%--        <div class="toast-success">탑승자 정보 저장 완료</div>--%>
<%--    </c:if>--%>

<!-- 예약 정보 -->
</div><!-- container 닫기 -->
<!-- 예약 항공편 (전체 너비) -->
<div class="flight-section">
    <div class="flight-section__title">예약 항공편</div>
    <div class="flight-card page-card">
        <c:forEach var="s" items="${vm.segments}">
            <div class="flight-row">
                <div class="flight-row__date">
                    <fmt:parseDate value="${s.snapDepartureTime}" pattern="yyyy-MM-dd'T'HH:mm" var="depDate"/>
                    <fmt:formatDate value="${depDate}" pattern="yyyy-MM-dd"/>
                </div>
                <div class="flight-row__info">
                    성인 ${vm.passengerCount} / ${s.snapCabinClassCode}
                </div>
                <div class="flight-row__airline">
                        ${s.snapAirlineName} ${s.snapFlightNumber}
                </div>
                <div class="flight-row__segment">
                    <div>
                        <div class="flight-row__time">
                            <fmt:formatDate value="${depDate}" pattern="HH:mm"/>
                        </div>
                        <div class="flight-row__airport">${s.snapDepartureCity}(${s.snapDepartureAirport})</div>
                    </div>
                    <div class="flight-row__duration">
                            ${s.segmentOrder == 1 ? '가는편' : '오는편'}
                    </div>
                    <div>
                        <fmt:parseDate value="${s.snapArrivalTime}" pattern="yyyy-MM-dd'T'HH:mm" var="arrDate"/>
                        <div class="flight-row__time">
                            <fmt:formatDate value="${arrDate}" pattern="HH:mm"/>
                        </div>
                        <div class="flight-row__airport">${s.snapArrivalCity}(${s.snapArrivalAirport})</div>
                    </div>
                </div>

            </div>
        </c:forEach>
        <c:if test="${empty vm.segments}">
            <div class="muted">항공편 정보가 없습니다.</div>
        </c:if>
    </div>
</div>

<div class="container"><!-- container 다시 열기 -->



<!-- 탑승자 정보 입력 -->
    <!-- 탑승자 정보 -->
    <div class="box passenger-box page-card flush-in-container">
    <div class="passenger-section-title">탑승자 정보</div>

        <ul class="passenger-help">
            <li>예약 후 영문 변경은 불가하니 실제 탑승객의 여권상 영문 이름을 입력해주세요.</li>
            <li>정보가 잘못 입력된 경우 공항에서 탑승이 거절될 수 있으며, 정보 등록에 대한 책임은 탑승객 본인에게 있음을 안내드립니다.</li>
        </ul>

        <c:url var="saveUrl" value="/reservations/${vm.reservationId}/passengers"/>

        <form id="passengerForm" onsubmit="return savePassengers(event)">
            <c:forEach var="p" items="${vm.passengers}" varStatus="st">

                <div class="passenger-card">
                    <div class="passenger-card__header">
                        <h4 class="passenger-card__title">탑승자 ${st.index + 1}</h4>
                    </div>

                    <input type="hidden" name="passengers[${st.index}].passengerId" value="${p.passengerId}"/>

                    <div class="pgrid">
                        <!-- 한글 -->
                        <div class="pfield">
                            <label class="plabel">한글 성</label>
                            <div class="pcontrol">
                                <input name="passengers[${st.index}].krLastName" value="${p.krLastName}" placeholder="한글 성" required />
                            </div>
                        </div>
                        <div class="pfield">
                            <label class="plabel">한글 이름</label>
                            <div class="pcontrol">
                                <input name="passengers[${st.index}].krFirstName" value="${p.krFirstName}" placeholder="한글 이름" required />
                            </div>
                        </div>

                        <!-- 영문 -->
                        <div class="pfield">
                            <label class="plabel">영문 성</label>
                            <div class="pcontrol">
                                <input name="passengers[${st.index}].lastName" value="${p.lastName}" placeholder="영문 성" required />
                            </div>
                        </div>
                        <div class="pfield">
                            <label class="plabel">영문 이름</label>
                            <div class="pcontrol">
                                <input name="passengers[${st.index}].firstName" value="${p.firstName}" placeholder="영문 이름" required />
                            </div>
                        </div>

                        <!-- 생년월일(가로 전체) + 성별(버튼형) -->
                        <div class="pfield span-2">
                            <div class="birth-gender">
                                <div class="birth">
                                    <label class="plabel">생년월일</label>
                                    <div class="pcontrol">
                                        <input type="date" name="passengers[${st.index}].birth" value="${p.birth}" required />
                                    </div>
                                </div>

                                <div>
                                    <label class="plabel">성별</label>
                                    <div class="segmented" role="group" aria-label="gender">
                                        <input id="gM_${st.index}" type="radio"
                                               name="passengers[${st.index}].gender"
                                               value="M" <c:if test="${p.gender == 'M'}">checked</c:if> />
                                        <label for="gM_${st.index}">남</label>

                                        <input id="gF_${st.index}" type="radio"
                                               name="passengers[${st.index}].gender"
                                               value="F" <c:if test="${p.gender == 'F'}">checked</c:if> />
                                        <label for="gF_${st.index}">여</label>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 연락처/이메일 -->
                        <div class="pfield">
                            <label class="plabel">연락처</label>
                            <div class="pcontrol">
                                <input name="passengers[${st.index}].phoneNumber" value="${p.phoneNumber}" placeholder="- 제외 연락처" required />
                            </div>
                        </div>
                        <div class="pfield">
                            <label class="plabel">이메일</label>
                            <div class="pcontrol">
                                <input type="email" name="passengers[${st.index}].email" value="${p.email}" placeholder="이메일" required />
                            </div>
                        </div>
                    </div>

                    <!-- 여권 정보 헤더 -->
                    <div class="passport-header">
                        <div class="passport-title">여권 정보</div>
                    </div>

                    <!-- 여권 입력 (2열) -->
                    <div class="pgrid" id="passportGrid_${st.index}">
                        <div class="pfield">
                            <label class="plabel">여권번호</label>
                            <div class="pcontrol">
                                <input name="passengers[${st.index}].passportNo" value="${p.passportNo}" placeholder="여권번호" />
                            </div>
                        </div>
                        <div class="pfield">
                            <label class="plabel">여권만료일</label>
                            <div class="pcontrol">
                                <input type="date" name="passengers[${st.index}].passportExpiryDate" value="${p.passportExpiryDate}" />
                            </div>
                        </div>

                        <!-- 요구사항대로 국적 입력칸 유지(내국/외국 버튼 없음) -->
                        <div class="pfield">
                            <label class="plabel">국적</label>
                            <div class="pcontrol">
                                <input name="passengers[${st.index}].country" value="${p.country}" placeholder="대한민국" />
                            </div>
                        </div>
                        <div class="pfield">
                            <label class="plabel">발행국</label>
                            <div class="pcontrol">
                                <input name="passengers[${st.index}].passportIssueCountry" value="${p.passportIssueCountry}" placeholder="대한민국" />
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>

            <button class="btn primary save-btn" type="submit">탑승자 정보 저장</button>
        </form>
    </div>

    <ul class="notice-blue flush-in-container">
        <li>여권 유효기간은 출발일 기준으로 6개월 이상 남아 있어야 합니다.</li>
        <li>여권 정보 수정은 가능하나, 일부 항공사의 경우 여권 정보 수정 기한 및 수정 횟수가 제한될 수 있습니다.</li>
        <li>여권 정보를 나중에 등록하는 경우, 임의의 여권 번호와 만료일이 임시로 등록됩니다.</li>
    </ul>

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

    <!-- 좌석 상세 -->
    <c:forEach var="s" items="${vm.segments}">
        <c:if test="${not empty s.passengerSeats}">
            <div class="price-row" style="padding-left: 20px; font-size: 13px; color: #666;">
                  <span class="price-label">${s.segmentOrder == 1 ? '가는편' : '오는편'}:
                      <c:forEach var="seat" items="${s.passengerSeats}" varStatus="st">
                          ${seat.passengerName} ${seat.seatNo}<c:if test="${!st.last}">, </c:if>
                      </c:forEach>
                  </span>
            </div>
        </c:if>
    </c:forEach>

    <div class="price-row">
        <span class="price-label">부가서비스</span>
        <span class="price-value" id="servicePrice">₩0</span>
    </div>
    <!-- 부가서비스 상세 (수하물) -->
    <c:forEach var="s" items="${vm.segments}">
        <c:set var="hasBaggage" value="false"/>
        <c:forEach var="svc" items="${s.passengerServices}">
            <c:if test="${svc.serviceType == '0'}"><c:set var="hasBaggage" value="true"/></c:if>
        </c:forEach>
        <c:if test="${hasBaggage}">
            <div class="price-row" style="padding-left: 20px; font-size: 13px; color: #666;">
                  <span class="price-label">${s.segmentOrder == 1 ? '가는편' : '오는편'} 수하물:
                      <c:forEach var="svc" items="${s.passengerServices}" varStatus="st">
                          <c:if test="${svc.serviceType == '0'}">
                              ${svc.passengerName} ${svc.serviceName}<c:if test="${!st.last}">, </c:if>
                          </c:if>
                      </c:forEach>
                  </span>
            </div>
        </c:if>
    </c:forEach>
    <!-- 부가서비스 상세 (기내식) -->
    <c:forEach var="s" items="${vm.segments}">
        <c:set var="hasMeal" value="false"/>
        <c:forEach var="svc" items="${s.passengerServices}">
            <c:if test="${svc.serviceType == '1'}"><c:set var="hasMeal" value="true"/></c:if>
        </c:forEach>
        <c:if test="${hasMeal}">
            <div class="price-row" style="padding-left: 20px; font-size: 13px; color: #666;">
                  <span class="price-label">${s.segmentOrder == 1 ? '가는편' : '오는편'} 기내식:
                      <c:forEach var="svc" items="${s.passengerServices}" varStatus="st">
                          <c:if test="${svc.serviceType == '1'}">
                              ${svc.passengerName} ${svc.serviceName}<c:if test="${!st.last}">, </c:if>
                          </c:if>
                      </c:forEach>
                  </span>
            </div>
        </c:if>
    </c:forEach>

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
</div>
<script type="module">
    import { fetchWithRefresh } from '/resources/common/js/authFetch.js';
    var reservationId = '${vm.reservationId}';
    var passengerSaved = ${vm.passengerSaved};
    var passengerCount = ${vm.passengerCount};
    var segments = [
        <c:forEach var="s" items="${vm.segments}" varStatus="st">
        { snapPrice: ${s.snapPrice != null ? s.snapPrice : 0} }<c:if test="${!st.last}">,</c:if>
        </c:forEach>
    ];

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
        var service = parsePrice(document.getElementById('servicePrice').textContent);
        var total = flight + service;
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
        // 결제 페이지로 이동 (PaymentController)
        location.href = '/payments/' + reservationId;
    }


    // 페이지 로드 시 부가서비스 총액 조회
    document.addEventListener('DOMContentLoaded', function() {
        var flightTotal = 0;
        segments.forEach(function(seg) {
            flightTotal += seg.snapPrice;
        });
        flightTotal *= passengerCount;
        document.getElementById('flightPrice').textContent = '₩' + numberWithCommas(flightTotal);

        if (passengerSaved) {
            refreshServiceTotal();
        }
        updateTotalPrice();
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

            const genderEl = document.querySelector(
                'input[name="passengers[' + index + '].gender"]:checked'
            );

            passengers.push({
                passengerId: passengerIdEl.value,
                krLastName: document.querySelector('input[name="passengers[' + index + '].krLastName"]').value,
                krFirstName: document.querySelector('input[name="passengers[' + index + '].krFirstName"]').value,
                lastName: document.querySelector('input[name="passengers[' + index + '].lastName"]').value,
                firstName: document.querySelector('input[name="passengers[' + index + '].firstName"]').value,
                birth: document.querySelector('input[name="passengers[' + index + '].birth"]').value,


                gender: genderEl ? genderEl.value : '',

                email: document.querySelector('input[name="passengers[' + index + '].email"]').value,
                phoneNumber: document.querySelector('input[name="passengers[' + index + '].phoneNumber"]').value,
                passportNo: document.querySelector('input[name="passengers[' + index + '].passportNo"]').value,
                country: document.querySelector('input[name="passengers[' + index + '].country"]').value,
                passportExpiryDate: document.querySelector('input[name="passengers[' + index + '].passportExpiryDate"]').value,
                passportIssueCountry: document.querySelector('input[name="passengers[' + index + '].passportIssueCountry"]').value
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