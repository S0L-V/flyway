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
            position: relative; /* For absolute positioning of scroll-to-top */
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

        /* 정렬 컨테이너 */
        .sort-container {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-left: auto;
        }

        /* 검색결과 배지 */
        .result-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 6px 12px;
            background: #f0f7ff;
            border-radius: 8px;
            font-size: 13px;
            font-weight: 500;
            color: #64748b;
            font-family: 'Pretendard', sans-serif;
        }
        .result-badge i {
            width: 14px;
            height: 14px;
            color: #3093F7;
        }
        .result-badge strong {
            color: #3093F7;
            font-weight: 700;
            margin-left: 2px;
        }

        /* 정렬 버튼 */
        .sort-button {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            padding: 6px 12px;
            border-radius: 8px;
            font-size: 13px;
            font-weight: 600;
            color: #64748b;
            background: white;
            border: 1px solid #e2e8f0;
            cursor: pointer;
            transition: all 0.2s ease;
            font-family: 'Pretendard', sans-serif;
        }
        .sort-button:hover {
            border-color: #3093F7;
            color: #3093F7;
        }
        .sort-button.asc,
        .sort-button.desc {
            background: #3093F7;
            border-color: #3093F7;
            color: white;
        }
        .sort-button i {
            width: 14px;
            height: 14px;
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

        /* Pagination Jelly Style */
        .pagination-container {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 8px;
            margin-top: 40px;
            margin-bottom: 60px;
        }

        .pg-btn {
            min-width: 40px;
            height: 40px;
            border: none;
            background: #ffffff;
            color: #64748b;
            border-radius: 12px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
            font-family: 'Pretendard', sans-serif;
        }

        .pg-btn:hover:not(:disabled) {
            background: #f1f5f9;
            color: #1f6feb;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(31, 111, 235, 0.15);
        }

        .pg-btn:active:not(:disabled) {
            transform: scale(0.95);
        }

        .pg-btn.active {
            background: linear-gradient(135deg, #1f6feb, #165bca);
            color: #ffffff;
            box-shadow: 0 4px 12px rgba(31, 111, 235, 0.3);
            font-weight: 700;
        }

        .pg-btn:disabled {
            background: #f8fafc;
            color: #cbd5e1;
            cursor: not-allowed;
            box-shadow: none;
        }

        .pg-control {
            font-size: 12px;
            font-weight: 700;
            color: #94a3b8;
        }

        .pg-pages {
            display: flex;
            gap: 8px;
        }

        /* New Filter Styles */
        .filter-panel {
            padding: 0 !important;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            min-width: 320px;
        }

        /* Fix: Ensure hidden attribute works with display: flex */
        .filter-panel[hidden] {
            display: none !important;
        }

        .filter-header {
            padding: 20px 20px 12px;
            background: #fff;
        }

        .filter-title {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 16px;
            font-weight: 700;
            color: #1f2937;
            margin-bottom: 4px;
        }

        .filter-title-icon {
            width: 18px;
            height: 18px;
            color: #3093F7;
        }

        .filter-subtitle {
            font-size: 12px;
            color: #9ca3af;
            margin: 0;
        }

        /* Airline Filter */
        .airline-search-box {
            position: relative;
            margin: 0 20px 12px;
        }

        .airline-search-box input {
            width: 100%;
            padding: 10px 12px 10px 36px;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            font-size: 13px;
            outline: none;
            transition: border-color 0.2s;
            box-sizing: border-box;
        }

        .airline-search-box input:focus {
            border-color: #3093F7;
        }

        .airline-search-box .search-icon {
            position: absolute;
            left: 10px;
            top: 50%;
            transform: translateY(-50%);
            width: 16px;
            height: 16px;
            color: #9ca3af;
        }

        .airline-list-container {
            max-height: 240px;
            overflow-y: auto;
            padding: 0 20px;
            margin-bottom: 12px;
        }

        .airline-list li {
            padding: 0 !important;
            margin-bottom: 4px;
        }

        /* Time Filter Grid */
        .time-filter-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 8px;
            padding: 0 20px 20px;
        }

        .time-option {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 16px;
            border: 1px solid #e5e7eb;
            border-radius: 12px;
            cursor: pointer;
            transition: all 0.2s;
            background: #fff;
            text-align: center;
        }

        .time-option:hover {
            background: #f9fafb;
            border-color: #d1d5db;
        }

        .time-option.active {
            background: #eff6ff;
            border-color: #3093F7;
            color: #1d4ed8;
            box-shadow: 0 4px 12px rgba(37, 99, 235, 0.1);
        }

        .time-option-icon {
            margin-bottom: 8px;
            color: #6b7280;
            width: 20px;
            height: 20px;
        }

        .time-option.active .time-option-icon {
            color: #3093F7;
        }

        .time-option-label {
            font-size: 14px;
            font-weight: 700;
            margin-bottom: 2px;
        }

        .time-option-range {
            font-size: 11px;
            color: #9ca3af;
        }

        .time-option.active .time-option-range {
            color: #60a5fa;
        }

        /* All Button Style */
        .time-option-all {
            grid-column: 1 / -1;
            flex-direction: row;
            gap: 8px;
            padding: 12px;
            background-color: #f8fafc;
        }
        .time-option-all .time-option-icon {
            margin-bottom: 0;
            color: #64748b;
        }
        .time-option-all.active {
            background-color: #eff6ff;
            border-color: #3093F7;
        }
        .time-option-all.active .time-option-icon {
            color: #3093F7;
        }

        /* Panel Actions */
        .panel-actions {
            padding: 16px 20px;
            border-top: 1px solid #f3f4f6;
            display: flex;
            justify-content: flex-end; /* Changed to flex-end since we removed reset button */
            align-items: center;
            background: #fff;
            margin-top: auto;
        }

        .btn-text {
            background: none;
            border: none;
            color: #6b7280;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
            padding: 0;
        }

        .btn-text:hover {
            color: #1f2937;
            text-decoration: underline;
        }

        .btn-primary {
            background: #3093F7;
            color: white;
            border: none;
            padding: 8px 20px;
            border-radius: 6px;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.2s;
        }

        .btn-primary:hover {
            background: #2563eb;
        }

        /* Specific override for airline panel actions to keep space-between */
        .airline-options .panel-actions {
            justify-content: space-between;
        }

        .search-filters .filter .filter-panel[hidden] {
            display: none !important;
        }

        /* Trip Selector Row */
        .trip-selector-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
        }

        .policy-btn {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 6px 12px;
            border-radius: 9999px;
            background-color: #fff;
            border: 1px solid #e5e7eb;
            color: #6b7280;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s;
        }

        .policy-btn:hover {
            border-color: #3093F7;
            color: #3093F7;
            background-color: #f0f7ff;
        }

        .policy-btn i {
            font-size: 14px;
        }

        /* Scroll To Top Button */
        .scroll-to-top {
            position: fixed;
            bottom: 50%; /* 화면 중간 높이 */
            right: 40px;
            width: 48px;
            height: 48px;
            border-radius: 50%;
            background-color: rgba(47, 147, 247, 0.2); /* 파란 불투명 배경 */
            backdrop-filter: blur(4px);
            border: 1px solid rgba(47, 147, 247, 0.3);
            color: #1d4ed8; /* 진파랑 화살표 */
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            z-index: 90;
            opacity: 0;
            visibility: hidden;
            transform: translateY(20px);
        }

        .scroll-to-top.visible {
            opacity: 1;
            visibility: visible;
            transform: translateY(0);
        }

        .scroll-to-top:hover {
            background-color: rgba(47, 147, 247, 0.4); /* 호버 시 조금 더 진하게 */
            color: #1e40af;
            border-color: #3093F7;
            transform: translateY(-4px);
            box-shadow: 0 8px 16px rgba(48, 147, 247, 0.25);
        }

        @media (max-width: 1400px) {
            .scroll-to-top {
                right: 20px;
            }
        }
    </style>
