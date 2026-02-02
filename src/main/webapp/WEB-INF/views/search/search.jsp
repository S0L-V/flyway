<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <jsp:include page="/WEB-INF/views/common/head.jsp" />
    <title>검색 결과 - Flyway</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/search.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/details.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/flights.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/main/css/hero.css?v=<%= System.currentTimeMillis() %>">
    <!-- Pretendard Font -->
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- Flatpickr -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/themes/material_blue.css">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        /* search.jsp 전용 스타일 오버라이드 */
        body {
            background-color: #f5f7fb;
            font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, Roboto, sans-serif;
        }
        .main-content {
            padding-top: 120px; /* 헤더 높이만큼 내림 */
            padding-bottom: 60px;
            min-height: 100vh;
        }

        .search-container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 20px;
            margin-bottom: 24px;
        }

        /* 필터 및 정렬 행 */
        .filter-sort-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 24px;
            flex-wrap: wrap;
            gap: 12px;
        }

        .search-filters {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }

        /* Flight.jsp 스타일의 필터 버튼 */
        .filter-button {
            padding: 10px 12px 10px 16px;
            border-radius: 9999px;
            border: 1px solid #e5e7eb;
            background-color: #ffffff;
            font-size: 14px;
            font-weight: 700;
            color: #374151;
            transition: all 0.2s;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
            display: flex;
            align-items: center;
            gap: 8px;
            cursor: pointer;
            font-family: 'Pretendard', sans-serif;
        }
        .filter-button:hover {
            border-color: #bfdbfe;
            background-color: rgba(239, 246, 255, 0.5);
        }
        .filter-button.active {
            border-color: #1f6feb;
            color: #1f6feb;
            background-color: #eff6ff;
        }

        .filter-icon-circle {
            width: 20px;
            height: 20px;
            border-radius: 9999px;
            background-color: #f3f4f6;
            color: #6b7280;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: background-color 0.2s, color 0.2s;
        }
        .filter-button:hover .filter-icon-circle {
            background-color: #dbeafe;
            color: #2563eb;
        }

        /* 정렬 버튼 */
        .sort-container {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-left: auto;
        }
        .result-count {
            font-size: 14px;
            font-weight: 500;
            color: #6b7280;
            font-family: 'Pretendard', sans-serif;
        }
        .result-count strong {
            color: #111827;
        }
        .divider {
            height: 16px;
            width: 1px;
            background-color: #d1d5db;
        }
        .sort-button {
            display: flex;
            align-items: center;
            gap: 6px;
            padding: 6px 12px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 700;
            color: #2563eb;
            background: transparent;
            border: none;
            cursor: pointer;
            transition: all 0.2s;
            font-family: 'Pretendard', sans-serif;
        }
        .sort-button:hover {
            background-color: #ffffff;
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
        }

        /* Flights Header (Title only) */
        .flights-header {
            max-width: 1200px;
            margin: 0 auto 12px;
            padding: 0 20px;
        }
        .flights-title {
            font-size: 18px;
            font-weight: 800;
            color: #1e293b;
            font-family: 'Pretendard', sans-serif;
        }
    </style>
</head>

