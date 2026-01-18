document.addEventListener("DOMContentLoaded", () => {
    // loadFlights();
});

function loadFlights() {
    const url = `${CONTEXT_PATH}/api/flights`;

    fetch(url, { headers: { "Accept": "application/json" } })
        .then(async (res) => {
            const ct = res.headers.get("content-type");
            const text = await res.text();

            console.log("[flights fetch]", {
                url,
                status: res.status,
                contentType: ct,
                preview: text.slice(0, 200),
            });

            // JSON이 아닐 경우 여기서 바로 걸림
            if (!ct || !ct.includes("application/json")) {
                throw new Error("JSON이 아닌 응답이 내려옴 (HTML/리다이렉트/에러페이지 가능)");
            }

            return JSON.parse(text);
        })
        .then((list) => {
            console.log("[flights data sample]", list?.[0]);
            if (!Array.isArray(list)) {
                console.error("응답이 배열이 아님:", list);
                return;
            }
            renderFlights(list);
        })
        .catch((err) => console.error(err));
}

function renderSegment(f) {
    const flightNumber = f.flightNumber ?? f.flight_number ?? "-";
    const depAirport = f.departureAirport ?? f.departure_airport ?? "-";
    const arrAirport = f.arrivalAirport ?? f.arrival_airport ?? "-";
    const seatCount = f.seatCount ?? "-";
    const depTime = formatTimeArray(f.departureTime ?? f.departure_time);
    const arrTime = formatTimeArray(f.arrivalTime ?? f.arrival_time);

    return `
      <div class="flight-segment">
        <div class="flight-info">
          <span class="flight-number">${flightNumber}</span>
        </div>
        <div class="flight-details">
          <div class="flight-time">
            <div class="time-info">
              <div class="time">${depTime}</div>
              <div class="airport">${depAirport}</div>
            </div>
            <div class="duration-info">
              <span class="duration-badge">직항</span>
              <div class="duration-time">2시간 40분</div>
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
function renderFooter(option) {
    return `
      <div class="flight-footer">
        <div class="flight-actions">
          <button class="action-button">가격 변동 그래프</button>
          <button class="action-button">여정 상세</button>
        </div>
        <div class="seats-remaining">9석 남음</div>
        <div class="flight-price">
          <span class="price">411,700원</span>
          <img src="${CONTEXT_PATH}/resources/search/img/arrow-right.svg" alt="" class="price-arrow" />
        </div>
      </div>
    `;
}

function createOneWayCard(option) {
    const f = option.outbound;

    return `
    <article class="flight-card" data-out-id="${option.outbound.flightId}">
      ${renderSegment(f)}
      ${renderFooter(option)}
    </article>
  `;
}

function createRoundTripCard(option) {
    const o = option.outbound;
    const i = option.inbound;

    return `
    <article class="flight-card"
      data-out-id="${o.flightId}"
      data-in-id="${i.flightId}"
    >
      ${renderSegment(o)}
      ${renderSegment(i)}
      ${renderFooter(option)}
    </article>
  `;
}
// 기본 리스트 search 화면 들어왔을 때 보여줄거면 넣으면 됨
// function renderFlights(list) {
//     const container = document.querySelector(".flights-list");
//     if (!container) return;
//
//     container.innerHTML = list.map(createFlightCard).join("");
// }

function renderOneWay(options) {
    const el = document.getElementById("resultList");
    if (!el) return;

    if (!options || options.length === 0) {
        el.innerHTML = `<div class="empty">검색 결과가 없습니다.</div>`;
        return;
    }

    el.innerHTML = options
        .map(option => createOneWayCard(option))
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
        .map(option => createRoundTripCard(option))
        .join("");
}


function handleSearchResult(data) {
    const options = data.options ?? [];

    if (state.tripType === "OW") {
        renderOneWay(options);
    } else {
        renderRoundTrip(options);
    }
}



// departureTime: [2026,2,1,8,45] → "08:45"
function formatTimeArray(arr) {
    if (!Array.isArray(arr) || arr.length < 5) return "-";
    const hh = String(arr[3]).padStart(2, "0");
    const mm = String(arr[4]).padStart(2, "0");
    return `${hh}:${mm}`;
}