</head>

<body>
<!-- Header -->
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<!-- Main Content -->
<main class="main-content">

    <div class="search-container">
        <!-- Trip Type Selector Row -->
        <div class="trip-selector-row">
            <div class="trip-selector" id="tripSelector">
                <div class="trip-indicator" id="tripIndicator"></div>
                <button class="trip-btn active" data-trip="RT">왕복</button>
                <button class="trip-btn" data-trip="OW">편도</button>
            </div>

            <button onclick="openPolicyModal()" class="policy-btn">
                <i class="fa-solid fa-scale-balanced"></i>
                <span>가격정책</span>
            </button>
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
                <!-- Airline Filter -->
                <div class="filter" data-filter-wrap="airline">
                    <button class="filter-button" data-filter="airline" type="button">
                        <div class="filter-icon-circle">
                            <i data-lucide="plane" style="width: 12px; height: 12px;"></i>
                        </div>
                        <span>항공사</span>
                        <i data-lucide="chevron-down" style="width: 14px; height: 14px; color: #9ca3af;"></i>
                    </button>

                    <div class="filter-panel airline-options" data-filter-panel="airline" hidden>
                        <div class="filter-header">
                            <div class="filter-title">
                                <i data-lucide="plane" class="filter-title-icon"></i>
                                <span>항공사 선택</span>
                            </div>
                            <p class="filter-subtitle">원하는 항공사를 선택하세요</p>
                        </div>

                        <div class="airline-search-box">
                            <i data-lucide="search" class="search-icon"></i>
                            <input type="text" id="airlineSearchInput" placeholder="항공사 검색">
                        </div>

                        <div class="airline-list-container">
                            <ul id="airlineFilterList" class="airline-list">
                                <li class="text-xs text-gray-400">항공사 목록 불러오는 중...</li>
                            </ul>
                        </div>

                        <div class="panel-actions">
                            <button type="button" class="btn-text" id="btnResetAirline">전체 선택</button>
                            <button type="button" class="btn-primary" data-action="apply-airline">적용</button>
                        </div>
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

                    <div class="filter-panel price-panel" data-filter-panel="price" hidden>
                        <div class="price-filter-content">
                            <div class="price-filter-header">
                                <div class="price-filter-title">
                                    <i data-lucide="wallet" class="price-title-icon"></i>
                                    <span>가격 범위 설정</span>
                                </div>
                                <p class="price-filter-subtitle">성인 1인 기준 편도 요금</p>
                            </div>

                            <div class="price-display-box">
                                <div class="price-display-item">
                                    <span class="price-label">최저</span>
                                    <span class="price-value" id="price-min-display">0</span>
                                    <span class="price-unit">원</span>
                                </div>
                                <div class="price-display-divider">
                                    <i data-lucide="arrow-right"></i>
                                </div>
                                <div class="price-display-item">
                                    <span class="price-label">최고</span>
                                    <span class="price-value" id="price-max-display">0</span>
                                    <span class="price-unit">원</span>
                                </div>
                            </div>

                            <div class="range-slider-container">
                                <div class="slider-track"></div>
                                <div class="slider-range" id="slider-range-bar"></div>
                                <input type="range" id="price-min-input" class="range-input min-range" min="0" max="1000000" value="0" step="5000">
                                <input type="range" id="price-max-input" class="range-input max-range" min="0" max="1000000" value="1000000" step="5000">
                            </div>

                            <div class="price-range-labels">
                                <span class="range-label-min" id="total-min-price">0원</span>
                                <span class="range-label-max" id="total-max-price">1,000,000원</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Out Time Filter -->
                <div class="filter" data-filter-wrap="out-time">
                    <button class="filter-button" data-filter="out-time">
                        <div class="filter-icon-circle">
                            <i data-lucide="clock" style="width: 12px; height: 12px;"></i>
                        </div>
                        <span>가는 날 시간대</span>
                        <i data-lucide="chevron-down" style="width: 14px; height: 14px; color: #9ca3af;"></i>
                    </button>
                    <div class="filter-panel time-filter-panel" data-filter-panel="out-time" hidden>
                        <div class="filter-header">
                            <div class="filter-title">
                                <i data-lucide="clock" class="filter-title-icon"></i>
                                <span>가는 날 출발 시간</span>
                            </div>
                            <p class="filter-subtitle">원하는 시간대를 선택하세요</p>
                        </div>

                        <div class="time-filter-grid">
                            <button type="button" class="time-chip time-option time-option-all" data-range="ALL">
                                <i data-lucide="check-circle" class="time-option-icon"></i>
                                <span class="time-option-label">전체 시간대</span>
                            </button>
                            <button type="button" class="time-chip time-option" data-range="0006">
                                <i data-lucide="moon" class="time-option-icon"></i>
                                <span class="time-option-label">새벽</span>
                                <span class="time-option-range">00:00~06:00</span>
                            </button>
                            <button type="button" class="time-chip time-option" data-range="0612">
                                <i data-lucide="sun" class="time-option-icon"></i>
                                <span class="time-option-label">오전</span>
                                <span class="time-option-range">06:00~12:00</span>
                            </button>
                            <button type="button" class="time-chip time-option" data-range="1218">
                                <i data-lucide="sunset" class="time-option-icon"></i>
                                <span class="time-option-label">오후</span>
                                <span class="time-option-range">12:00~18:00</span>
                            </button>
                            <button type="button" class="time-chip time-option" data-range="1824">
                                <i data-lucide="moon-star" class="time-option-icon"></i>
                                <span class="time-option-label">밤</span>
                                <span class="time-option-range">18:00~24:00</span>
                            </button>
                        </div>

                        <div class="panel-actions">
                            <button type="button" class="btn-primary" data-action="apply-time" data-scope="out">적용</button>
                        </div>
                    </div>
                </div>

                <!-- In Time Filter -->
                <div class="filter" data-filter-wrap="in-time" data-rt-only>
                    <button class="filter-button" data-filter="in-time">
                        <div class="filter-icon-circle">
                            <i data-lucide="clock" style="width: 12px; height: 12px;"></i>
                        </div>
                        <span>오는 날 시간대</span>
                        <i data-lucide="chevron-down" style="width: 14px; height: 14px; color: #9ca3af;"></i>
                    </button>

                    <div class="filter-panel time-filter-panel" data-filter-panel="in-time" hidden>
                        <div class="filter-header">
                            <div class="filter-title">
                                <i data-lucide="clock" class="filter-title-icon"></i>
                                <span>오는 날 출발 시간</span>
                            </div>
                            <p class="filter-subtitle">원하는 시간대를 선택하세요</p>
                        </div>

                        <div class="time-filter-grid">
                            <button type="button" class="time-chip time-option time-option-all" data-range="ALL">
                                <i data-lucide="check-circle" class="time-option-icon"></i>
                                <span class="time-option-label">전체 시간대</span>
                            </button>
                            <button type="button" class="time-chip time-option" data-range="0006">
                                <i data-lucide="moon" class="time-option-icon"></i>
                                <span class="time-option-label">새벽</span>
                                <span class="time-option-range">00:00~06:00</span>
                            </button>
                            <button type="button" class="time-chip time-option" data-range="0612">
                                <i data-lucide="sun" class="time-option-icon"></i>
                                <span class="time-option-label">오전</span>
                                <span class="time-option-range">06:00~12:00</span>
                            </button>
                            <button type="button" class="time-chip time-option" data-range="1218">
                                <i data-lucide="sunset" class="time-option-icon"></i>
                                <span class="time-option-label">오후</span>
                                <span class="time-option-range">12:00~18:00</span>
                            </button>
                            <button type="button" class="time-chip time-option" data-range="1824">
                                <i data-lucide="moon-star" class="time-option-icon"></i>
                                <span class="time-option-label">밤</span>
                                <span class="time-option-range">18:00~24:00</span>
                            </button>
                        </div>

                        <div class="panel-actions">
                            <button type="button" class="btn-primary" data-action="apply-time" data-scope="in">적용</button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Sort Button -->
            <div class="sort-container">
                <div class="result-badge">
                    <i data-lucide="plane"></i>
                    <span>검색결과</span>
                    <strong id="resultCount">0</strong>
                </div>
                <button class="sort-button" id="sortPriceBtn">
                    <span id="sortLabel">가격순</span>
                    <i data-lucide="chevrons-up-down" id="sortIcon"></i>
                </button>
            </div>
        </div>
    </div>

    <!-- Flight Results -->
    <section class="flights-section">
        <div class="flights-header">
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

