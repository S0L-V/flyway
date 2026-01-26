<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>사전 좌석 지정 - Flyway</title>
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/variables.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/search/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/seat/seat-select.css">
</head>
<body>
    <!-- Header -->
    <header class="seat-header">
        <div class="seat-header__content">
            <div class="seat-header__logo">
                <img src="${pageContext.request.contextPath}/resources/seat/img/logo-icon.svg" alt="Flyway Logo" class="seat-header__logo-icon" />
                <span class="seat-header__logo-text">flyway</span>
            </div>
            <h1 class="seat-header__title">사전 좌석 지정</h1>
        </div>
    </header>

    <!-- Main Container -->
    <div class="seat-container">
        <!-- Flight Segment Tabs -->
        <div class="segment-tabs">
            <button class="segment-tab segment-tab--active" data-segment="outbound">
                <div class="segment-tab__route">인천(ICN) → 나리타(NRT)</div>
                <div class="segment-tab__datetime">2026-01-08 14:40</div>
            </button>
            <button class="segment-tab" data-segment="inbound">
                <div class="segment-tab__route">나리타(NRT) → 인천(ICN)</div>
                <div class="segment-tab__datetime">2026-02-12 20:40</div>
            </button>
        </div>

        <!-- Main Content Area -->
        <div class="seat-content">
            <!-- Left + Center: Seat Map Area -->
            <div class="seat-map-area">
                <!-- Airplane Illustration -->
                <div class="airplane-illustration">
                    <img src="${pageContext.request.contextPath}/resources/seat/img/airplane-diagram.svg" alt="Airplane Layout" />
                </div>

                <!-- Seat Map Container (Dynamic) -->
                <div class="seat-map-container">
                    <div id="seat-grid"
                         class="seat-grid"
                         data-ctx="${pageContext.request.contextPath}"
                         data-rid="${reservationId}"
                         data-sid="${reservationSegmentId}">
                        <p class="seat-grid__loading">좌석 정보를 불러오는 중...</p>
                    </div>


                    <!-- Legend -->
                    <div class="seat-legend">
                        <div class="seat-legend__item">
                            <span class="seat-legend__icon seat-legend__icon--available"></span>
                            <span class="seat-legend__label">선택 가능</span>
                        </div>
                        <div class="seat-legend__item">
                            <span class="seat-legend__icon seat-legend__icon--selected"></span>
                            <span class="seat-legend__label">선택됨</span>
                        </div>
                        <div class="seat-legend__item">
                            <span class="seat-legend__icon seat-legend__icon--unavailable"></span>
                            <span class="seat-legend__label">선택 불가</span>
                        </div>
                        <div class="seat-legend__item">
                            <span class="seat-legend__icon seat-legend__icon--hold"></span>
                            <span class="seat-legend__label">예약 대기</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Right: Selection Summary Panel -->
            <aside class="selection-panel">
                <div class="selection-panel__content">
                    <h2 class="selection-panel__title">좌석 선택 내역</h2>
                    
                    <!-- Dynamic Summary Container -->
                    <div id="selected-summary" class="selected-summary">
                        <!-- Passenger selections will be rendered by JavaScript -->
                        <div class="selected-summary__empty">
                            <p>선택된 좌석이 없습니다.</p>
                        </div>
                    </div>

                    <!-- Total Price -->
                    <div class="selection-total">
                        <span class="selection-total__label">총 추가 금액</span>
                        <span class="selection-total__amount" id="total-amount">₩0</span>
                    </div>
                </div>

                <!-- Action Buttons -->
                <div class="selection-actions">
                    <button type="button" class="btn btn--secondary" id="btn-cancel">취소</button>
                    <button type="button" class="btn btn--primary" id="btn-confirm">완료</button>
                </div>
            </aside>
        </div>

        <!-- Notices Section -->
        <section class="seat-notices">
            <h3 class="seat-notices__title">유의사항</h3>
            <div class="seat-notices__content">
                <p>※ 관련 규정에 따라 비상구 좌석 이용이 부적합한 승객의 경우, 해당 좌석 배정이 제한될 수 있습니다.</p>
                <p>※ 항공기 기종 및 운항 스케줄은 사전 예고 없이 변경될 수 있으며, 이로 인해 사전 구매한 좌석이 변경될 수 있습니다.</p>
                <p>※ 항공기 운영 상황에 따라 일부 좌석은 사전 좌석 지정이 제한될 수 있는 점 양해 부탁드립니다.</p>
                <p>※ 좌석 전면의 격벽 유무는 탑승 후 환불 사유로 인정되지 않으므로, 구매 시 반드시 확인하시기 바랍니다.</p>
                <p>※ 사전 구매한 좌석은 변경이 불가하며, 변경을 원하실 경우 취소(환불) 후 재구매하셔야 합니다.</p>
            </div>

            <!-- Terms Accordion -->
            <div class="seat-terms">
                <button class="seat-terms__header" id="terms-toggle" aria-expanded="false">
                    <img src="${pageContext.request.contextPath}/resources/seat/img/check-circle.svg" alt="" class="seat-terms__icon" />
                    <span class="seat-terms__title">[필수] 좌석 선택 규정</span>
                    <img src="${pageContext.request.contextPath}/resources/seat/img/chevron-up.svg" alt="" class="seat-terms__chevron" />
                </button>
                <div class="seat-terms__content" id="terms-content" hidden>
                    <h4>사전 좌석 선택</h4>
                    <ul>
                        <li>좌석 선택은 항공권 예매 시 또는 예매 완료 후 사전 구매를 통해 이용할 수 있습니다.</li>
                        <li>일부 항공권, 운임 종류 또는 항공편의 경우 사전 좌석 선택이 제한될 수 있습니다.</li>
                    </ul>
                    
                    <h4>좌석 배정 제한</h4>
                    <ul>
                        <li>비상구 좌석은 관련 법령 및 항공사 규정에 따라 이용이 제한될 수 있으며, 해당 좌석 이용이 부적합하다고 판단되는 승객에게는 배정되지 않을 수 있습니다.</li>
                        <li>항공기 운영상 필요에 따라 일부 좌석은 사전 지정이 불가할 수 있습니다.</li>
                    </ul>
                    
                    <h4>항공기 변경 및 좌석 변경</h4>
                    <ul>
                        <li>항공기 기종 또는 운항 스케줄은 사전 예고 없이 변경될 수 있으며, 이로 인해 사전 구매한 좌석이 변경될 수 있습니다.</li>
                        <li>항공기 변경 등 불가피한 사유로 좌석이 변경되는 경우, 항공사는 대체 좌석을 배정할 수 있습니다.</li>
                    </ul>
                    
                    <h4>좌석 형태 및 편의 사양</h4>
                    <ul>
                        <li>좌석 전면의 격벽 유무, 좌석 간 간격, 창가·통로 여부 등은 항공기 기종에 따라 달라질 수 있습니다.</li>
                        <li>좌석 형태 또는 편의 사양에 대한 차이는 탑승 후 환불 사유로 인정되지 않습니다.</li>
                    </ul>
                    
                    <h4>좌석 변경 및 환불</h4>
                    <ul>
                        <li>사전 구매한 좌석은 변경이 불가하며, 변경을 원하는 경우 좌석 구매를 취소(환불)한 후 재구매해야 합니다.</li>
                        <li>좌석 취소 및 환불은 항공사 및 판매처의 환불 규정에 따라 처리됩니다.</li>
                    </ul>
                    
                    <h4>현장 배정</h4>
                    <ul>
                        <li>사전 좌석을 구매하지 않은 경우, 좌석은 체크인 시 자동 배정될 수 있으며 좌석 위치는 선택이 제한될 수 있습니다.</li>
                    </ul>
                    
                    <h4>기타</h4>
                    <ul>
                        <li>본 규정에 명시되지 않은 사항은 해당 항공사의 좌석 정책 및 운송 약관을 따릅니다.</li>
                    </ul>
                </div>
            </div>
        </section>
    </div>

    <script>
        // Terms accordion toggle
        document.getElementById('terms-toggle').addEventListener('click', function() {
            const content = document.getElementById('terms-content');
            const isExpanded = this.getAttribute('aria-expanded') === 'true';
            
            this.setAttribute('aria-expanded', !isExpanded);
            content.hidden = isExpanded;
            this.classList.toggle('seat-terms__header--expanded', !isExpanded);
        });
    </script>

    <script src="${pageContext.request.contextPath}/resources/seat/seat-select.js"></script>

</body>
</html>
