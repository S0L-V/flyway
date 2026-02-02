// 가격 변동 그래프 전역 상태
const priceGraphCache = new Map();      // key: `${flightId}:${cabin}` -> points[]
const priceChartInstances = new Map(); // key: `${flightId}:${cabin}` -> Chart instance
let openedGraphCard = null;
const sumGraphCache = new Map();        // key: `SUM:${outId}:${inId}:${cabin}` -> points

// Chart.js "현재가 말풍선" 플러그인 (전역 1회)
const lastPriceLabelPlugin = {
    id: "lastPriceLabelPlugin",
    afterDatasetsDraw(chart) {
        const { ctx } = chart;
        const meta = chart.getDatasetMeta(0);
        if (!meta || !meta.data || meta.data.length === 0) return;

        const lastIndex = meta.data.length - 1;
        const lastPoint = meta.data[lastIndex];
        if (!lastPoint) return;

        const dataset = chart.data.datasets[0];
        const lastValue = dataset.data[lastIndex];
        if (lastValue == null) return;

        const text = `현재가 ${Number(lastValue).toLocaleString("ko-KR")}원`;

        // 말풍선 위치
        let x = lastPoint.x + 10;
        const y = lastPoint.y;

        ctx.save();
        ctx.font = "12px Pretendard, sans-serif";
        ctx.textBaseline = "middle";

        const paddingX = 10;
        const textWidth = ctx.measureText(text).width;
        const boxWidth = textWidth + paddingX * 2;
        const boxHeight = 24;

        // 차트 영역 밖으로 나가면 왼쪽으로
        const chartRight = chart.chartArea.right;
        if (x + boxWidth > chartRight) x = lastPoint.x - boxWidth - 10;

        const boxX = x;
        const boxY = y - boxHeight / 2;

        const color = dataset.borderColor || "#2563eb";
        ctx.fillStyle = color;

        roundRect(ctx, boxX, boxY, boxWidth, boxHeight, 10);
        ctx.fill();

        ctx.fillStyle = "#fff";
        ctx.fillText(text, boxX + paddingX, y);

        ctx.restore();
    }
};

