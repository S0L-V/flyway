<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>검색 결과 - Flyway</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/common/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/search.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/details.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/flights.css?v=<%= System.currentTimeMillis() %>">
</head>

<body>
<!-- Header -->
<%@ include file="/WEB-INF/views/common/header.jsp" %>

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

        <div class="search-filters">

            <div class="filter" data-filter-wrap="airline">
                <button class="filter-button" data-filter="airline" type="button">
                    <span>항공사</span>
                    <img src="${pageContext.request.contextPath}/resources/search/img/dropdown-arrow.svg" alt="" class="filter-icon" />
                </button>

                <div class="filter-panel airline-options" data-filter-panel="airline" hidden>
<%--                    <label class="chip"><input type="checkbox" name="airline" value="OZ" checked> 아시아나</label>--%>
<%--                    <label class="chip"><input type="checkbox" name="airline" value="KE" checked> 대한항공</label>--%>
                    <ul id="airlineFilterList" class="airline-list">
                        <li class="text-xs text-gray-400">항공사 목록 불러오는 중...</li>
                    </ul>
                </div>
            </div>

            <!-- 가격 -->
            <div class="filter" data-filter-wrap="price">
                <button class="filter-button" data-filter="price" type="button">
                    <span>가격</span>
                    <img src="${pageContext.request.contextPath}/resources/search/img/dropdown-arrow.svg" alt="" class="filter-icon" />
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
                    <span>가는 날 시간대</span>
                    <img src="${pageContext.request.contextPath}/resources/search/img/dropdown-arrow.svg" alt="" class="filter-icon" />
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
                    <span>오는 날 시간대</span>
                    <img src="${pageContext.request.contextPath}/resources/search/img/dropdown-arrow.svg" alt="" class="filter-icon" />
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

<script src="${pageContext.request.contextPath}/resources/search/js/search.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="${pageContext.request.contextPath}/resources/search/js/paging.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="${pageContext.request.contextPath}/resources/search/js/filtering.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="${pageContext.request.contextPath}/resources/search/js/sort.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="${pageContext.request.contextPath}/resources/search/js/flight.js?v=<%= System.currentTimeMillis() %>"></script>
<script src="${pageContext.request.contextPath}/resources/search/js/details.js?v=<%= System.currentTimeMillis() %>"></script>
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
</form>
</body>
</html>
