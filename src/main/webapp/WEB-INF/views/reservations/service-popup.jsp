<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>부가서비스 선택</title>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <link rel="preconnect" href="https://cdn.jsdelivr.net">
    <link href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }

        body {
            font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, sans-serif;
            background-color: #f5f7fb;
            min-height: 100vh;
            padding: 24px;
            color: #1a1a1a;
        }

        h3 {
            font-size: 1.5rem;
            font-weight: 700;
            color: #1f6feb;
            margin-bottom: 20px;
        }

        /* 탭 스타일 */
        .tabs {
            display: flex;
            gap: 8px;
            margin-bottom: 20px;
            padding: 4px;
            background: #ffffff;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        }
        .tab {
            flex: 1;
            padding: 12px 20px;
            cursor: pointer;
            border: none;
            background: transparent;
            color: #6b7280;
            border-radius: 8px;
            font-weight: 600;
            font-size: 0.9rem;
            transition: all 0.3s ease;
        }
        .tab:hover:not(:disabled) {
            background: #f3f4f6;
            color: #1f6feb;
        }
        .tab.active {
            background: #1f6feb;
            color: #fff;
            box-shadow: 0 4px 12px rgba(31, 111, 235, 0.3);
        }
        .tab:disabled {
            opacity: 0.4;
            cursor: not-allowed;
        }

        .tab-content { display: none; }
        .tab-content.active { display: block; }

        /* 구간 박스 */
        .segment-box {
            background: #ffffff;
            border-radius: 16px;
            padding: 20px;
            margin-bottom: 16px;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
        }
        .segment-title {
            font-weight: 700;
            font-size: 1rem;
            margin-bottom: 16px;
            color: #1a1a1a;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .segment-title::before {
            content: '✈';
            font-size: 1.1rem;
            color: #1f6feb;
        }
        .segment-title span {
            color: #9ca3af;
            font-weight: 400;
            font-size: 0.85rem;
        }

        /* 승객 박스 */
        .passenger-box {
            background: #f8fafc;
            border: 1px solid #e5e7eb;
            border-radius: 12px;
            padding: 16px;
            margin-bottom: 12px;
        }
        .passenger-name {
            font-weight: 600;
            margin-bottom: 12px;
            color: #1f6feb;
            font-size: 0.95rem;
        }

        /* 폼 요소 */
        .form-row {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 8px;
            flex-wrap: wrap;
        }
        .form-row label {
            min-width: 80px;
            color: #6b7280;
            font-size: 0.85rem;
            font-weight: 500;
        }
        .form-row select {
            padding: 10px 16px;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            background: #ffffff;
            color: #1a1a1a;
            font-size: 0.9rem;
            cursor: pointer;
            transition: all 0.2s;
        }
        .form-row select:hover {
            border-color: #1f6feb;
        }
        .form-row select:focus {
            outline: none;
            border-color: #1f6feb;
            box-shadow: 0 0 0 3px rgba(31, 111, 235, 0.15);
        }
        .form-row select option {
            background: #ffffff;
            color: #1a1a1a;
        }
        .form-row .price {
            color: #1f6feb;
            font-weight: 700;
            min-width: 100px;
            text-align: right;
            font-size: 1rem;
        }

        /* 안내 박스 */
        .policy-info {
            background: #eff6ff;
            border: 1px solid #bfdbfe;
            padding: 16px;
            border-radius: 12px;
            margin-bottom: 20px;
            font-size: 0.85rem;
            line-height: 1.6;
            color: #374151;
        }
        .policy-info b {
            color: #1f6feb;
            display: block;
            margin-bottom: 8px;
            font-size: 0.9rem;
        }

        /* 기내식 그리드 */
        .meal-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
            gap: 12px;
            margin-top: 12px;
        }
        .meal-item {
            border: 2px solid #e5e7eb;
            border-radius: 12px;
            padding: 12px;
            text-align: center;
            cursor: pointer;
            font-size: 0.8rem;
            background: #ffffff;
            transition: all 0.3s ease;
            color: #374151;
            position: relative;
        }
        .meal-item:hover {
            border-color: #1f6feb;
            background: #eff6ff;
            transform: translateY(-2px);
        }
        .meal-item.selected {
            border-color: #1f6feb;
            background: #eff6ff;
            box-shadow: 0 4px 12px rgba(31, 111, 235, 0.2);
        }
        .meal-item.selected::after {
            content: '✓';
            position: absolute;
            top: 8px;
            right: 8px;
            background: #1f6feb;
            color: #fff;
            width: 20px;
            height: 20px;
            border-radius: 50%;
            font-size: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .meal-item img {
            width: 100%;
            height: 70px;
            object-fit: cover;
            border-radius: 8px;
            margin-bottom: 8px;
        }

        /* 푸터 */
        .footer {
            position: sticky;
            bottom: 0;
            background: #ffffff;
            border-top: 1px solid #e5e7eb;
            padding: 20px;
            margin: 20px -24px -24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.04);
        }
        .total-price {
            font-size: 1rem;
            color: #6b7280;
        }
        .total-price span {
            color: #1f6feb;
            font-weight: 700;
            font-size: 1.3rem;
            margin-left: 8px;
        }

        /* 버튼 */
        .btn {
            padding: 12px 28px;
            border: 1px solid #e5e7eb;
            background: #ffffff;
            color: #374151;
            cursor: pointer;
            border-radius: 10px;
            font-weight: 600;
            font-size: 0.9rem;
            transition: all 0.2s;
        }
        .btn:hover {
            background: #f3f4f6;
            border-color: #d1d5db;
        }
        .btn.primary {
            background: #1f6feb;
            border: none;
            color: #fff;
            box-shadow: 0 4px 12px rgba(31, 111, 235, 0.3);
        }
        .btn.primary:hover {
            background: #1a5fd1;
            transform: translateY(-2px);
            box-shadow: 0 6px 16px rgba(31, 111, 235, 0.4);
        }

        .footer > div {
            display: flex;
            gap: 12px;
        }

        /* 스크롤바 */
        ::-webkit-scrollbar {
            width: 8px;
        }
        ::-webkit-scrollbar-track {
            background: #f1f5f9;
        }
        ::-webkit-scrollbar-thumb {
            background: #cbd5e1;
            border-radius: 4px;
        }
        ::-webkit-scrollbar-thumb:hover {
            background: #94a3b8;
        }
    </style>
