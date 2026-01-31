<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>flyway - 항공권 예약</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/search.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/details.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/flights.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/main/css/main.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/main/css/promotion.css?v=<%= System.currentTimeMillis() %>">
</head>
<body>
<!-- Header -->
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<!-- Hero Banner -->
<div class="hero-banner"></div>

<!-- Main Content -->
<div class="container">
    <div class="content">
        <!-- Search Box -->
        <section class="search-section">
            <div class="search-tabs">
                <button class="search-tab search-tab--active" data-trip="RT">왕복</button>
                <button class="search-tab" data-trip="OW">편도</button>
            </div>

            <div class="search-inputs" id="searchBox">

                <!-- 출발 공항 -->
                <div class="field dropdown" data-field="from">
                    <button type="button" class="search-input dropdown-toggle" aria-expanded="false">
                        <span class="value" data-value>출발 공항 선택</span>
                        <span class="hint" data-hint></span>
                    </button>

                    <div class="dropdown-panel" hidden>
                        <input class="dropdown-search" type="text" placeholder="공항 검색 (예: ICN, 인천)" autocomplete="off">
                        <ul class="dropdown-list" data-list></ul>
                    </div>
                </div>

                <!-- 도착 공항 -->
                <div class="field dropdown" data-field="to">
                    <button type="button" class="search-input dropdown-toggle" aria-expanded="false">
                        <span class="value" data-value>도착 공항 선택</span>
                        <span class="hint" data-hint></span>
                    </button>

                    <div class="dropdown-panel" hidden>
                        <input class="dropdown-search" type="text" placeholder="공항 검색 (예: NRT, 나리타)" autocomplete="off">
                        <ul class="dropdown-list" data-list></ul>
                    </div>
                </div>

                <!-- 날짜 -->
                <div class="field dropdown" data-field="dates">
                    <button type="button" class="search-input dropdown-toggle" aria-expanded="false">
                        <span class="value" data-value>날짜 선택</span>
                        <span class="hint" data-hint></span>
                    </button>

                    <div class="dropdown-panel" hidden>
                        <div class="date-row">
                            <label>
                                출발일
                                <input type="date" id="dateStart" min="2026-02-01" max="2026-12-31">
                            </label>
                            <label data-rt-only>
                                도착일
                                <input type="date" id="dateEnd" min="2026-02-01" max="2027-01-02" data-rt-only>
                            </label>
                        </div>
                        <p class="date-help" id="dateError" hidden>도착일은 출발일보다 같거나 이후여야 해요.</p>
                        <div class="panel-actions">
                            <button type="button" class="btn" data-action="applyDates">적용</button>
                        </div>
                    </div>
                </div>

                <!-- 인원 + 좌석 -->
                <div class="field dropdown" data-field="paxCabin">
                    <button type="button" class="search-input dropdown-toggle" aria-expanded="false">
                        <span class="value" data-value>탑승 인원 / 좌석 선택</span>
                        <span class="hint" data-hint></span>
                    </button>

                    <div class="dropdown-panel" hidden>
                        <div class="pax-row">
                            <span class="label">탑승 인원</span>
                            <div class="stepper">
                                <button type="button" class="stepper-btn" data-action="dec">-</button>
                                <span class="stepper-value" id="paxCount">1</span>
                                <button type="button" class="stepper-btn" data-action="inc">+</button>
                            </div>
                        </div>

                        <div class="cabin-row">
                            <span class="label">좌석 등급</span>
                            <div class="cabin-options">
                                <label class="chip"><input type="radio" name="cabin" value="FST"> 퍼스트</label>
                                <label class="chip"><input type="radio" name="cabin" value="BIZ"> 비즈니스</label>
                                <label class="chip"><input type="radio" name="cabin" value="ECO" checked> 이코노미</label>
                            </div>
                        </div>

                        <div class="panel-actions">
                            <button type="button" class="btn" data-action="applyPaxCabin">적용</button>
                        </div>
                    </div>
                </div>

                <button class="search-button" id="btnSearch" type="button">검색</button>
            </div>
        </section>

        <section class="special-offers-section">
            <h2 class="section-title">✨ 지금 떠나면 이 가격! 특가 항공권</h2>

            <div class="slider-wrapper" id="slider-wrapper">
                <button class="nav-btn prev" id="offerPrev">&lt;</button>
                <button class="nav-btn next" id="offerNext">&gt;</button>

                <div class="main-slider-track is-entering" id="offerTrack">

                </div>
            </div>
        </section>

        <section class="trending-section">
            <div class="trending-header">
                <h2 class="section-title">
                    여행지 인기 급상승 <span class="badge-live">LIVE</span>
                </h2>
                <a href="#" class="view-all-link">실시간 데이터 상세 보기 →</a>
            </div>
            <p class="trending-desc">"최근 일주일, 가장 많이 검색된 도시들"</p>

            <div class="trending-grid">

            </div>
        </section>
    </div>
</div>

<script>
    const CONTEXT_PATH = "${pageContext.request.contextPath}";
</script>

<script src="${pageContext.request.contextPath}/resources/search/js/search.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="${pageContext.request.contextPath}/resources/main/js/main.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="${pageContext.request.contextPath}/resources/main/js/promotion.js?v=<%= System.currentTimeMillis() %>"></script>

<%--예약 폼--%>
<form id="reservationForm" action="${pageContext.request.contextPath}/reservations/draft" method="POST"
      style="display:none;">
    <sec:csrfInput />
    <input type="hidden" name="outFlightId" id="hiddenOutFlightId">
    <input type="hidden" name="inFlightId" id="hiddenInFlightId">
    <input type="hidden" name="passengerCount" id="hiddenPassengerCount">
    <input type="hidden" name="cabinClassCode" id="hiddenCabinClassCode">
    <input type="hidden" name="outPrice" id="hiddenOutPrice">
    <input type="hidden" name="inPrice" id="hiddenInPrice">
</form>
</body>
</html>
