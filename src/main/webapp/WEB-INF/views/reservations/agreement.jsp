<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>약관 동의</title>

    <style>
        :root{
            --bg:#f5f7fb;
            --card:#ffffff;
            --line:#e8edf3;
            --text:#111;
            --muted:#666;
            --primary:#1f6feb;
            --danger:#d32f2f;
            --shadow:0 2px 10px rgba(0,0,0,0.06);
            --radius:10px;
        }

        * { box-sizing: border-box; }

        body{
            margin:0;
            font-family: Arial, Helvetica, sans-serif;
            background: var(--bg);
            color: var(--text);
        }

        /* ✅ 반응형 컨테이너: 고정 width 제거 */
        .container{
            max-width: 880px;
            margin: 0 auto;
            padding: 16px 16px 60px;
        }

        /* Top bar */
        .topbar{
            display:flex;
            align-items:center;
            justify-content:space-between;
            gap:12px;
            flex-wrap:wrap;
            padding: 10px 0 16px;
        }
        .brand{ font-weight:700; letter-spacing:0.5px; }
        .menu{ font-size:12px; color:var(--muted); }

        /* Card base */
        .card{
            background: var(--card);
            border-radius: var(--radius);
            padding: 16px 18px;
            box-shadow: var(--shadow);
        }
        .section{ margin-top: 16px; }
        .subTitle{ font-weight:700; margin-bottom:10px; }

        /* Header + steps */
        .header{
            background: var(--card);
            border-radius: var(--radius);
            padding: 18px 20px;
            box-shadow: var(--shadow);
        }
        .titleRow{
            display:flex;
            align-items:flex-start;
            justify-content:space-between;
            gap: 12px;
            flex-wrap:wrap;
        }
        .title{ font-size:18px; font-weight:700; }

        .steps{
            display:flex;
            align-items:center;
            gap:10px;
            flex-wrap:wrap; /* ✅ 좁으면 줄바꿈 */
        }
        .step{
            display:flex;
            align-items:center;
            gap:6px;
            color:#9aa3ad;
            font-size:12px;
            white-space:nowrap;
        }
        .dot{
            width:18px;
            height:18px;
            border-radius:50%;
            display:inline-flex;
            align-items:center;
            justify-content:center;
            border:1px solid #cfd6df;
            font-size:11px;
            flex: 0 0 auto;
        }
        .active{ color:var(--primary); font-weight:700; }
        .active .dot{ border-color:var(--primary); color:var(--primary); }

        /* Flight summary */
        .flightBox{
            background:#eef6ff;
            border-radius: var(--radius);
            padding: 14px;
        }

        /* ✅ 4칸 고정 flex -> 반응형 grid */
        .flightGrid{
            display:grid;
            grid-template-columns: repeat(4, minmax(0, 1fr));
            gap: 12px;
            align-items:start;
        }
        .flightCol{
            font-size:12px;
            color:#333;
            min-width:0;
        }
        .flightCol strong{
            display:block;
            font-size:13px;
            margin-bottom:4px;
        }
        .pill{
            display:inline-block;
            padding:2px 8px;
            border-radius:999px;
            background:var(--primary);
            color:#fff;
            font-size:11px;
        }

        /* Notice */
        .notice{
            font-size:12px;
            color:#555;
            line-height:1.6;
        }

        /* Terms */
        .error{
            color:var(--danger);
            font-size:12px;
            margin: 10px 0 0;
        }

        .agreeAllRow{
            display:flex;
            align-items:center;
            gap:10px;
            padding: 10px 0 14px;
            border-bottom: 1px solid var(--line);
        }
        .agreeAllRow label{ font-weight:700; }

        .termsList{ margin-top: 6px; }

        .termItem{
            display:flex;
            align-items:flex-start;
            gap:10px;
            padding: 10px 0;
            border-bottom: 1px solid var(--line);
        }
        .termMain{
            flex:1;
            min-width:0;
        }
        .termTitleRow{
            display:flex;
            gap:8px;
            align-items:center;
            justify-content:space-between;
            cursor:pointer;
            user-select:none;
        }
        .termLeft{
            display:flex;
            align-items:center;
            gap:10px;
            min-width:0;
        }
        .badgeRequired{
            color:var(--primary);
            font-weight:700;
            font-size:12px;
            flex:0 0 auto;
        }
        .badgeOptional{
            color:#6b7280;
            font-weight:700;
            font-size:12px;
            flex:0 0 auto;
        }
        .termName{
            font-size:13px;
            overflow:hidden;
            text-overflow:ellipsis;
            white-space:nowrap;
        }
        .chev{
            color:#6b7280;
            font-size:14px;
            flex:0 0 auto;
            transform: rotate(0deg);
            transition: transform .15s ease;
        }
        .termBody{
            display:none;
            margin-top:10px;
            font-size:12px;
            color:#666;
            line-height:1.6;
        }
        .termItem.open .termBody{ display:block; }
        .termItem.open .chev{ transform: rotate(180deg); }

        /* Bottom buttons */
        .btnRow{
            margin-top: 16px;
            display:flex;
            gap:12px;
        }
        .btn{
            width:50%;
            padding: 14px 0;
            border-radius: 8px;
            border:0;
            font-size:14px;
            cursor:pointer;
        }
        .btnCancel{ background:#dfe3e8; color:#333; }
        .btnNext{ background:var(--primary); color:#fff; font-weight:700; }
        .btnNext:disabled{
            opacity:.55;
            cursor:not-allowed;
        }

        /* ✅ 반응형 브레이크포인트 */
        @media (max-width: 820px){
            .flightGrid{ grid-template-columns: repeat(2, minmax(0, 1fr)); }
            .btn{ width: 100%; }
            .btnRow{ flex-direction: column; }
        }
        @media (max-width: 480px){
            .header{ padding: 16px; }
            .card{ padding: 14px; }
            .flightBox{ padding: 12px; }
            .flightGrid{ grid-template-columns: 1fr; }
            .menu{ width:100%; }
        }
    </style>
</head>

<body>
<div class="container">

    <!-- Top bar -->
    <div class="topbar">
        <div class="brand">flyway</div>
        <div class="menu">로그인 | 마이페이지 | 고객센터</div>
    </div>

    <!-- Header with steps -->
    <div class="header">
        <div class="titleRow">
            <div class="title">예약하기</div>
            <div class="steps">
                <div class="step active"><span class="dot">1</span> <span>약관동의</span></div>
                <div class="step"><span class="dot">2</span> <span>예약자/탑승객</span></div>
                <div class="step"><span class="dot">3</span> <span>결제정보</span></div>
            </div>
        </div>
    </div>

    <!-- Flight summary -->
    <div class="section card">
        <div class="subTitle">예약 항공편</div>
        <c:forEach var="seg" items="${segments}">
            <div class="flightBox" style="margin-bottom: 10px;">
                <div class="flightGrid">
                    <div class="flightCol">
                        <strong><c:out value="${seg.snapDepartureTime.toLocalDate()}"/></strong>
                        <div><c:out value="${seg.snapDepartureCity}"/>(<c:out value="${seg.snapDepartureAirport}"/>) →
                            <c:out value="${seg.snapArrivalCity}"/>(<c:out value="${seg.snapArrivalAirport}"/>)</div>
                    </div>
                    <div class="flightCol">
                        <strong><c:out value="${seg.snapDepartureTime.toLocalTime()}"/></strong>
                        <div><span class="pill">직항</span></div>
                    </div>
                    <div class="flightCol">
                        <strong><c:out value="${seg.snapAirlineName}"/></strong>
                        <div><c:out value="${seg.snapFlightNumber}"/></div>
                    </div>
                    <div class="flightCol">
                        <strong><c:out value="${seg.snapArrivalTime.toLocalTime()}"/></strong>
                        <div>도착</div>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>

    <!-- Notices -->
    <div class="section card">
        <div class="subTitle">유의사항</div>
        <div class="notice">
            ※ 출국 전 경유지 및 목적지의 필요 서류, 비자, 자격 요건 등을 반드시 확인하시어 불이익이 발생하지 않도록 사전에 준비하시기 바랍니다.<br/>
            ※ 편도 항공권으로 여행하시는 경우, 입국 국가의 유효한 비자를 반드시 소지하셔야 합니다. 비자를 소지하지 않은 경우 해당 국가로의 출국이 거부될 수
            있습니다.<br/>
            (일부 국가는 편도 항공권만 소지한 경우 탑승이 제한될 수 있으니 각별히 유의하시기 바랍니다.)<br/>
            ※ 무비자 입국이 가능한 국가라도 편도 항공권으로는 입국이 제한될 수 있으므로, 해당 국가 대사관 또는 공식 기관을 통해 입국 조건을 사전에 확인하시
            기 바랍니다.<br/>
            ※ 항공기 탑승 및 입국 관련 사항은 탑승객 본인의 책임 하에 확인해야 하며, 탑승 거절 또는 입국 거부가 발생하더라도 여행사는 이에 대한 책임을 지지
            않습니다.<br/>
            ※ 일부 특가 운임은 무료 수하물이 포함되지 않을 수 있으므로, 항공 스케줄 및 운임 상세 내용을 반드시 확인하시기 바랍니다.<br/>
            ※ 항공권 취소 수수료는 항공사별 규정에 따라 상이하므로, 해당 항공권의 요금 규정을 반드시 확인하시기 바랍니다.<br/>
            ※ 환불이 가능한 항공권의 경우, 요금 규정에 명시된 항공권 취소 수수료 외에 여행업무대행수수료가 별도로 부과될 수 있습니다.
        </div>
    </div>

    <!-- Terms -->
    <div class="section card">
        <div class="subTitle">약관 동의</div>

        <!-- 서버에서 넘어온 error 파라미터 -->
        <c:if test="${param.error eq 'agreeRequired'}">
            <div class="error">모든 필수 약관에 동의해야 다음 단계로 이동할 수 있습니다.</div>
        </c:if>

        <form method="post" id="agreeForm">
            <!-- 서버는 agreeAll 파라미터만 보므로, 아래 JS가 true/false를 세팅 -->
            <input type="hidden" name="agreeAll" id="agreeAllHidden" value="false"/>

            <!-- 전체 동의 -->
            <div class="agreeAllRow">
                <input id="agreeAll" type="checkbox"/>
                <label for="agreeAll">전체 동의</label>
            </div>

            <!-- 개별 약관 -->
            <div class="termsList">

                <!-- 필수1 -->
                <div class="termItem open" data-required="true">
                    <input class="termChk" type="checkbox" id="t1"/>
                    <div class="termMain">
                        <div class="termTitleRow" data-toggle>
                            <div class="termLeft">
                                <span class="badgeRequired">[필수]</span>
                                <span class="termName">결제 및 서비스 약관 동의</span>
                            </div>
                            <span class="chev">▾</span>
                        </div>
                        <div class="termBody">
                            결제 관련 유의사항과 서비스 제공 조건 (임시 텍스트)<br/>
                        </div>
                    </div>
                </div>

                <!-- 필수2 -->
                <div class="termItem" data-required="true">
                    <input class="termChk" type="checkbox" id="t2"/>
                    <div class="termMain">
                        <div class="termTitleRow" data-toggle>
                            <div class="termLeft">
                                <span class="badgeRequired">[필수]</span>
                                <span class="termName">개인정보 수집·이용 동의</span>
                            </div>
                            <span class="chev">▾</span>
                        </div>
                        <div class="termBody">
                            예약 처리 및 고객 응대를 위해 최소한의 개인정보를 수집 (임시 텍스트)
                        </div>
                    </div>
                </div>

                <!-- 필수3 -->
                <div class="termItem" data-required="true">
                    <input class="termChk" type="checkbox" id="t3"/>
                    <div class="termMain">
                        <div class="termTitleRow" data-toggle>
                            <div class="termLeft">
                                <span class="badgeRequired">[필수]</span>
                                <span class="termName">제3자 제공 동의(항공사)</span>
                            </div>
                            <span class="chev">▾</span>
                        </div>
                        <div class="termBody">
                            제3자 제공 동의 (임시 텍스트)
                        </div>
                    </div>
                </div>



            </div>

            <div class="btnRow">
                <button type="button" class="btn btnCancel" onclick="history.back()">취소</button>
                <button type="submit" class="btn btnNext" id="nextBtn" disabled>다음</button>
            </div>
        </form>
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
                item.classList.toggle('open');
            });
        });

        // 초기 상태 반영
        syncState();
    })();
</script>

</body>
</html>