</head>
<body>

<h3>부가서비스 선택</h3>

<!-- 탭 버튼 -->
<div class="tabs">
    <button class="tab active" onclick="showTab('baggage', this)">수하물</button>
    <c:if test="${vm.mealAvailable}">
        <button class="tab" onclick="showTab('meal', this)">기내식</button>
    </c:if>
    <c:if test="${!vm.mealAvailable}">
        <button class="tab" disabled title="국제선만 제공">기내식 (국제선만)</button>
    </c:if>
</div>

<!-- 수하물 탭 -->
<div id="baggage-tab" class="tab-content active">

    <c:if test="${vm.baggagePolicy != null}">
        <div class="policy-info">
            <b>수하물 정책</b><br>
            기본 제공: ${vm.baggagePolicy.freeCheckedWeightKg}kg × ${vm.baggagePolicy.freeCheckedBags}개<br>
            초과 요금: ₩<span id="policyOverweightFee">${vm.baggagePolicy.overweightFeePerKg}</span>/kg |
            추가 수하물: ₩<span id="policyExtraBagFee">${vm.baggagePolicy.extraBagFee}</span>/개
        </div>
    </c:if>

    <c:forEach var="seg" items="${vm.segments}">
        <div class="segment-box">
            <div class="segment-title">
                구간 ${seg.segmentOrder}: ${seg.snapDepartureAirport} → ${seg.snapArrivalAirport}
                <span style="color:#666; font-weight:normal;">
                  (${seg.snapDepartureTime.toLocalDate()} ${seg.snapDepartureTime.toLocalTime().withSecond(0)})
              </span>
            </div>

            <c:forEach var="pax" items="${vm.passengers}">
                <div class="passenger-box">
                    <div class="passenger-name">${pax.firstName} ${pax.lastName}</div>

                    <div class="form-row">
                        <label>추가 중량</label>
                        <c:set var="savedBaggage" value="${null}" />
                        <c:forEach var="bs" items="${pax.baggageServices}">
                            <c:if test="${bs.reservationSegmentId == seg.reservationSegmentId}">
                                <c:set var="savedBaggage" value="${bs}" />
                            </c:if>
                        </c:forEach>

                        <select id="extraKg_${pax.passengerId}_${seg.reservationSegmentId}"
                                onchange="calculateBaggagePrice()"
                                data-saved-details='${savedBaggage.serviceDetails}'>
                            <option value="0">0kg</option>
                            <option value="5">+5kg</option>
                            <option value="10">+10kg</option>
                            <option value="15">+15kg</option>
                            <option value="20">+20kg</option>
                        </select>

                        <select id="extraBags_${pax.passengerId}_${seg.reservationSegmentId}"
                                onchange="calculateBaggagePrice()">
                            <option value="0">0개</option>
                            <option value="1">1개</option>
                            <option value="2">2개</option>
                        </select>
                        <span class="price" id="price_${pax.passengerId}_${seg.reservationSegmentId}">₩0</span>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:forEach>