<body>
<!-- Header -->
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<!-- Main Content -->
<main class="main-content">

    <div class="search-container">
        <!-- Trip Type Selector -->
        <div class="trip-selector">
            <button class="trip-btn active" data-trip="RT">왕복</button>
            <button class="trip-btn" data-trip="OW">편도</button>
        </div>

        <!-- Search Bar (Home Style) -->
        <div class="search-glass-panel">
            <div class="search-grid" id="searchBox">

                <!-- 출발 공항 -->
                <div class="search-field" data-field="from">
                    <label class="field-label" id="label-from">Departure</label>
                    <div class="field-input dropdown-toggle"
                         role="button" tabindex="0"
                         aria-expanded="false" aria-labelledby="label-from"
                         aria-haspopup="listbox">
                        <div class="field-icon departure">
                            <i class="fa-solid fa-plane-departure"></i>
                        </div>
                        <span class="field-value" data-value>출발지 선택</span>
                    </div>
                    <div class="dropdown-panel" hidden>
                        <input class="dropdown-search" type="text" placeholder="공항 검색 (예: ICN, 인천)" autocomplete="off">
                        <ul class="dropdown-list" data-list role="listbox"></ul>
                    </div>
                </div>

                <!-- 도착 공항 -->
                <div class="search-field" data-field="to">
                    <label class="field-label" id="label-to">Destination</label>
                    <div class="field-input dropdown-toggle"
                         role="button" tabindex="0"
                         aria-expanded="false" aria-labelledby="label-to"
                         aria-haspopup="listbox">
                        <div class="field-icon">
                            <i class="fa-solid fa-location-dot"></i>
                        </div>
                        <span class="field-value" data-value>도착지 선택</span>
                    </div>
                    <div class="dropdown-panel" hidden>
                        <input class="dropdown-search" type="text" placeholder="공항 검색 (예: NRT, 나리타)" autocomplete="off">
                        <ul class="dropdown-list" data-list role="listbox"></ul>
                    </div>
                </div>

                <!-- 날짜 -->
                <div class="search-field date-field" data-field="dates">
                    <label class="field-label" id="label-dates">Dates</label>
                    <div class="field-input date-input-wrap" id="dateFieldWrap"
                         role="button" tabindex="0"
                         aria-labelledby="label-dates">
                        <div class="field-icon">
                            <i class="fa-regular fa-calendar"></i>
                        </div>
                        <div class="date-display-text">
                            <span class="date-range-text" id="dateRangeText">날짜를 선택하세요</span>
                        </div>
                        <input type="hidden" id="dateStart">
                        <input type="hidden" id="dateEnd">
                    </div>
                    <p class="date-help" id="dateError" hidden>도착일은 출발일보다 같거나 이후여야 해요.</p>
                </div>

                <!-- 인원 + 좌석 -->
                <div class="search-field pax-field" data-field="paxCabin">
                    <label class="field-label" id="label-pax">Travelers</label>
                    <div class="field-input dropdown-toggle"
                         role="button" tabindex="0"
                         aria-expanded="false" aria-labelledby="label-pax"
                         aria-haspopup="true">
                        <div class="field-icon">
                            <i class="fa-solid fa-user"></i>
                        </div>
                        <span class="field-value" data-value>1명 / 이코노미</span>
                    </div>
                    <div class="dropdown-panel" hidden>
                        <div class="pax-row">
                            <span class="label">탑승 인원</span>
                            <div class="stepper">
                                <button type="button" class="stepper-btn" data-action="dec">-</button>
                                <span class="stepper-value" id="paxCount">1</span>
                                <button type="button" class="stepper-btn" data-action="inc">+</button>
                            </div>
                        </div>
                        <div class="cabin-section">
                            <span class="label">좌석 등급</span>
                            <div class="cabin-cards">
                                <label class="cabin-card" data-cabin="FST">
                                    <input type="radio" name="cabin" value="FST">
                                    <div class="cabin-card-inner">
                                        <div class="cabin-icon">
                                            <i class="fa-solid fa-crown"></i>
                                        </div>
                                        <div class="cabin-info">
                                            <span class="cabin-name">퍼스트</span>
                                            <span class="cabin-desc">최고급 서비스</span>
                                        </div>
                                        <div class="cabin-check">
                                            <i class="fa-solid fa-check"></i>
                                        </div>
                                    </div>
                                </label>
                                <label class="cabin-card" data-cabin="BIZ">
                                    <input type="radio" name="cabin" value="BIZ">
                                    <div class="cabin-card-inner">
                                        <div class="cabin-icon">
                                            <i class="fa-solid fa-briefcase"></i>
                                        </div>
                                        <div class="cabin-info">
                                            <span class="cabin-name">비즈니스</span>
                                            <span class="cabin-desc">편안한 출장</span>
                                        </div>
                                        <div class="cabin-check">
                                            <i class="fa-solid fa-check"></i>
                                        </div>
                                    </div>
                                </label>
                                <label class="cabin-card" data-cabin="ECO">
                                    <input type="radio" name="cabin" value="ECO" checked>
                                    <div class="cabin-card-inner">
                                        <div class="cabin-icon">
                                            <i class="fa-solid fa-chair"></i>
                                        </div>
                                        <div class="cabin-info">
                                            <span class="cabin-name">이코노미</span>
                                            <span class="cabin-desc">합리적인 선택</span>
                                        </div>
                                        <div class="cabin-check">
                                            <i class="fa-solid fa-check"></i>
                                        </div>
                                    </div>
                                </label>
                            </div>
                        </div>
                        <div class="panel-actions">
                            <button type="button" class="btn" data-action="applyPaxCabin">적용</button>
                        </div>
                    </div>
                </div>

                <!-- 검색 버튼 -->
                <button class="search-btn-logo" id="btnSearch" type="button">
                    <img src="${pageContext.request.contextPath}/resources/seat/img/logo-icon.svg" alt="Search" class="search-logo-icon">
                </button>
            </div>
        </div>

        <!-- Filter & Sort Row -->
        <div class="filter-sort-row">
            <!-- Filters -->
            <div class="search-filters">
                <div class="filter" data-filter-wrap="airline">
                    <button class="filter-button" data-filter="airline" type="button">
                        <div class="filter-icon-circle">
                            <i data-lucide="plane" style="width: 12px; height: 12px;"></i>
                        </div>
                        <span>항공사</span>
                        <i data-lucide="chevron-down" style="width: 14px; height: 14px; color: #9ca3af;"></i>
                    </button>

                    <div class="filter-panel airline-options" data-filter-panel="airline" hidden>
                        <ul id="airlineFilterList" class="airline-list">
                            <li class="text-xs text-gray-400">항공사 목록 불러오는 중...</li>
                        </ul>
                    </div>
                </div>

                <!-- 가격 -->
                <div class="filter" data-filter-wrap="price">
                    <button class="filter-button" data-filter="price" type="button">
                        <div class="filter-icon-circle">
                            <i data-lucide="banknote" style="width: 12px; height: 12px;"></i>
                        </div>
                        <span>가격</span>
                        <i data-lucide="chevron-down" style="width: 14px; height: 14px; color: #9ca3af;"></i>
                    </button>

                    <div class="filter-panel price-options" data-filter-panel="price" hidden>
                        <div class="price-header text-left px-4 pt-4">
                            <h3 class="text-lg font-bold">가격대</h3>
                            <p class="text-gray-500 text-sm mb-2">성인 1인 기준 요금</p>
                            <p class="selected-price text-blue-600 font-bold text-lg">
                                <span class="currency">￦</span>
                                <span id="price-min-display">0</span> - <span id="price-max-display">0</span>
                            </p>
                        </div>

                        <div class="range-slider-container px-4 py-4 relative h-16">
                            <div class="slider-track absolute top-1/2 transform -translate-y-1/2 w-full h-1 bg-gray-200 rounded"></div>
                            <div class="slider-range absolute top-1/2 transform -translate-y-1/2 h-1 bg-blue-600 rounded" id="slider-range-bar"></div>

                            <input type="range" id="price-min-input" class="range-input min-range absolute top-1/2 transform -translate-y-1/2 w-full pointer-events-none appearance-none bg-transparent z-20" min="0" max="1000000" value="0" step="1000">
                            <input type="range" id="price-max-input" class="range-input max-range absolute top-1/2 transform -translate-y-1/2 w-full pointer-events-none appearance-none bg-transparent z-10" min="0" max="1000000" value="1000000" step="1000">

                            <div class="range-labels flex justify-between text-gray-500 text-sm mt-8">
                                <span id="total-min-price" hidden>0</span>
                                <span id="total-max-price" hidden>0</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="filter" data-filter-wrap="out-time">
                    <button class="filter-button" data-filter="out-time">
                        <div class="filter-icon-circle">
                            <i data-lucide="clock" style="width: 12px; height: 12px;"></i>
                        </div>
                        <span>가는 날 시간대</span>
                        <i data-lucide="chevron-down" style="width: 14px; height: 14px; color: #9ca3af;"></i>
                    </button>
                    <div class="filter-panel time-filter-panel" data-filter-panel="out-time" hidden>
                        <div class="time-filter-header">
                            <span class="time-chip active" data-range="ALL">가는 날 전체</span>
                        </div>

                        <div class="time-filter-group">
                            <div class="time-label">새벽</div>
                            <div class="time-chips">
                                <button type="button" class="time-chip" data-range="0006">00:00~06:00</button>
                            </div>
                        </div>

                        <div class="time-filter-group">
                            <div class="time-label">오전</div>
                            <div class="time-chips">
                                <button type="button" class="time-chip" data-range="0609">06:00~09:00</button>
                                <button type="button" class="time-chip" data-range="0912">09:00~12:00</button>
                            </div>
                        </div>

                        <div class="time-filter-group">
                            <div class="time-label">오후</div>
                            <div class="time-chips">
                                <button type="button" class="time-chip" data-range="1215">12:00~15:00</button>
                                <button type="button" class="time-chip" data-range="1518">15:00~18:00</button>
                            </div>
                        </div>

                        <div class="time-filter-group">
                            <div class="time-label">밤</div>
                            <div class="time-chips">
                                <button type="button" class="time-chip" data-range="1821">18:00~21:00</button>
                                <button type="button" class="time-chip" data-range="2124">21:00~24:00</button>
                            </div>
                        </div>
                        <div class="panel-actions">
                            <button type="button" class="btn" data-action="apply-time" data-scope="out">적용</button>
                        </div>
                    </div>
                </div>

                <!-- 오는 날 시간대 (왕복만) -->
                <div class="filter" data-filter-wrap="in-time" data-rt-only>
                    <button class="filter-button" data-filter="in-time">
                        <div class="filter-icon-circle">
                            <i data-lucide="clock" style="width: 12px; height: 12px;"></i>
                        </div>
                        <span>오는 날 시간대</span>
                        <i data-lucide="chevron-down" style="width: 14px; height: 14px; color: #9ca3af;"></i>
                    </button>

                    <div class="filter-panel time-filter-panel" data-filter-panel="in-time" hidden>
                        <div class="time-filter-header">
                            <span class="time-chip active" data-range="ALL">오는 날 전체</span>
                        </div>

                        <div class="time-filter-group">
                            <div class="time-label">새벽</div>
                            <div class="time-chips">
                                <button type="button" class="time-chip" data-range="0006">00:00~06:00</button>
                            </div>
                        </div>

                        <div class="time-filter-group">
                            <div class="time-label">오전</div>
                            <div class="time-chips">
                                <button type="button" class="time-chip" data-range="0609">06:00~09:00</button>
                                <button type="button" class="time-chip" data-range="0912">09:00~12:00</button>
                            </div>
                        </div>

                        <div class="time-filter-group">
                            <div class="time-label">오후</div>
                            <div class="time-chips">
                                <button type="button" class="time-chip" data-range="1215">12:00~15:00</button>
                                <button type="button" class="time-chip" data-range="1518">15:00~18:00</button>
                            </div>
                        </div>

                        <div class="time-filter-group">
                            <div class="time-label">밤</div>
                            <div class="time-chips">
                                <button type="button" class="time-chip" data-range="1821">18:00~21:00</button>
                                <button type="button" class="time-chip" data-range="2124">21:00~24:00</button>
                            </div>
                        </div>
                        <div class="panel-actions">
                            <button type="button" class="btn" data-action="apply-time" data-scope="in">적용</button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Sort Button -->
            <div class="sort-container">
                <span class="result-count">
                    <strong id="resultCount">0개</strong>의 항공편
                </span>
                <div class="divider"></div>
                <button class="sort-button">
                    <i data-lucide="arrow-down-wide-narrow" style="width: 16px; height: 16px;"></i>
                    최저가순
                </button>
            </div>
        </div>
    </div>

    <!-- Flight Results -->
    <section class="flights-section">
        <div class="flights-header">
            <h2 class="flights-title">항공편</h2>
        </div>

        <div id="resultList" class="flights-list"></div>

        <div class="pagination-container" id="paginationBox">
            <button type="button" class="pg-btn pg-control" data-action="first" title="처음 페이지">
                &lt;&lt;
            </button>
            <button type="button" class="pg-btn pg-control" data-action="prev" title="이전 페이지">
                &lt;
            </button>

            <div class="pg-pages" id="paginationNumbers">
            </div>

            <button type="button" class="pg-btn pg-control" data-action="next" title="다음 페이지">
                &gt;
            </button>
            <button type="button" class="pg-btn pg-control" data-action="last" title="마지막 페이지">
                &gt;&gt;
            </button>
        </div>
    </section>
