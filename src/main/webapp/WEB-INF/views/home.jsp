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

            <div class="slider-wrapper">
                <button class="nav-btn prev" id="offerPrev">&lt;</button>
                <button class="nav-btn next" id="offerNext">&gt;</button>

                <div class="main-slider-track" id="offerTrack">
                    <div class="offer-card" style="background-image: url('https://images.unsplash.com/photo-1565624765373-c1f0383d7350?auto=format&fit=crop&w=400&q=80');">
                        <div class="card-badges"><span class="card-badge blue">직항</span></div>
                        <div class="offer-overlay">
                            <div class="offer-airline">대한항공</div>
                            <div class="offer-city">나트랑</div>
                            <div class="offer-date">02.15(토) - 02.22(토)</div>
                            <div class="offer-price-area"><div class="offer-price">₩837,800</div></div>
                        </div>
                    </div>
                    <div class="offer-card" style="background-image: url('https://images.unsplash.com/photo-1559592413-7cec430aa33f?auto=format&fit=crop&w=400&q=80');">
                        <div class="card-badges"><span class="card-badge blue">특가</span></div>
                        <div class="offer-overlay">
                            <div class="offer-airline">비엣젯</div>
                            <div class="offer-city">다낭</div>
                            <div class="offer-date">02.16(일) - 02.20(목)</div>
                            <div class="offer-price-area"><div class="offer-price">₩725,000</div></div>
                        </div>
                    </div>
                    <div class="offer-card" style="background-image: url('https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?auto=format&fit=crop&w=400&q=80');">
                        <div class="card-badges"><span class="card-badge blue">최저가</span></div>
                        <div class="offer-overlay">
                            <div class="offer-airline">아시아나</div>
                            <div class="offer-city">도쿄</div>
                            <div class="offer-date">02.18(화) - 02.21(금)</div>
                            <div class="offer-price-area"><div class="offer-price">₩345,000</div></div>
                        </div>
                    </div>
                    <div class="offer-card" style="background-image: url('https://images.unsplash.com/photo-1508009603885-50cf7c579365?auto=format&fit=crop&w=400&q=80');">
                        <div class="card-badges"><span class="card-badge blue">얼리버드</span></div>
                        <div class="offer-overlay">
                            <div class="offer-airline">진에어</div>
                            <div class="offer-city">방콕</div>
                            <div class="offer-date">03.01(토) - 03.05(수)</div>
                            <div class="offer-price-area"><div class="offer-price">₩520,000</div></div>
                        </div>
                    </div>
                    <div class="offer-card" style="background-image: url('https://images.unsplash.com/photo-1590559899731-a38283956c8c?auto=format&fit=crop&w=400&q=80');">
                        <div class="card-badges"><span class="card-badge">땡처리</span></div>
                        <div class="offer-overlay">
                            <div class="offer-airline">제주항공</div>
                            <div class="offer-city">오사카</div>
                            <div class="offer-date">02.10(월) - 02.12(수)</div>
                            <div class="offer-price-area"><div class="offer-price">₩210,000</div></div>
                        </div>
                    </div>
                    <div class="offer-card" style="background-image: url('https://images.unsplash.com/photo-1552465011-b4e21bf6e79a?auto=format&fit=crop&w=400&q=80');">
                        <div class="card-badges"><span class="card-badge">NEW</span></div>
                        <div class="offer-overlay">
                            <div class="offer-airline">티웨이</div>
                            <div class="offer-city">푸꾸옥</div>
                            <div class="offer-date">03.10(월) - 03.14(금)</div>
                            <div class="offer-price-area"><div class="offer-price">₩410,000</div></div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <section class="trending-section">
        </section>

        <section class="trending-section">
            <div class="trending-header">
                <h2 class="section-title">
                    실시간 인기 급상승 <span class="badge-live">LIVE</span>
                </h2>
                <a href="#" class="view-all-link">실시간 데이터 상세 보기 →</a>
            </div>
            <p style="color: #666; margin-top:-15px; margin-bottom: 20px; font-size: 14px;">"지금 이 순간, 가장 많이 검색되는 도시들"</p>

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

<script>
    document.addEventListener('DOMContentLoaded', () => {
        const track = document.getElementById('offerTrack');
        const wrapper = document.getElementById('sliderWrapper');
        const prevBtn = document.getElementById('offerPrev');
        const nextBtn = document.getElementById('offerNext');
        const cards = document.querySelectorAll('.offer-card');

        const cardWidth = 280; // CSS와 동일해야 함
        const gap = 24;        // CSS와 동일해야 함
        const totalCards = cards.length;
        let currentIndex = 1; // 2번째 카드를 기본 중앙(1번 인덱스)으로 설정 (취향에 따라 0으로 변경 가능)

        // 카드를 중앙에 배치하는 함수
        const updatePosition = () => {
            // 컨테이너의 중앙 지점 (너비 / 2)
            const wrapperCenter = wrapper.clientWidth / 2;
            // 카드의 중앙 지점 (너비 / 2)
            const cardCenter = cardWidth / 2;

            // 중앙 정렬을 위한 초기 오프셋 값 (화면 중앙 - 카드 반쪽)
            const centerOffset = wrapperCenter - cardCenter;

            // 현재 인덱스만큼 이동할 거리 (카드 너비 + 간격)
            const moveDistance = currentIndex * (cardWidth + gap);

            // 최종 이동 값 계산
            const finalTranslate = centerOffset - moveDistance;

            track.style.transform = `translateX(${finalTranslate}px)`;

            // (선택사항) 중앙에 있는 카드에 active 클래스 추가 (CSS 확대 효과용)
            cards.forEach((card, index) => {
                if (index === currentIndex) card.classList.add('active');
                else card.classList.remove('active');
            });
        };

        // 초기 실행
        updatePosition();
        // 화면 크기 바뀔 때마다 재계산 (반응형 대응)
        window.addEventListener('resize', updatePosition);

        // 다음 버튼
        nextBtn.addEventListener('click', () => {
            if (currentIndex < totalCards - 1) {
                currentIndex++;
                updatePosition();
            }
        });

        // 이전 버튼
        prevBtn.addEventListener('click', () => {
            if (currentIndex > 0) {
                currentIndex--;
                updatePosition();
            }
        });
    });
</script>
</body>
</html>