</div>

<!-- 기내식 탭 -->
<c:if test="${vm.mealAvailable}">
    <div id="meal-tab" class="tab-content">
        <div class="policy-info">
            <b>기내식 안내</b><br>
            국제선에서만 제공됩니다. 요금은 무료입니다.
        </div>

        <c:forEach var="seg" items="${vm.segments}">
            <div class="segment-box">
                <div class="segment-title">
                    구간 ${seg.segmentOrder}: ${seg.snapDepartureAirport} → ${seg.snapArrivalAirport}
                </div>

                <c:forEach var="pax" items="${vm.passengers}">
                    <div class="passenger-box">
                        <div class="passenger-name">${pax.firstName} ${pax.lastName}</div>

                        <div class="meal-grid">
                            <c:forEach var="meal" items="${vm.mealOptions}">
                                <c:set var="savedMeal" value="${null}" />
                                <c:forEach var="ms" items="${pax.mealServices}">
                                    <c:if test="${ms.reservationSegmentId == seg.reservationSegmentId}">
                                        <c:set var="savedMeal" value="${ms}" />
                                    </c:if>
                                </c:forEach>

                                <div class="meal-item ${savedMeal.mealId == meal.mealId ? 'selected' : ''}"
                                     id="meal_${pax.passengerId}_${seg.reservationSegmentId}_${meal.mealId}"
                                     onclick="selectMeal('${pax.passengerId}', '${seg.reservationSegmentId}', '${meal.mealId}')">
                                    <c:if test="${not empty meal.imageUrl}">
                                        <img src="${meal.imageUrl}" alt=""/>
                                    </c:if>
                                    <div>${meal.mealName}</div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:forEach>
    </div>
</c:if>

<!-- 하단 고정 -->
<div class="footer">
    <div class="total-price">총 추가 금액: <span id="totalServicePrice">₩0</span></div>
    <div>
        <button class="btn" onclick="window.close()">취소</button>
        <button class="btn primary" onclick="saveAndClose()">확인</button>
    </div>
</div>

