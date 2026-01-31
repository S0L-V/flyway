let detailRequestSeq = 0;

async function openDetailPage(index) {
    const flight = displayedOptions[index];

    if (!flight) {
        console.error("비행편 정보를 찾을 수 없습니다 index:", index);
        return;
    }

    const modal = document.getElementById('flightDetailModal');

    const flightsRow = modal.querySelector('.flights-row');

    // 수하물 정보 없이 기본 카드만 먼저
    let cardsHtml = "";
    cardsHtml += createDetail(flight.outbound, {}, "가는 편");
    if (flight.inbound) {
        cardsHtml += createDetail(flight.inbound, {}, "오는 편");
    }
    flightsRow.innerHTML = cardsHtml;

    modal.hidden = false;

    const routeType = flight.outbound.routeType;
    const cabinClass = state.cabin;

    const requestSeq = ++detailRequestSeq;
    try {
        const res = await fetch(`${CONTEXT_PATH}/api/public/flights/details?cabinClass=${cabinClass}&routeType=${routeType}`);

        if (res.ok) {
            const details = await res.json();
            if (requestSeq !== detailRequestSeq) return; // stale response

            let updatedHtml = "";
            updatedHtml += createDetail(flight.outbound, details, "가는 편");
            if (flight.inbound) {
                updatedHtml += createDetail(flight.inbound, details, "오는 편");
            }
            flightsRow.innerHTML = updatedHtml;
        }
    } catch (err) {
        console.error("규정 조회 실패:", err);
    }
}

function closeDetailPage() {
    document.getElementById('flightDetailModal').hidden = true;
}

function createDetail(f, details, type) {
    const segmentKey = type === "가는 편" ? "outbound" : "inbound";
    const flightNumber = f.flightNumber ?? "-";
    const depAirport = f.departureAirport ?? "-";
    const arrAirport = f.arrivalAirport ?? "-";
    const depCity = f.departureCity ?? "-";
    const arrCity = f.arrivalCity ?? "-";
    const terminalNo = f.terminalNo ?? "-";
    const depTime = formatTime(f.departureTime);
    const arrTime = formatTime(f.arrivalTime);
    const depDate = formatDate(f.departureTime);
    const arrDate = formatDate(f.arrivalTime);
    const durationMinutes = Number.isFinite(f.durationMinutes) ? f.durationMinutes : null;

    const freeCheckedBags = details.freeCheckedBags ?? "-";
    const freeCheckedWeights = details.freeCheckedWeights ?? "-";

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
        <div class="flight-card" id="dynamic-${segmentKey}">
            <div class="badge-label">${type}</div>
            <div class="flight-airline" id="modal-${segmentKey}-airline">${airlineLogoHtml}${airlineName} ${flightNumber}</div> 
            <div class="flight-route">
                <div class="flight-point">
                    <div class="flight-time">
                        <span id="modal-${segmentKey}-start-date" class="date-highlight">${depDate}</span>
                        <span id="modal-${segmentKey}-start-time" class="time-highlight">${depTime}</span>
                    </div>
                    <div class="flight-airport" id="modal-${segmentKey}-start-airport">${depCity}(${depAirport}) ${terminalNo}</div>
                </div>
    
                <div class="flight-duration">
                    <span class="flight-direct-badge">직항</span>
                    <div class="flight-duration-text" id="modal-${segmentKey}-duration">${time}</div>
                </div>
    
                <div class="flight-point">
                    <div class="flight-time">
                        <span id="modal-${segmentKey}-end-date" class="date-highlight">${arrDate}</span>
                        <span id="modal-${segmentKey}-end-time" class="time-highlight">${arrTime}</span>
                    </div>
                    <div class="flight-airport" id="modal-${segmentKey}-end-airport">${arrCity}(${arrAirport})</div>
                </div>
            </div>

            <div class="flight-separator"></div>

            <div class="flight-baggage">
                <div class="baggage-title">수하물</div>
                <div class="baggage-details">무료 위탁수하물 개수: ${freeCheckedBags}개<br>무료 위탁수하물 무게: ${freeCheckedWeights}kg</div>
            </div>
        </div>
    `
}

function formatTime(dateTime) {
    if (!dateTime) return "-";

    const date = new Date(dateTime);
    const hh = String(date.getHours()).padStart(2, "0");
    const mm = String(date.getMinutes()).padStart(2, "0");

    return `${hh}:${mm}`;
}

function formatDate(dateTime) {
    if (!dateTime) return "-";

    const date = new Date(dateTime);
    const year = String(date.getFullYear());
    const month = String(date.getMonth()).padStart(2, "0");
    const day = String(date.getDate()).padStart(2,"0");
    return `${year}-${month}-${day}`;
}