function renderSegment(f) {
    const flightNumber = f.flightNumber ?? "-";
    const depAirport = f.departureAirport ?? "-";
    const arrAirport = f.arrivalAirport ?? "-";
    const depTime = formatDepTime(f.departureTime);
    const arrTime = formatArrTime(f.departureTime, f.arrivalTime);
    const durationMinutes = Number.isFinite(f.durationMinutes) ? f.durationMinutes : null;

    // 시간 표시
    let time = "-";
    if (durationMinutes !== null) {
        const hours = Math.floor(durationMinutes / 60);
        const minutes = durationMinutes % 60;
        time = hours === 0 ? `${minutes}분` : `${hours}시간 ${minutes}분`;
    }

    // 항공사, 아이콘 표시
    let airlineName = "항공사";
    let airlineLogoUrl = "";

    const prefix = flightNumber.substring(0, 2).toUpperCase();

    if (prefix === 'OZ') {
        airlineName = '아시아나항공';
        airlineLogoUrl = '/resources/search/img/asiana-logo.svg';
    } else if (prefix === 'KE') {
        airlineName = '대한항공';
        airlineLogoUrl = '/resources/search/img/korean-logo.svg';
    }

    // 직항/경유 여부 (임시 로직: durationMinutes가 있으면 직항으로 간주)
    const isDirect = true; 
    const directBadge = isDirect 
        ? `<span class="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold bg-green-50 text-green-600 border border-green-100 font-pretendard">직항</span>`
        : `<span class="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold bg-gray-100 text-gray-600 border border-gray-200 font-pretendard">경유</span>`;

    // 도착일 +1일 체크
    const depDateObj = new Date(f.departureTime);
    const arrDateObj = new Date(f.arrivalTime);
    const dayDiff = isNextDay(depDateObj, arrDateObj);
    const nextDayBadge = dayDiff > 0 ? `<span class="text-[10px] font-bold text-red-500 mt-1 font-pretendard">+${dayDiff}일</span>` : '';

    return `
      <div class="flex flex-col sm:flex-row sm:items-center py-8 gap-4 sm:gap-6 border-b border-gray-100 last:border-0 group px-5 sm:px-7 font-pretendard">
        <!-- Airline -->
        <div class="flex items-center gap-3 min-w-[140px]">
            <div class="w-10 h-10 rounded-full overflow-hidden bg-white border border-gray-100 flex items-center justify-center p-1.5 shrink-0 shadow-sm">
                <img src="${airlineLogoUrl}" alt="${airlineName}" class="w-full h-full object-contain" />
            </div>
            <div class="flex flex-col">
                <span class="text-sm font-bold text-gray-900 font-pretendard">${airlineName}</span>
                <span class="text-xs text-gray-400 font-medium font-pretendard">${flightNumber}</span>
            </div>
        </div>

        <!-- Timeline -->
        <div class="flex-1 flex items-center justify-between gap-2 sm:gap-6">
            <div class="text-right min-w-[70px]">
                <div class="text-xl font-bold text-gray-900 leading-tight font-pretendard">${depTime}</div>
                <div class="text-xs font-semibold text-gray-400 bg-gray-100 px-1.5 py-0.5 rounded inline-block mt-1 font-pretendard">${depAirport}</div>
            </div>

            <div class="flex-1 flex flex-col items-center px-2">
                <div class="text-xs text-gray-500 mb-1 font-medium font-pretendard">${time}</div>
                <div class="w-full h-[2px] bg-gray-200 relative flex items-center justify-center">
                    <div class="absolute w-1.5 h-1.5 rounded-full bg-gray-300 left-0"></div>
                    <div class="absolute w-1.5 h-1.5 rounded-full bg-gray-300 right-0"></div>
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" data-lucide="plane" aria-hidden="true" class="lucide lucide-plane text-slate-400 transform rotate-45 w-4 h-4"><path d="M17.8 19.2 16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.5-.1 1 .3 1.3L9 12l-2 3H4l-1 1 3 2 2 3 1-1v-3l3-2 3.5 5.3c.3.4.8.5 1.3.3l.5-.2c.4-.3.6-.7.5-1.2z"></path></svg>
                </div>
                <div class="mt-1">
                    ${directBadge}
                </div>
            </div>

            <div class="text-left min-w-[70px]">
                <div class="flex items-start gap-1">
                    <span class="text-xl font-bold text-gray-900 leading-tight font-pretendard">${arrTime.split(' ')[0]}</span> <!-- +1일 제거된 시간만 -->
                    ${nextDayBadge}
                </div>
                <div class="text-xs font-semibold text-gray-400 bg-gray-100 px-1.5 py-0.5 rounded inline-block mt-1 font-pretendard">${arrAirport}</div>
            </div>
        </div>
      </div>
    `;
}

function renderFooter(option, index) {
    const seatCount = option.totalSeats ?? "-";
    const totalPrice = option.totalPrice ?? "-";

    let totalSeatCount = seatCount;
    let seatBadge = "";

    if (totalSeatCount !== "-" && totalSeatCount <= 9) {
        seatBadge = `
            <div class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold text-red-600 bg-red-50 whitespace-nowrap border border-red-100 font-pretendard">
                <i data-lucide="armchair" class="w-3.5 h-3.5"></i>
                ${totalSeatCount}석 남음
            </div>
        `;
    }
    
    const price = formatPrice(totalPrice);

    return `
      <div class="bg-gray-50/50 border-t border-gray-100 px-5 sm:px-7 py-4 flex flex-col sm:flex-row items-center justify-between gap-4 backdrop-blur-sm font-pretendard">
        <div class="flex items-center gap-3 w-full sm:w-auto overflow-x-auto pb-2 sm:pb-0 scrollbar-hide">
            <button class="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold text-blue-600 bg-gradient-to-b from-blue-50 to-blue-100 hover:from-blue-100 hover:to-blue-200 shadow-sm hover:shadow-md transition-all active:scale-95 whitespace-nowrap cursor-pointer font-pretendard border-0" type="button" data-action="open-graph">
                <i data-lucide="trending-up" class="w-3.5 h-3.5"></i>
                가격 변동
            </button>
            <button class="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold text-gray-600 bg-gradient-to-b from-gray-50 to-gray-100 hover:from-gray-100 hover:to-gray-200 shadow-sm hover:shadow-md transition-all active:scale-95 whitespace-nowrap cursor-pointer font-pretendard border-0" data-action="open-detail" onclick="openDetailPage(${index})">
                <i data-lucide="info" class="w-3.5 h-3.5"></i>
                여정 상세
            </button>
            ${seatBadge}
        </div>

        <div class="flex items-center justify-end gap-4 w-full sm:w-auto">
            <div class="text-right">
                <div class="text-2xl font-bold text-gray-900 tracking-tight font-pretendard">
                    ${price}<span class="text-lg font-medium text-gray-500 ml-1 font-pretendard">원</span>
                </div>
                <div class="text-[10px] text-gray-400 font-normal font-pretendard">유류할증료 및 세금 포함</div>
            </div>

            <button class="flex items-center justify-center w-auto h-auto px-6 py-3 bg-gradient-to-b from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white rounded-xl shadow-md hover:shadow-lg transition-all duration-200 active:scale-95 flight-select-btn cursor-pointer border-0 font-pretendard">
                <span class="font-bold text-sm font-pretendard">선택</span>
            </button>
        </div>
      </div>
    `;
}

