<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>flyway - 항공권 예약</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            corePlugins: {
                preflight: false,
            }
        }
    </script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/layout.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/search.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/details.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/flights.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/main/css/main.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/main/css/promotion.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/main/css/hero.css?v=<%= System.currentTimeMillis() %>">
    <!-- Flatpickr -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/themes/dark.css">
</head>
<body>

<!-- Header -->
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<!-- Hero Section with Video Background -->
<section class="hero-section">
    <!-- 비디오 컨테이너 (overflow: hidden) -->
    <div class="hero-video-container">
        <video autoplay loop muted playsinline class="hero-video">
            <source src="https://assets.mixkit.co/videos/preview/mixkit-flying-over-the-clouds-at-sunset-24422-large.mp4" type="video/mp4" />
        </video>
        <div class="hero-overlay"></div>
    </div>

    <div class="hero-content">
        <!-- Badge -->
        <div class="hero-badge animate-flyway">
            <span>Infinite Horizon, Perfect Flight</span>
        </div>

        <!-- Title -->
        <h1 class="hero-title animate-flyway" style="animation-delay: 0.2s;">
            설렘을 예약하세요 <br class="hidden md:block" /> 목적지는 어디인가요?
        </h1>

        <!-- Trip Type Selector -->
        <div class="trip-selector animate-flyway" style="animation-delay: 0.3s;">
            <div class="trip-indicator" id="tripIndicator"></div>
            <button class="trip-btn active" data-trip="RT">왕복</button>
            <button class="trip-btn" data-trip="OW">편도</button>
        </div>

        <!-- Search Bar -->
        <div class="search-glass-panel animate-flyway" style="animation-delay: 0.4s;">
            <div class="search-grid" id="searchBox">

                <!-- 출발 공항 -->
                <div class="search-field" data-field="from">
                    <label class="field-label">Departure</label>
                    <div class="field-input dropdown-toggle" aria-expanded="false">
                        <div class="field-icon departure">
                            <i class="fa-solid fa-plane-departure"></i>
                        </div>
                        <span class="field-value" data-value>출발지 선택</span>
                    </div>
                    <div class="dropdown-panel" hidden>
                        <input class="dropdown-search" type="text" placeholder="공항 검색 (예: ICN, 인천)" autocomplete="off">
                        <ul class="dropdown-list" data-list></ul>
                    </div>
                </div>

                <!-- 도착 공항 -->
                <div class="search-field" data-field="to">
                    <label class="field-label">Destination</label>
                    <div class="field-input dropdown-toggle" aria-expanded="false">
                        <div class="field-icon">
                            <i class="fa-solid fa-location-dot"></i>
                        </div>
                        <span class="field-value" data-value>도착지 선택</span>
                    </div>
                    <div class="dropdown-panel" hidden>
                        <input class="dropdown-search" type="text" placeholder="공항 검색 (예: NRT, 나리타)" autocomplete="off">
                        <ul class="dropdown-list" data-list></ul>
                    </div>
                </div>

                <!-- 날짜 -->
                <div class="search-field date-field" data-field="dates">
                    <label class="field-label">Dates</label>
                    <div class="field-input date-input-wrap" id="dateFieldWrap">
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
                    <label class="field-label">Travelers</label>
                    <div class="field-input dropdown-toggle" aria-expanded="false">
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
    </div>
</section>

<!-- Special Offers Section -->
<section class="special-offers-section">
    <div class="container">
        <div class="section-header">
            <span class="section-label">Special Offers</span>
            <h2 class="section-title-modern">지금 떠나면 이 가격!</h2>
            <p class="section-subtitle">놓치면 후회할 특가 항공권</p>
        </div>
    </div>
    <div class="slider-wrapper" id="slider-wrapper">
        <button class="nav-btn prev" id="offerPrev">&lt;</button>
        <button class="nav-btn next" id="offerNext">&gt;</button>
        <div class="main-slider-track is-entering" id="offerTrack"></div>
    </div>
</section>

