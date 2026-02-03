<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>약관 동의 - Flyway</title>

    <jsp:include page="/WEB-INF/views/common/head.jsp" />
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary: '#1f6feb',
                        'primary-hover': '#165bca',
                    }
                }
            }
        }
    </script>
    <style>
        body { font-family: 'Pretendard', Arial, sans-serif; background-color: #f5f7fb; }
        .stepper__item.is-active .stepper__circle { background-color: #1f6feb; border-color: #1f6feb; color: white; }
        .stepper__item.is-completed .stepper__circle { background-color: #333; border-color: #333; color: white; }

        /* Flight Card Styles (Same as booking.jsp) */
        .flight-card {
            background: #ffffff;
            border-radius: 16px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.04);
            padding: 20px;
            margin-bottom: 16px;
        }
        .flight-time { font-size: 24px; font-weight: 800; color: #1a1a1a; line-height: 1; }
        .airport-code { font-size: 16px; font-weight: 700; color: #1a1a1a; margin-top: 4px; }
        .airport-name { font-size: 12px; color: #8a8a8a; margin-top: 2px; }
        .flight-duration { font-size: 12px; color: #9aa4b2; text-align: center; margin-bottom: 6px; }
        .timeline-line {
            height: 1px;
            background-image: linear-gradient(to right, #e2e8f0 50%, transparent 50%);
            background-size: 6px 1px;
            background-repeat: repeat-x;
            position: relative;
            width: 100%;
        }
        .timeline-icon {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: #fff;
            padding: 0 6px;
            color: #1f6feb;
        }
        .timeline-icon svg {
            width: 14px;
            height: 14px;
        }
    </style>
</head>

<body class="text-gray-900">
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="max-w-[1100px] mx-auto px-4 pb-20">

    <!-- Header & Stepper -->
    <div class="flex flex-col sm:flex-row items-center justify-between py-6 gap-4">
        <div class="text-2xl font-extrabold text-gray-900">예약하기</div>
        <ol class="flex gap-4 items-center m-0 p-0 list-none">
            <li class="flex flex-col items-center gap-1.5 text-xs font-bold text-primary is-active">
                <span class="stepper__circle w-6 h-6 rounded-full border border-gray-300 flex items-center justify-center text-xs font-extrabold bg-white">1</span>
                <span>약관동의</span>
            </li>
            <li class="flex flex-col items-center gap-1.5 text-xs font-bold text-gray-400">
                <span class="stepper__circle w-6 h-6 rounded-full border border-gray-300 flex items-center justify-center text-xs font-extrabold bg-white">2</span>
                <span>정보입력/결제</span>
            </li>
            <li class="flex flex-col items-center gap-1.5 text-xs font-bold text-gray-400">
                <span class="stepper__circle w-6 h-6 rounded-full border border-gray-300 flex items-center justify-center text-xs font-extrabold bg-white">3</span>
                <span>예약결과</span>
            </li>
        </ol>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-[1fr_340px] gap-6 items-start">

        <!-- Left Column: Flight Summary & Notices -->
        <div class="flex flex-col gap-4">

            <!-- Flight Summary (Redesigned & Compact) -->
            <div class="flex flex-col gap-3">
                <c:forEach var="s" items="${segments}">
                    <div class="flight-card">
                        <!-- Card Header -->
                        <div class="flex justify-between items-center mb-4 text-xs text-[#8a8a8a]">
                            <div class="flex items-center gap-1.5 font-bold text-[#1a1a1a]">
                                <span class="text-primary">✈</span>
                                <span>${s.segmentOrder == 1 ? '가는 편' : '오는 편'}</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <fmt:parseDate value="${s.snapDepartureTime}" pattern="yyyy-MM-dd'T'HH:mm" var="depDate"/>
                                <fmt:formatDate value="${depDate}" pattern="yyyy-MM-dd"/>
                                <span class="w-0.5 h-0.5 bg-gray-300 rounded-full"></span>
                                <span>${s.snapAirlineName} ${s.snapFlightNumber}</span>
                            </div>
                        </div>

                        <!-- Main Layout (3 Columns) -->
                        <div class="grid grid-cols-[1fr_1.2fr_1fr] items-center gap-2">
                            <!-- Left: Departure -->
                            <div class="text-left">
                                <div class="flight-time">
                                    <fmt:formatDate value="${depDate}" pattern="HH:mm"/>
                                </div>
                                <div class="airport-code uppercase">${s.snapDepartureAirport}</div>
                                <div class="airport-name">${s.snapDepartureCity}</div>
                            </div>

                            <!-- Center: Timeline -->
                            <div class="flex flex-col items-center w-full px-2">
                                <div class="flight-duration">
                                    <!-- Duration calculation logic -->
                                    <fmt:parseDate value="${s.snapArrivalTime}" pattern="yyyy-MM-dd'T'HH:mm" var="arrDate"/>
                                    <c:set var="durationMillis" value="${arrDate.time - depDate.time}" />
                                    <c:set var="durationHours" value="${durationMillis / (1000 * 60 * 60)}" />
                                    <c:set var="durationMinutes" value="${(durationMillis / (1000 * 60)) % 60}" />
                                    <fmt:formatNumber value="${durationHours}" pattern="#,##0" maxFractionDigits="0" />시간
                                    <fmt:formatNumber value="${durationMinutes}" pattern="#,##0" />분
                                </div>
                                <div class="timeline-line">
                                    <div class="timeline-icon">
                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" class="transform rotate-90">
                                            <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
                                        </svg>
                                    </div>
                                </div>
                            </div>

                            <!-- Right: Arrival -->
                            <div class="text-right">
                                <div class="flight-time">
                                    <fmt:formatDate value="${arrDate}" pattern="HH:mm"/>
                                </div>
                                <div class="airport-code uppercase">${s.snapArrivalAirport}</div>
                                <div class="airport-name">
                                    ${s.snapArrivalCity}
                                    <c:set var="depDayMillis"
                                           value="${Math.floor(depDate.time / (1000*60*60*24))}"/>
                                    <c:set var="arrDayMillis"
                                           value="${Math.floor(arrDate.time / (1000*60*60*24))}"/>

                                    <c:set var="dayDiff" value="${arrDayMillis - depDayMillis}"/>

                                    <c:if test="${dayDiff > 0}">
                                        <span class="text-red-500 font-bold ml-1">+<fmt:formatNumber value="${dayDiff}" maxFractionDigits="0" />일</span>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <!-- Notices -->
            <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
                <div class="flex items-center gap-2 mb-4 font-bold text-lg text-gray-900">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#f97316" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                    유의사항
                </div>
                <div class="text-xs text-gray-500 leading-relaxed space-y-2">
                    <p>※ 출국 전 경유지 및 목적지의 필요 서류, 비자, 자격 요건 등을 반드시 확인하시어 불이익이 발생하지 않도록 사전에 준비하시기 바랍니다.</p>
                    <p>※ 편도 항공권으로 여행하시는 경우, 입국 국가의 유효한 비자를 반드시 소지하셔야 합니다. 비자를 소지하지 않은 경우 해당 국가로의 출국이 거부될 수 있습니다.</p>
                    <p>※ 무비자 입국이 가능한 국가라도 편도 항공권으로는 입국이 제한될 수 있으므로, 해당 국가 대사관 또는 공식 기관을 통해 입국 조건을 사전에 확인하시기 바랍니다.</p>
                    <p>※ 항공기 탑승 및 입국 관련 사항은 탑승객 본인의 책임 하에 확인해야 하며, 탑승 거절 또는 입국 거부가 발생하더라도 여행사는 이에 대한 책임을 지지 않습니다.</p>
                    <p>※ 일부 특가 운임은 무료 수하물이 포함되지 않을 수 있으므로, 항공 스케줄 및 운임 상세 내용을 반드시 확인하시기 바랍니다.</p>
                    <p>※ 항공권 취소 수수료는 항공사별 규정에 따라 상이하므로, 해당 항공권의 요금 규정을 반드시 확인하시기 바랍니다.</p>
                </div>
            </div>

        </div>

        <!-- Right Column: Terms -->
        <div class="sticky top-6">
            <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
                <div class="font-bold text-lg text-gray-900 mb-4 pb-4 border-b border-gray-100">약관 동의</div>

                <!-- 서버에서 넘어온 error 파라미터 -->
                <c:if test="${param.error eq 'agreeRequired'}">
                    <div class="text-red-500 text-xs font-bold mb-4 bg-red-50 p-3 rounded-lg">
                        모든 필수 약관에 동의해야 다음 단계로 이동할 수 있습니다.
                    </div>
                </c:if>

                <form method="post" id="agreeForm">
                    <input type="hidden" name="agreeAll" id="agreeAllHidden" value="false"/>

                    <!-- 전체 동의 -->
                    <div class="flex items-center gap-3 py-3 border-b border-gray-100 mb-2">
                        <input id="agreeAll" type="checkbox" class="w-5 h-5 rounded border-gray-300 text-primary focus:ring-primary"/>
                        <label for="agreeAll" class="font-bold text-sm cursor-pointer select-none">전체 동의</label>
                    </div>

                    <!-- 개별 약관 -->
                    <div class="flex flex-col gap-0">
                        <!-- 필수1 -->
                        <div class="termItem py-3 border-b border-gray-50 last:border-0 open" data-required="true">
                            <div class="flex items-start gap-3">
                                <input class="termChk w-4 h-4 mt-0.5 rounded border-gray-300 text-primary focus:ring-primary" type="checkbox" id="t1"/>
                                <div class="flex-1 min-w-0">
                                    <div class="flex justify-between items-center cursor-pointer select-none" data-toggle>
                                        <div class="flex items-center gap-2 text-sm">
                                            <span class="text-primary font-bold text-xs">[필수]</span>
                                            <span class="truncate">결제 및 서비스 약관 동의</span>
                                        </div>
                                        <span class="chev text-gray-400 text-xs transform transition-transform">▼</span>
                                    </div>
                                    <div class="termBody hidden mt-2 text-xs text-gray-500 leading-relaxed bg-gray-50 p-3 rounded-lg">
                                        결제 관련 유의사항과 서비스 제공 조건 (임시 텍스트)<br/>
                                        전자상거래 등에서의 소비자보호에 관한 법률 등 관련 법령에 따릅니다.
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 필수2 -->
                        <div class="termItem py-3 border-b border-gray-50 last:border-0" data-required="true">
                            <div class="flex items-start gap-3">
                                <input class="termChk w-4 h-4 mt-0.5 rounded border-gray-300 text-primary focus:ring-primary" type="checkbox" id="t2"/>
                                <div class="flex-1 min-w-0">
                                    <div class="flex justify-between items-center cursor-pointer select-none" data-toggle>
                                        <div class="flex items-center gap-2 text-sm">
                                            <span class="text-primary font-bold text-xs">[필수]</span>
                                            <span class="truncate">개인정보 수집·이용 동의</span>
                                        </div>
                                        <span class="chev text-gray-400 text-xs transform transition-transform">▼</span>
                                    </div>
                                    <div class="termBody hidden mt-2 text-xs text-gray-500 leading-relaxed bg-gray-50 p-3 rounded-lg">
                                        예약 처리 및 고객 응대를 위해 최소한의 개인정보를 수집합니다.<br/>
                                        수집항목: 성명, 생년월일, 성별, 연락처, 이메일, 여권정보 등
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 필수3 -->
                        <div class="termItem py-3 border-b border-gray-50 last:border-0" data-required="true">
                            <div class="flex items-start gap-3">
                                <input class="termChk w-4 h-4 mt-0.5 rounded border-gray-300 text-primary focus:ring-primary" type="checkbox" id="t3"/>
                                <div class="flex-1 min-w-0">
                                    <div class="flex justify-between items-center cursor-pointer select-none" data-toggle>
                                        <div class="flex items-center gap-2 text-sm">
                                            <span class="text-primary font-bold text-xs">[필수]</span>
                                            <span class="truncate">제3자 제공 동의(항공사)</span>
                                        </div>
                                        <span class="chev text-gray-400 text-xs transform transition-transform">▼</span>
                                    </div>
                                    <div class="termBody hidden mt-2 text-xs text-gray-500 leading-relaxed bg-gray-50 p-3 rounded-lg">
                                        항공권 발권 및 운송 계약 이행을 위해 항공사에 개인정보를 제공합니다.
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="flex gap-3 mt-6 pt-4 border-t border-gray-100">
                        <button type="button" class="flex-1 py-3.5 bg-gray-200 text-gray-700 rounded-xl font-bold text-sm hover:bg-gray-300 transition-colors" onclick="history.back()">취소</button>
                        <button type="submit" class="flex-1 py-3.5 bg-primary text-white rounded-xl font-bold text-sm shadow-lg shadow-blue-500/20 hover:bg-blue-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed" id="nextBtn" disabled>다음 단계</button>
                    </div>
                </form>
            </div>
        </div>

    </div>
</div>

<script>
    (function () {
        const agreeAll = document.getElementById('agreeAll');
        const agreeAllHidden = document.getElementById('agreeAllHidden');
        const nextBtn = document.getElementById('nextBtn');

        const termItems = Array.from(document.querySelectorAll('.termItem'));
        const termChks  = Array.from(document.querySelectorAll('.termChk'));

        function requiredCheckedAll() {
            return termItems
                .filter(item => item.dataset.required === 'true')
                .every(item => item.querySelector('.termChk').checked);
        }

        function syncState() {
            const requiredOk = requiredCheckedAll();
            nextBtn.disabled = !requiredOk;

            // 서버는 agreeAll=true여야 통과시키므로,
            // 여기서 "필수 전부 체크"면 true로 세팅(=다음 버튼 활성 조건과 동일)
            agreeAllHidden.value = requiredOk ? 'true' : 'false';

            // 전체동의 체크 상태(필수+선택 모두 체크됐을 때만 true로 보이게)
            const allOk = termChks.every(chk => chk.checked);
            agreeAll.checked = allOk;
        }

        // 전체동의 클릭 → 개별 약관 모두 토글
        agreeAll.addEventListener('change', () => {
            termChks.forEach(chk => chk.checked = agreeAll.checked);
            syncState();
        });

        // 개별 체크 변경 → 상태 동기화
        termChks.forEach(chk => chk.addEventListener('change', syncState));

        // 아코디언 토글(제목줄 클릭 시 열고 닫기)
        document.querySelectorAll('[data-toggle]').forEach(el => {
            el.addEventListener('click', () => {
                const item = el.closest('.termItem');
                const body = item.querySelector('.termBody');
                const chev = item.querySelector('.chev');

                if (body.classList.contains('hidden')) {
                    body.classList.remove('hidden');
                    chev.style.transform = 'rotate(180deg)';
                } else {
                    body.classList.add('hidden');
                    chev.style.transform = 'rotate(0deg)';
                }
            });
        });

        // 초기 오픈 상태 처리 (첫번째 약관)
        const firstItem = document.querySelector('.termItem.open');
        if(firstItem) {
            const body = firstItem.querySelector('.termBody');
            const chev = firstItem.querySelector('.chev');
            body.classList.remove('hidden');
            chev.style.transform = 'rotate(180deg)';
        }

        // 초기 상태 반영
        syncState();
    })();
</script>

</body>
</html>