// 아이콘 활성화
function refreshIcons() {
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
}

function createOneWayCard(option, index) {
    const f = option.outbound;

    return `
    <article class="bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 border border-gray-100 overflow-hidden w-full mx-auto flight-card mb-5 font-pretendard" data-out-id="${option.outbound.flightId}">
      ${renderSegment(f)}
      ${renderFooter(option, index)}
      
      <!-- 토글 인라인 그래프 패널 -->
      <div class="price-graph-panel" hidden>
        <div class="price-graph-head">
        </div>

        <div class="price-graph-body">
          <div class="price-graph-loading font-pretendard" hidden>불러오는 중..</div>
          <div class="price-graph-empty font-pretendard" hidden>가격 이력이 없습니다.</div>
          <canvas class="price-graph-canvas"></canvas>
        </div>

        <button type="button" class="price-graph-closebar font-pretendard" data-action="close-graph">
          그래프 닫기
        </button>
      </div>
    </article>
  `;
}

function createRoundTripCard(option, index) {
    const o = option.outbound;
    const i = option.inbound;

    return `
    <article class="bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 border border-gray-100 overflow-hidden w-full mx-auto flight-card mb-5 font-pretendard" data-out-id="${o.flightId}" data-in-id="${i.flightId}">
      ${renderSegment(o)}
      ${renderSegment(i)}
      ${renderFooter(option, index)}

      <!-- 토글 인라인 그래프 패널 -->
      <div class="price-graph-panel" hidden>
        <div class="price-graph-head">

          <div class="price-graph-tabs font-pretendard">
            <button type="button" class="price-graph-tab is-active"
                    data-action="graph-tab" data-target="sum">합산가</button>    
            <button type="button" class="price-graph-tab"
                    data-action="graph-tab" data-target="out">가는편</button>
            <button type="button" class="price-graph-tab"
                    data-action="graph-tab" data-target="in">오는편</button>
          </div>
        </div>

        <div class="price-graph-body">
          <div class="price-graph-loading font-pretendard" hidden>불러오는 중..</div>
          <div class="price-graph-empty font-pretendard" hidden>가격 이력이 없습니다.</div>
          <canvas class="price-graph-canvas"></canvas>
        </div>

        <button type="button" class="price-graph-closebar font-pretendard" data-action="close-graph">
          그래프 닫기
        </button>
      </div>
    </article>
  `;
}

// 현재가 표시 helper
function roundRect(ctx, x, y, w, h, r) {
    const radius = Math.min(r, w / 2, h / 2);
    ctx.beginPath();
    ctx.moveTo(x + radius, y);
    ctx.lineTo(x + w - radius, y);
    ctx.quadraticCurveTo(x + w, y, x + w, y + radius);
    ctx.lineTo(x + w, y + h - radius);
    ctx.quadraticCurveTo(x + w, y + h, x + w - radius, y + h);
    ctx.lineTo(x + radius, y + h);
    ctx.quadraticCurveTo(x, y + h, x, y + h - radius);
    ctx.lineTo(x, y + radius);
    ctx.quadraticCurveTo(x, y, x + radius, y);
    ctx.closePath();
}

// API 호출
async function fetchPriceHistory({ flightId, cabinClassCode, from, to }) {
    const params = new URLSearchParams({ flightId, cabinClassCode });
    if (from) params.set("from", from);
    if (to) params.set("to", to);

    const url = `${CONTEXT_PATH}/api/public/flights/price-history?${params.toString()}`;

    const res = await fetch(url, { headers: { "Accept": "application/json" } });
    if (!res.ok) throw new Error("price-history fetch failed");
    return await res.json(); // { points: [{t, price, type}, ...] }
}

