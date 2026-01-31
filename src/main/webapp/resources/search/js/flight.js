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
        ctx.font = "12px sans-serif";
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
    const seatCount = f.seatCount ?? "-";
    const depTime = formatDepTimeArray(f.departureTime);
    const arrTime = formatArrTimeArray(f.departureTime, f.arrivalTime);
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
    let airlineLogoHtml = "";

    const prefix = flightNumber.substring(0, 2).toUpperCase();

    if (prefix === 'OZ') {
        airlineName = '아시아나항공';
        airlineLogoHtml = '<img src="/resources/search/img/asiana-logo.svg" alt="Asiana" class="airline-logo">';
    } else if (prefix === 'KE') {
        airlineName = '대한항공';
        airlineLogoHtml = '<img src="/resources/search/img/korean-logo.svg" alt="Korean" class="airline-logo">';
    }

    return `
      <div class="flight-segment">
        <div class="airline-info-group">
          ${airlineLogoHtml}
          <div class="airline-text">
            <span class="flight-number">${airlineName}</span>
            <span class="flight-number">${flightNumber}</span>
          </div>
        </div>
        
        <div class="flight-details">
          <div class="flight-time">
            <div class="time-info">
              <div class="time">${depTime}</div>
              <div class="airport">${depAirport}</div>
            </div>
            <div class="duration-info">
              <span class="duration-badge">직항</span>
              <div class="duration-time">${time}</div>
            </div>
            <div class="time-info">
              <div class="time">${arrTime}</div>
              <div class="airport">${arrAirport}</div>
            </div>
            <div class="seats-remaining">${seatCount}석 남음</div>
          </div>
        </div>
      </div>
    `;
}
function renderFooter(option, index) {
    const seatCount = option.totalSeats ?? "-";
    const totalPrice = option.totalPrice ?? "-";

    const price = formatPrice(totalPrice);

    return `
      <div class="flight-footer">
        <div class="flight-actions">
          <button class="action-button" type="button" data-action="toggle-graph">가격 변동 그래프</button>
          <button class="action-button" onclick="openDetailPage(${index})">여정 상세</button>
        </div>
        <div class="seats-remaining">${seatCount}석 남음</div>
        <div class="flight-price" tabindex="0">
          <span class="price">${price}원</span>
          <img src="${CONTEXT_PATH}/resources/search/img/arrow-right.svg" alt="" class="price-arrow" />
        </div>
      </div>
    `;
}

function createOneWayCard(option, index) {
    const f = option.outbound;

    return `
    <article class="flight-card" data-out-id="${option.outbound.flightId}">
      ${renderSegment(f)}
      ${renderFooter(option, index)}
      
      <!-- 토글 인라인 그래프 패널 -->
      <div class="price-graph-panel" hidden>
        <div class="price-graph-head">
        </div>

        <div class="price-graph-body">
          <div class="price-graph-loading" hidden>불러오는 중..</div>
          <div class="price-graph-empty" hidden>가격 이력이 없습니다.</div>
          <canvas class="price-graph-canvas"></canvas>
        </div>

        <button type="button" class="price-graph-closebar" data-action="close-graph">
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
    <article class="flight-card" data-out-id="${o.flightId}" data-in-id="${i.flightId}">
      ${renderSegment(o)}
      ${renderSegment(i)}
      ${renderFooter(option, index)}

      <!-- 토글 인라인 그래프 패널 -->
      <div class="price-graph-panel" hidden>
        <div class="price-graph-head">

          <div class="price-graph-tabs">
            <button type="button" class="price-graph-tab is-active"
                    data-action="graph-tab" data-target="sum">합산가</button>    
            <button type="button" class="price-graph-tab is-active"
                    data-action="graph-tab" data-target="out">가는편</button>
            <button type="button" class="price-graph-tab"
                    data-action="graph-tab" data-target="in">오는편</button>
          </div>
        </div>

        <div class="price-graph-body">
          <div class="price-graph-loading" hidden>불러오는 중..</div>
          <div class="price-graph-empty" hidden>가격 이력이 없습니다.</div>
          <canvas class="price-graph-canvas"></canvas>
        </div>

        <button type="button" class="price-graph-closebar" data-action="close-graph">
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
}

// departureTime: [2026,2,1,8,45] → "08:45"
function formatDepTimeArray(arr) {
    if (!Array.isArray(arr) || arr.length < 5) return "-";
    const hh = String(arr[3]).padStart(2, "0");
    const mm = String(arr[4]).padStart(2, "0");
    return `${hh}:${mm}`;
}

function formatArrTimeArray(depArr, arrArr) {
    if (!Array.isArray(arrArr) || arrArr.length < 5) return "-";
    const hh = String(arrArr[3]).padStart(2, "0");
    const mm = String(arrArr[4]).padStart(2, "0");
    const day = isNextDay(depArr, arrArr);

    if(day > 0) {
        return `${hh}:${mm} +${day}일`;
    } else if(day < 0) {
        return `${hh}:${mm} ${day}일`;
    } else {
        return `${hh}:${mm}`;
    }
}

function isNextDay(depArr, arrArr) {
    const depDate = new Date(depArr[0], depArr[1] - 1, depArr[2]);
    const arrDate = new Date(arrArr[0], arrArr[1] - 1, arrArr[2]);

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
    const graphBtn = e.target.closest('button[data-action="toggle-graph"]');
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

    if (e.target.closest(".action-button") || e.target.closest("button")) {
        return;
    }

    const card = e.target.closest(".flight-card");
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
});