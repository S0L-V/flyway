<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>부가서비스 선택</title>
    <style>
        * { box-sizing: border-box; }
        body { font-family: Arial, sans-serif; margin: 0; padding: 16px; }

        .tabs { display: flex; border-bottom: 2px solid #1f6feb; margin-bottom: 16px; }
        .tab { padding: 12px 24px; cursor: pointer; border: none; background: #f0f0f0; margin-right: 4px; border-radius: 8px 8px 0 0; }
        .tab.active { background: #1f6feb; color: #fff; }
        .tab:disabled { opacity: 0.4; cursor: not-allowed; }

        .tab-content { display: none; }
        .tab-content.active { display: block; }

        .segment-box { border: 1px solid #ddd; border-radius: 8px; padding: 12px; margin-bottom: 16px; }
        .segment-title { font-weight: bold; margin-bottom: 12px; color: #333; }

        .passenger-box { background: #f9f9f9; border-radius: 6px; padding: 12px; margin-bottom: 12px; }
        .passenger-name { font-weight: bold; margin-bottom: 8px; }

        .form-row { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
        .form-row label { min-width: 100px; color: #555; }
        .form-row select { padding: 6px 12px; border: 1px solid #ccc; border-radius: 4px; }
        .form-row .price { color: #1f6feb; font-weight: bold; min-width: 100px; text-align: right; }

        .policy-info { background: #e8f4fd; padding: 12px; border-radius: 6px; margin-bottom: 16px; font-size: 0.9em; }
        .policy-info b { color: #1f6feb; }

        .meal-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(120px, 1fr)); gap: 10px; margin-top: 8px; }
        .meal-item { border: 2px solid #ddd; border-radius: 8px; padding: 8px; text-align: center; cursor: pointer; font-size: 12px; }
        .meal-item:hover { border-color: #1f6feb; }
        .meal-item.selected { border-color: #1f6feb; background: #e8f0fe; }
        .meal-item img { width: 100%; height: 60px; object-fit: cover; border-radius: 4px; margin-bottom: 4px; }

        .footer { position: sticky; bottom: 0; background: #fff; border-top: 1px solid #ddd; padding: 16px; margin: 16px -16px -16px; display: flex; justify-content: space-between; align-items: center; }
        .total-price { font-size: 1.2em; }
        .total-price span { color: #1f6feb; font-weight: bold; }

        .btn { padding: 10px 24px; border: 1px solid #333; background: #fff; cursor: pointer; border-radius: 6px; }
        .btn.primary { background: #1f6feb; color: #fff; border-color: #1f6feb; }
    </style>
</head>
<body>

<h3 style="margin-top:0;">부가서비스 선택</h3>

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

    // 기내식 선택
    function selectMeal(passengerId, segmentId, mealId) {
        var key = passengerId + '_' + segmentId;

        // 기존 선택 해제
        document.querySelectorAll('[id^="meal_' + passengerId + '_' + segmentId + '_"]')
            .forEach(function(el) { el.classList.remove('selected'); });

        // 새로 선택
        document.getElementById('meal_' + passengerId + '_' + segmentId + '_' + mealId).classList.add('selected');
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

            // 부모 창에 총액 갱신 요청
            if (window.opener && window.opener.refreshServiceTotal) {
                window.opener.refreshServiceTotal();
            }
            alert('저장되었습니다.');
            window.close();

        } catch (err) {
            alert('저장 실패: ' + err.message);
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