// 시간 제거: 날짜(YYYY-MM-DD)로 정규화
function normalizePoints(points) {
    return (points || [])
        .filter(p => p && p.t && p.price != null)
        .map(p => ({
            t: String(p.t).includes("T") ? String(p.t).split("T")[0] : String(p.t).slice(0, 10),
            price: Number(p.price),
            type: p.type
        }));
}

// 왕복 합산 공식(서버 로직과 동일하게 맞춘 버전)
function calcRoundTripTotal(outPrice, inPrice) {
    const rawTotal = (outPrice + inPrice) * 10.0 / 14.0;
    return Math.floor((rawTotal + 500) / 1000) * 1000;
}

// 합산 시리즈 생성(날짜 기준)
function buildSumSeries(outPoints, inPoints) {
    const outMap = new Map(outPoints.map(p => [p.t, p.price]));
    const inMap  = new Map(inPoints.map(p => [p.t, p.price]));

    // 날짜 union (오름차순)
    const dates = Array.from(new Set([...outMap.keys(), ...inMap.keys()])).sort();

    const points = [];
    let lastOut = null;
    let lastIn = null;

    for (const d of dates) {
        if (outMap.has(d)) lastOut = outMap.get(d);
        if (inMap.has(d))  lastIn  = inMap.get(d);

        // carry 결과가 아직 없으면(초기 구간) 스킵
        if (lastOut == null || lastIn == null) continue;

        const sum = calcRoundTripTotal(lastOut, lastIn);
        points.push({ t: d, price: sum, type: "SUM" });
    }
    return points;
}

// 패널 내 차트를 안전하게 destroy (탭 전환시 누수 방지)
function destroyPanelChartIfAny(panel) {
    const prevKey = panel.dataset.currentChartKey;
    if (prevKey) {
        const inst = priceChartInstances.get(prevKey);
        if (inst) {
            inst.destroy();
            priceChartInstances.delete(prevKey);
        }
        panel.dataset.currentChartKey = "";
    }
}