<script type="module">
    import { fetchWithRefresh } from '/resources/common/js/authFetch.js';
    var reservationId = '${vm.reservationId}';
    var overweightFeePerKg = ${vm.baggagePolicy != null ? vm.baggagePolicy.overweightFeePerKg : 0};
    var extraBagFee = ${vm.baggagePolicy != null ? vm.baggagePolicy.extraBagFee : 0};
    var mealAvailable = ${vm.mealAvailable};
    var selectedMeals = {};

    // 탭 전환
    function showTab(tabName, btn) {
        document.querySelectorAll('.tab-content').forEach(function(el) {
            el.classList.remove('active');
        });
        document.querySelectorAll('.tab').forEach(function(el) {
            el.classList.remove('active');
        });
        document.getElementById(tabName + '-tab').classList.add('active');
        btn.classList.add('active');
    }

    // 수하물 가격 계산
    function calculateBaggagePrice() {
        var total = 0;

        <c:forEach var="seg" items="${vm.segments}">
        <c:forEach var="pax" items="${vm.passengers}">
        var kgSel = document.getElementById('extraKg_${pax.passengerId}_${seg.reservationSegmentId}');
        var bagSel = document.getElementById('extraBags_${pax.passengerId}_${seg.reservationSegmentId}');
        var priceEl = document.getElementById('price_${pax.passengerId}_${seg.reservationSegmentId}');

        if (kgSel && bagSel && priceEl) {
            var kg = parseInt(kgSel.value) || 0;
            var bags = parseInt(bagSel.value) || 0;
            var price = (kg * overweightFeePerKg) + (bags * extraBagFee);
            priceEl.textContent = '₩' + numberWithCommas(price);
            total += price;
        }
        </c:forEach>
        </c:forEach>

        document.getElementById('totalServicePrice').textContent = '₩' + numberWithCommas(total);
    }

    // 기내식 선택 (토글 가능)
    function selectMeal(passengerId, segmentId, mealId) {
        var key = passengerId + '_' + segmentId;
        var clickedEl = document.getElementById('meal_' + passengerId + '_' + segmentId + '_' + mealId);

        // 이미 선택된 것을 다시 클릭하면 선택 해제
        if (clickedEl.classList.contains('selected')) {
            clickedEl.classList.remove('selected');
            delete selectedMeals[key];
            return;
        }

        // 기존 선택 해제
        document.querySelectorAll('[id^="meal_' + passengerId + '_' + segmentId + '_"]')
            .forEach(function(el) { el.classList.remove('selected'); });

        // 새로 선택
        clickedEl.classList.add('selected');
        selectedMeals[key] = mealId;
    }
    // 저장 후 닫기
    async function saveAndClose() {
        // 1. 수하물 저장
        var baggageItems = [];
        <c:forEach var="seg" items="${vm.segments}">
        <c:forEach var="pax" items="${vm.passengers}">
        var kgSel = document.getElementById('extraKg_${pax.passengerId}_${seg.reservationSegmentId}');
        var bagSel = document.getElementById('extraBags_${pax.passengerId}_${seg.reservationSegmentId}');
        if (kgSel && bagSel) {
            baggageItems.push({
                passengerId: '${pax.passengerId}',
                reservationSegmentId: '${seg.reservationSegmentId}',
                extraWeightKg: parseInt(kgSel.value) || 0,
                extraBagCount: parseInt(bagSel.value) || 0
            });
        }
        </c:forEach>
        </c:forEach>

        try {
            // 수하물 저장
            const baggageRes = await fetchWithRefresh('/reservations/' + reservationId + '/services/baggage', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ items: baggageItems })
            });
            const baggageData = await baggageRes.json();
            if (!baggageData.success) {
                throw new Error('수하물 저장 실패');
            }

            // 기내식 저장 (국제선만)
            if (mealAvailable) {
                var mealItems = [];
                <c:forEach var="seg" items="${vm.segments}">
                <c:forEach var="pax" items="${vm.passengers}">
                var key = '${pax.passengerId}_${seg.reservationSegmentId}';
                mealItems.push({
                    passengerId: '${pax.passengerId}',
                    reservationSegmentId: '${seg.reservationSegmentId}',
                    mealId: selectedMeals[key] || null
                });
                </c:forEach>
                </c:forEach>

                const mealRes = await fetchWithRefresh('/reservations/' + reservationId + '/services/meal', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ items: mealItems })
                });
                await mealRes.json();
            }

            // 부모 창에 부가서비스 정보 갱신 요청
            if (window.opener && typeof window.opener.refreshServiceInfo === 'function') {
                window.opener.refreshServiceInfo();
            } else if (window.opener && typeof window.opener.refreshServiceTotal === 'function') {
                window.opener.refreshServiceTotal();  // fallback
            }
            await Swal.fire({
                icon: 'success',
                title: '저장 완료',
                text: '부가서비스가 저장되었습니다.',
                confirmButtonText: '확인',
                confirmButtonColor: '#1f6feb'
            });
            window.close();

        } catch (err) {
            Swal.fire({
                icon: 'error',
                title: '저장 실패',
                text: err.message,
                confirmButtonText: '확인',
                confirmButtonColor: '#1f6feb'
            });
        }
    }
    function numberWithCommas(x) {
        return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    }

    // 초기화
    document.addEventListener('DOMContentLoaded', function() {
        // 기존 수하물 선택값 로드
        document.querySelectorAll('[data-saved-details]').forEach(function(el) {
            var details = el.getAttribute('data-saved-details');
            if (details && details !== 'null') {
                try {
                    var parsed = JSON.parse(details);
                    var id = el.id;
                    if (id.startsWith('extraKg_')) {
                        el.value = parsed.extraKg || 0;
                        // 같은 승객/구간의 extraBags도 설정
                        var bagsId = id.replace('extraKg_', 'extraBags_');
                        var bagsEl = document.getElementById(bagsId);
                        if (bagsEl) bagsEl.value = parsed.extraBags || 0;
                    }
                } catch (e) {
                }
            }
        });

        // 기존 기내식 선택값을 selectedMeals에 저장
        document.querySelectorAll('.meal-item.selected').forEach(function(el) {
            var parts = el.id.split('_'); // meal_passengerId_segmentId_mealId
            if (parts.length >= 4) {
                var key = parts[1] + '_' + parts[2];
                selectedMeals[key] = parts[3];
            }
        });

        calculateBaggagePrice();
    });
    window.showTab = showTab;
    window.calculateBaggagePrice = calculateBaggagePrice;
    window.selectMeal = selectMeal;
    window.saveAndClose = saveAndClose;
</script>

</body>
</html>