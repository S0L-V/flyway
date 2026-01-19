<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>검색 결과 - Flyway</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/variables.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/search.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/flights.css">
</head>

<body>
<!-- Header -->
<header class="header">
    <div class="header__content">
        <div class="header__logo">
            <img src="${pageContext.request.contextPath}/resources/search/img/logo.svg" alt="Flyway" />
        </div>
        <nav class="header__menu">
            <a href="#" class="header__menu-item">로그인</a>
            <a href="#" class="header__menu-item">회원가입</a>
            <a href="#" class="header__menu-item">마이페이지</a>
        </nav>
    </div>
</header>

<!-- Banner -->
<div class="banner"></div>

<!-- Main Content -->
<main class="main-content">
    <!-- Search Section -->
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
                            <input type="date" id="dateEnd" min="2026-02-01" max="2026-12-31" data-rt-only>
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

        <div class="search-filters">
            <button class="filter-button">
                <span>항공사</span>
                <img src="${pageContext.request.contextPath}/resources/search/img/dropdown-arrow.svg" alt="" class="filter-icon" />
            </button>
            <button class="filter-button">
                <span>가격</span>
                <img src="${pageContext.request.contextPath}/resources/search/img/dropdown-arrow.svg" alt="" class="filter-icon" />
            </button>
            <button class="filter-button">
                <span>이동 시간</span>
                <img src="${pageContext.request.contextPath}/resources/search/img/dropdown-arrow.svg" alt="" class="filter-icon" />
            </button>
            <button class="filter-button">
                <span>시간대</span>
                <img src="${pageContext.request.contextPath}/resources/search/img/dropdown-arrow.svg" alt="" class="filter-icon" />
            </button>
        </div>
    </section>

    <!-- Flight Results -->
    <section class="flights-section">
        <div class="flights-header">
            <h2 class="flights-title">항공편</h2>
            <button class="sort-button">
                <img src="${pageContext.request.contextPath}/resources/search/img/sort-icon.svg" alt="" class="sort-icon" />
                <span>가격순 정렬</span>
            </button>
        </div>

        <div id="resultList" class="flights-list"></div>

    </section>
</main>
<script>
    const CONTEXT_PATH = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/resources/search/js/search.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="${pageContext.request.contextPath}/resources/search/js/flight.js?v=<%= System.currentTimeMillis() %>"></script>
</body>
</html>