// points를 받아 그래프 렌더링(공통)
async function renderGraphFromPoints(panel, points, chartKeyForPanel) {
    const loading = panel.querySelector(".price-graph-loading");
    const empty = panel.querySelector(".price-graph-empty");
    const canvas = panel.querySelector(".price-graph-canvas");

    loading.hidden = true;
    empty.hidden = true;

    if (!points || points.length === 0) {
        empty.hidden = false;
        destroyPanelChartIfAny(panel);
        return;
    }

    // panel 단위 기존 차트 제거
    destroyPanelChartIfAny(panel);

    // 그래프 데이터 (y값 / x축 라벨)
    const values = points.map(p => p.price);
    const labels = points.map(p => p.t);

    const lineColor = "#2563eb"; // 고정 색상

    // 점 크기(마지막 점 강조)
    const pointRadiusFn = (ctx) => (ctx.dataIndex === values.length - 1 ? 6 : 3);
    const pointHoverRadiusFn = (ctx) => (ctx.dataIndex === values.length - 1 ? 8 : 5);

    const chart = new Chart(canvas.getContext("2d"), {
        type: "line",
        data: {
            labels,
            datasets: [{
                label: "가격",
                data: values,
                tension: 0.25,
                borderColor: lineColor,
                backgroundColor: "transparent",
                pointRadius: pointRadiusFn,
                pointHoverRadius: pointHoverRadiusFn,
                pointHitRadius: 10
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                x: {
                    ticks: { maxTicksLimit: 6 }
                },
                y: {
                    ticks: {
                        callback: (v) => Number(v).toLocaleString("ko-KR")
                    }
                }
            }
        },
        plugins: [lastPriceLabelPlugin]
    });

    // panel에 현재 차트 키 기록
    panel.dataset.currentChartKey = chartKeyForPanel;

    // key→instance 관리
    priceChartInstances.set(chartKeyForPanel, chart);
}

// flightId 단위로 points를 가져와 렌더링
async function renderGraph(panel, flightId, cabin) {
    const loading = panel.querySelector(".price-graph-loading");
    const empty = panel.querySelector(".price-graph-empty");
    loading.hidden = false;
    empty.hidden = true;

    const key = `${flightId}:${cabin}`;

    try {
        let points = priceGraphCache.get(key);
        if (!points) {
            const resp = await fetchPriceHistory({ flightId, cabinClassCode: cabin });
            points = normalizePoints(resp?.points);
            priceGraphCache.set(key, points);
        }
        loading.hidden = true;
        await renderGraphFromPoints(panel, points, key);
    } catch (e) {
        console.error(e);
        loading.hidden = true;
        empty.hidden = false;
        destroyPanelChartIfAny(panel);
    }
}

// 왕복 합산 렌더링
async function renderSumGraph(panel, outId, inId, cabin) {
    const loading = panel.querySelector(".price-graph-loading");
    const empty = panel.querySelector(".price-graph-empty");
    loading.hidden = false;
    empty.hidden = true;

    const sumKey = `SUM:${outId}:${inId}:${cabin}`;

    try {
        let points = sumGraphCache.get(sumKey);
        if (!points) {
            const outKey = `${outId}:${cabin}`;
            const inKey = `${inId}:${cabin}`;

            let outPoints = priceGraphCache.get(outKey);
            if (!outPoints) {
                const outResp = await fetchPriceHistory({ flightId: outId, cabinClassCode: cabin });
                outPoints = normalizePoints(outResp?.points);
                priceGraphCache.set(outKey, outPoints);
            }

            let inPoints = priceGraphCache.get(inKey);
            if (!inPoints) {
                const inResp = await fetchPriceHistory({ flightId: inId, cabinClassCode: cabin });
                inPoints = normalizePoints(inResp?.points);
                priceGraphCache.set(inKey, inPoints);
            }

            points = buildSumSeries(outPoints, inPoints);
            sumGraphCache.set(sumKey, points);
        }

        loading.hidden = true;
        await renderGraphFromPoints(panel, points, sumKey);
    } catch (e) {
        console.error(e);
        loading.hidden = true;
        empty.hidden = false;
        destroyPanelChartIfAny(panel);
    }
}

function closeGraphForCard(card) {
    const panel = card.querySelector(".price-graph-panel");
    if (panel) {
        panel.hidden = true;
        destroyPanelChartIfAny(panel);
    }
    if (openedGraphCard === card) openedGraphCard = null;
}

// 그래프 열기(기본 탭: 왕복이면 합산, 편도면 out)
async function openGraphForCard(card) {
    const cabin = state.cabin;
    const panel = card.querySelector(".price-graph-panel");
    if (!panel) return;

    // 1개만 열기
    if (openedGraphCard && openedGraphCard !== card) closeGraphForCard(openedGraphCard);
    openedGraphCard = card;

    panel.hidden = false;

    const isRoundTrip = !!card.dataset.inId;
    const outId = card.dataset.outId;
    const inId = card.dataset.inId;

    // 탭 초기화
    const tabs = panel.querySelectorAll('button[data-action="graph-tab"]');
    if (tabs.length) {
        // 왕복이면 sum이 기본
        const defaultTarget = isRoundTrip ? "sum" : "out";
        tabs.forEach(b => b.classList.toggle("is-active", b.dataset.target === defaultTarget));
        panel.dataset.graphTarget = defaultTarget;
    }

    if (isRoundTrip) {
        await renderSumGraph(panel, outId, inId, cabin);
    } else {
        await renderGraph(panel, outId, cabin);
    }
}

function renderOneWay(options) {
    const el = document.getElementById("resultList");
    if (!el) return;

    if (!options || options.length === 0) {
        el.innerHTML = `<div class="empty">검색 결과가 없습니다.</div>`;
        return;
    }

    el.innerHTML = options
        .map((option, index) => createOneWayCard(option, index))
        .join("");
}

function renderRoundTrip(options) {
    const el = document.getElementById("resultList");
    if (!el) return;

    if (!options || options.length === 0) {
        el.innerHTML = `<div class="empty">검색 결과가 없습니다.</div>`;
        return;
    }

    el.innerHTML = options
        .map((option, index) => createRoundTripCard(option, index))
        .join("");
}


function renderByTripType(data) {
    const options = Array.isArray(data) ? data : (data.options ?? []);

    if (state.tripType === "OW") {
        renderOneWay(options);
    } else {
        renderRoundTrip(options);
    }

    // HTML이 DOM에 삽입된 후 아이콘 생성 실행
    refreshIcons();
}

// departureTime: [2026,2,1,8,45] → "08:45"
function formatDepTime(depTime) {
    if (!depTime) return "-";

    const date = new Date(depTime);
    const hh = String(date.getHours()).padStart(2, "0");
    const mm = String(date.getMinutes()).padStart(2, "0");

    return `${hh}:${mm}`;
}

function formatArrTime(depTime, arrTime) {
    if (!depTime || !arrTime) return "-";

    const depDate = new Date(depTime);
    const arrDate = new Date(arrTime);

    const hh = String(arrDate.getHours()).padStart(2, "0");
    const mm = String(arrDate.getMinutes()).padStart(2, "0");

    const dayDiff = isNextDay(depDate, arrDate);

    if (dayDiff > 0) {
        return `${hh}:${mm} +${dayDiff}일`;
    } else if (dayDiff < 0) {
        return `${hh}:${mm} ${dayDiff}일`;
    } else {
        return `${hh}:${mm}`;
    }
}

function isNextDay(depTime, arrTime) {
    const depDate = new Date(depTime.getFullYear(), depTime.getMonth(), depTime.getDate());
    const arrDate = new Date(arrTime.getFullYear(), arrTime.getMonth(), arrTime.getDate());

    const diffMs = arrDate - depDate;
    const oneDayMs = 24 * 60 * 60 * 1000;

    return Math.round(diffMs / oneDayMs);
}

function formatPrice(price) {
    return Number(price).toLocaleString('ko-KR');
}

document.getElementById("resultList").addEventListener("click", async (e) => {
    // 1) 탭 클릭 처리(왕복)
    const tabBtn = e.target.closest('button[data-action="graph-tab"]');
    if (tabBtn) {
        const card = e.target.closest(".flight-card");
        if (!card) return;
        const panel = card.querySelector(".price-graph-panel");
        if (!panel) return;

        const target = tabBtn.dataset.target; // sum|out|in
        const cabin = state.cabin;

        // active UI
        panel.querySelectorAll(".price-graph-tab").forEach(b => b.classList.remove("is-active"));
        tabBtn.classList.add("is-active");
        panel.dataset.graphTarget = target;

        const outId = card.dataset.outId;
        const inId = card.dataset.inId;

        if (target === "sum") {
            await renderSumGraph(panel, outId, inId, cabin);
        } else if (target === "in") {
            await renderGraph(panel, inId, cabin);
        } else {
            await renderGraph(panel, outId, cabin);
        }
        return;
    }

    // 2) 그래프 열기/닫기
    const graphBtn = e.target.closest('button[data-action="open-graph"]');
    const closeBtn = e.target.closest('button[data-action="close-graph"]');

    if (graphBtn || closeBtn) {
        const card = e.target.closest(".flight-card");
        if (!card) return;

        if (closeBtn) {
            closeGraphForCard(card);
            return;
        }

        const panel = card.querySelector(".price-graph-panel");
        if (panel && !panel.hidden) {
            closeGraphForCard(card);
        } else {
            await openGraphForCard(card);
        }
        return;
    }

    // 선택 버튼 클릭 시
    const selectBtn = e.target.closest(".flight-select-btn");
    if (selectBtn) {
        const card = selectBtn.closest(".flight-card");
        if (!card) return;

        const outId = card.dataset.outId;
        const inId = card.dataset.inId || null; // 편도일 경우 null
        let priceData = null;

        try {
            const params = new URLSearchParams({
                outFlightId: outId,
                cabinClassCode: state.cabin
            });
            if (inId) {
                params.set("inFlightId", inId);
            }

            const response = await fetch(`/api/public/flights/prices?${params}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error("가격 못 불러옴");
            }
            priceData = await response.json();
        } catch (error) {
            console.log("가격조회 실패", error);
            return;
        }

        const outFlight = priceData.find(p => p.flightId == outId);
        const inFlight = inId ? priceData.find(p => p.flightId == inId) : null;

        if(!outFlight) {
            alert("항공편 정보 x");
            return;
        }

        // hidden 폼에 값 설정
        document.getElementById("hiddenOutFlightId").value = card.dataset.outId;
        document.getElementById("hiddenInFlightId").value = card.dataset.inId || "";
        document.getElementById("hiddenPassengerCount").value = state.passengers;
        document.getElementById("hiddenCabinClassCode").value = state.cabin;
        document.getElementById("hiddenOutPrice").value = outFlight.flightPrice;
        document.getElementById("hiddenInPrice").value = inFlight ? inFlight.flightPrice : 0;

        // 폼 제출 → /reservations/draft → 동의 페이지로 redirect
        document.getElementById("reservationForm").submit();
        return;
    }
});