<!-- Trending Section -->
<div class="container">
    <div class="content" style="padding-top: 0;">
        <section class="trending-section">
            <div class="trending-header">
                <h2 class="section-title">
                    여행지 인기 급상승 <span class="badge-live">LIVE</span>
                </h2>
                <a href="#" class="view-all-link">실시간 데이터 상세 보기 →</a>
            </div>
            <p class="trending-desc">"최근 일주일, 가장 많이 검색된 도시들"</p>
            <div class="trending-grid"></div>
        </section>
    </div>
</div>

<!-- Hidden Form for Reservation -->
<form id="reservationForm" action="${pageContext.request.contextPath}/reservations/draft" method="post" style="display:none;">
    <input type="hidden" id="hiddenOutFlightId" name="outFlightId">
    <input type="hidden" id="hiddenInFlightId" name="inFlightId">
    <input type="hidden" id="hiddenPassengerCount" name="passengerCount">
    <input type="hidden" id="hiddenCabinClassCode" name="cabinClassCode">
    <input type="hidden" id="hiddenOutPrice" name="outPrice">
    <input type="hidden" id="hiddenInPrice" name="inPrice">
</form>

<!-- Footer -->
<%@ include file="/WEB-INF/views/common/footer.jsp" %>

<script>
    const CONTEXT_PATH = "${pageContext.request.contextPath}";
</script>

<script src="${pageContext.request.contextPath}/resources/search/js/search.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="${pageContext.request.contextPath}/resources/main/js/main.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="${pageContext.request.contextPath}/resources/main/js/promotion.js?v=<%= System.currentTimeMillis() %>"></script>

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

            const today = new Date();
            const minDate = today.toISOString().split('T')[0];

            // 날짜 포맷 함수
            function formatDate(date) {
                const month = date.getMonth() + 1;
                const day = date.getDate();
                const weekdays = ['일', '월', '화', '수', '목', '금', '토'];
                const weekday = weekdays[date.getDay()];
                return month + '.' + day + ' (' + weekday + ')';
            }

            // 현재 tripType에 따른 모드 결정
            function getCurrentMode() {
                return (window.state && window.state.tripType === "OW") ? "single" : "range";
            }

            // 피커 생성 함수
            function createPicker() {
                if (rangePicker) {
                    rangePicker.destroy();
                }

                const isOneWay = window.state && window.state.tripType === "OW";

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
                            const start = formatDate(selectedDates[0]);
                            dateRangeText.textContent = start;
                            dateRangeText.classList.remove('selecting');

                            if (window.state) {
                                window.state.dateStart = selectedDates[0].toISOString().split('T')[0];
                                window.state.dateEnd = null;
                            }
                            if (startInput) startInput.value = selectedDates[0].toISOString().split('T')[0];
                            if (endInput) endInput.value = '';

                        } else if (!isOW && selectedDates.length === 1) {
                            // 왕복 - 첫 번째 날짜 선택
                            const start = formatDate(selectedDates[0]);
                            dateRangeText.textContent = start + '  →  오는날 선택';
                            dateRangeText.classList.add('selecting');

                            if (window.state) {
                                window.state.dateStart = selectedDates[0].toISOString().split('T')[0];
                                window.state.dateEnd = null;
                            }
                            if (startInput) startInput.value = selectedDates[0].toISOString().split('T')[0];
                            if (endInput) endInput.value = '';

                        } else if (selectedDates.length === 2) {
                            // 왕복 - 두 날짜 모두 선택
                            const start = formatDate(selectedDates[0]);
                            const end = formatDate(selectedDates[1]);
                            dateRangeText.textContent = start + '  →  ' + end;
                            dateRangeText.classList.remove('selecting');

                            if (window.state) {
                                window.state.dateStart = selectedDates[0].toISOString().split('T')[0];
                                window.state.dateEnd = selectedDates[1].toISOString().split('T')[0];
                            }
                            if (startInput) startInput.value = selectedDates[0].toISOString().split('T')[0];
                            if (endInput) endInput.value = selectedDates[1].toISOString().split('T')[0];
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
</script>
</body>
</html>