</main>
<script>
    const CONTEXT_PATH = "${pageContext.request.contextPath}";
</script>

<jsp:include page="include/flight-detail.jsp" />

<script src="https://unpkg.com/lucide@latest"></script>
<script src="${pageContext.request.contextPath}/resources/search/js/search.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="${pageContext.request.contextPath}/resources/search/js/paging.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="${pageContext.request.contextPath}/resources/search/js/filtering.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="${pageContext.request.contextPath}/resources/search/js/sort.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script src="${pageContext.request.contextPath}/resources/search/js/flight.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="${pageContext.request.contextPath}/resources/search/js/details.js?v=<%= System.currentTimeMillis() %>"></script>
<!-- Flatpickr -->
<script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
<script src="https://cdn.jsdelivr.net/npm/flatpickr/dist/l10n/ko.js"></script>
<script>
    (function() {
        let rangePicker = null;

        function initFlatpickr() {
            if (typeof flatpickr === 'undefined') {
                console.error('Flatpickr not loaded');
                return;
            }

            const dateFieldWrap = document.getElementById("dateFieldWrap");
            const dateRangeText = document.getElementById("dateRangeText");
            const startInput = document.getElementById("dateStart");
            const endInput = document.getElementById("dateEnd");

            if (!dateFieldWrap) {
                console.error('dateFieldWrap not found');
                return;
            }

            // 로컬 타임존 기준 오늘 날짜 (UTC 변환 시 날짜 어긋남 방지)
            const today = new Date();
            const minDate = today.getFullYear() + '-' +
                String(today.getMonth() + 1).padStart(2, '0') + '-' +
                String(today.getDate()).padStart(2, '0');

            // 날짜 포맷 함수
            function formatDate(date) {
                const month = date.getMonth() + 1;
                const day = date.getDate();
                const weekdays = ['일', '월', '화', '수', '목', '금', '토'];
                const weekday = weekdays[date.getDay()];
                return month + '.' + day + ' (' + weekday + ')';
            }

            // 피커 생성 함수
            function createPicker() {
                if (rangePicker) {
                    rangePicker.destroy();
                }

                // window.state가 없으면 기본값 설정 (search.js 로드 전일 수 있음)
                const isOneWay = (window.state && window.state.tripType === "OW");

                const toYYYYMMDD = (date) => {
                    if (!date) return '';
                    return date.getFullYear() + '-' +
                        String(date.getMonth() + 1).padStart(2, '0') + '-' +
                        String(date.getDate()).padStart(2, '0');
                };

                rangePicker = flatpickr(dateFieldWrap, {
                    locale: "ko",
                    mode: isOneWay ? "single" : "range",
                    dateFormat: "Y-m-d",
                    minDate: minDate,
                    showMonths: window.innerWidth > 768 ? 2 : 1,
                    disableMobile: true,
                    allowInput: false,
                    clickOpens: true,
                    onOpen: function(selectedDates, dateStr, instance) {
                        if (instance.calendarContainer) {
                            instance.calendarContainer.style.zIndex = '99999';
                        }
                    },
                    onChange: function(selectedDates, dateStr, instance) {
                        const isOW = window.state && window.state.tripType === "OW";

                        if (isOW && selectedDates.length === 1) {
                            // 편도 - 단일 날짜 선택
                            const startText = formatDate(selectedDates[0]);
                            const start = toYYYYMMDD(selectedDates[0]);
                            dateRangeText.textContent = startText;
                            dateRangeText.classList.remove('selecting');

                            if (window.state) {
                                window.state.dateStart = start;
                                window.state.dateEnd = null;
                            }
                            if (startInput) startInput.value = start;
                            if (endInput) endInput.value = '';

                        } else if (!isOW && selectedDates.length === 1) {
                            // 왕복 - 첫 번째 날짜 선택
                            const startText = formatDate(selectedDates[0]);
                            const start = toYYYYMMDD(selectedDates[0]);
                            dateRangeText.textContent = startText + '  →  오는날 선택';
                            dateRangeText.classList.add('selecting');

                            if (window.state) {
                                window.state.dateStart = start;
                                window.state.dateEnd = null;
                            }
                            if (startInput) startInput.value = start;
                            if (endInput) endInput.value = '';

                        } else if (selectedDates.length === 2) {
                            // 왕복 - 두 날짜 모두 선택
                            const startText = formatDate(selectedDates[0]);
                            const endText = formatDate(selectedDates[1]);
                            const start = toYYYYMMDD(selectedDates[0]);
                            const end = toYYYYMMDD(selectedDates[1]);
                            dateRangeText.textContent = startText + '  →  ' + endText;
                            dateRangeText.classList.remove('selecting');

                            if (window.state) {
                                window.state.dateStart = start;
                                window.state.dateEnd = end;
                            }
                            if (startInput) startInput.value = start;
                            if (endInput) endInput.value = end;
                        }
                    }
                });
            }

            // 초기 피커 생성
            createPicker();

            // 편도/왕복 전환 시 피커 재생성
            document.addEventListener('tripTypeChanged', function() {
                const savedStart = window.state ? window.state.dateStart : null;
                createPicker();

                // 이전에 선택한 출발일이 있으면 복원
                if (savedStart && rangePicker) {
                    rangePicker.setDate(savedStart);
                    dateRangeText.textContent = formatDate(new Date(savedStart));
                    if (window.state && window.state.tripType === "RT") {
                        dateRangeText.textContent += '  →  오는날 선택';
                        dateRangeText.classList.add('selecting');
                    }
                } else {
                    dateRangeText.textContent = '날짜를 선택하세요';
                    dateRangeText.classList.remove('selecting');
                }
            });

            // 반응형 - 창 크기 변경 시
            window.addEventListener('resize', function() {
                const months = window.innerWidth > 768 ? 2 : 1;
                if (rangePicker && rangePicker.config.showMonths !== months) {
                    rangePicker.set('showMonths', months);
                }
            });

            console.log('Flatpickr range picker initialized');
        }

        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', initFlatpickr);
        } else {
            setTimeout(initFlatpickr, 100);
        }
    })();

    // Lucide 아이콘 초기화
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
</script>
<script>
    // 1. 기본값은 비로그인(false)으로 설정
    let isUserLoggedIn = false;

    // 2. Spring Security 태그가 로그인 상태라면 true로 변경해줌
    // (서버에서 이 부분이 실행될 때만 자바스크립트 코드가 생성됨)
</script>

<sec:authorize access="isAuthenticated()">
    <script>
        isUserLoggedIn = true;
    </script>
</sec:authorize>

<%-- 예약 폼 (숨김) --%>
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