<!-- Scroll To Top Button -->
<button id="scrollToTopBtn" class="scroll-to-top" title="맨 위로">
    <i class="fa-solid fa-arrow-up"></i>
</button>

<!-- Policy Modal -->
<div id="policyModal" class="policy-modal-overlay">
    <div class="policy-modal-container">
        <div class="policy-modal-header">
            <h3 class="policy-modal-title">가격 정책 안내</h3>
            <button onclick="closePolicyModal()" class="policy-modal-close">
                <i class="fa-solid fa-xmark"></i>
            </button>
        </div>
        <div class="policy-modal-body">
            <jsp:include page="/WEB-INF/views/policy/policy.jsp" />
        </div>
    </div>
</div>

<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/policy/css/policy.css">

<script>
    const CONTEXT_PATH = "${pageContext.request.contextPath}";

    function openPolicyModal() {
        document.getElementById('policyModal').classList.add('active');
        document.body.style.overflow = 'hidden';
    }

    function closePolicyModal() {
        document.getElementById('policyModal').classList.remove('active');
        document.body.style.overflow = '';
    }

    // Close modal when clicking outside
    document.getElementById('policyModal').addEventListener('click', function(e) {
        if (e.target === this) {
            closePolicyModal();
        }
    });

    // Close modal on escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && document.getElementById('policyModal').classList.contains('active')) {
            closePolicyModal();
        }
    });

    // Scroll To Top Logic
    const scrollToTopBtn = document.getElementById('scrollToTopBtn');

    window.addEventListener('scroll', () => {
        if (window.scrollY > 300) {
            scrollToTopBtn.classList.add('visible');
        } else {
            scrollToTopBtn.classList.remove('visible');
        }
    });

    scrollToTopBtn.addEventListener('click', () => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
</script>

<jsp:include page="include/flight-detail.jsp" />

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

<script>
    document.addEventListener('DOMContentLoaded', function() {
        // Airline Search
        const airlineSearchInput = document.getElementById('airlineSearchInput');
        const airlineList = document.getElementById('airlineFilterList');

        if (airlineSearchInput && airlineList) {
            airlineSearchInput.addEventListener('input', function(e) {
                const term = e.target.value.toLowerCase();
                const items = airlineList.querySelectorAll('li');
                items.forEach(item => {
                    const text = item.textContent.toLowerCase();
                    if (text.includes(term)) {
                        item.style.display = '';
                    } else {
                        item.style.display = 'none';
                    }
                });
            });
        }

        // Airline Reset (Select All)
        const btnResetAirline = document.getElementById('btnResetAirline');
        if (btnResetAirline) {
            btnResetAirline.addEventListener('click', function() {
                const checkboxes = document.querySelectorAll('input[name="airline"]');
                checkboxes.forEach(cb => cb.checked = true);
                // Trigger change to update filter
                if(checkboxes.length > 0) {
                    checkboxes[0].dispatchEvent(new Event('change', { bubbles: true }));
                }
            });
        }

        // Airline Apply
        const btnApplyAirline = document.querySelector('[data-action="apply-airline"]');
        if (btnApplyAirline) {
            btnApplyAirline.addEventListener('click', function() {
                // Close panel
                const panel = document.querySelector('[data-filter-panel="airline"]');
                if (panel) {
                    panel.hidden = true;
                }
                const btn = document.querySelector('[data-filter="airline"]');
                if (btn) btn.setAttribute("aria-expanded", "false");
            });
        }

        // Remove old Time Reset listeners since we removed the buttons
    });
</script>

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
