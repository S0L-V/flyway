// document.addEventListener("DOMContentLoaded", () => {
//     // loadFlights();
// });
//
// function loadFlights() {
//     const url = `${CONTEXT_PATH}/api/flights`;
//
//     fetch(url, { headers: { "Accept": "application/json" } })
//         .then(async (res) => {
//             const ct = res.headers.get("content-type");
//             const text = await res.text();
//
//             console.log("[flights fetch]", {
//                 url,
//                 status: res.status,
//                 contentType: ct,
//                 preview: text.slice(0, 200),
//             });
//
//             // JSON이 아닐 경우 여기서 바로 걸림
//             if (!ct || !ct.includes("application/json")) {
//                 throw new Error("JSON이 아닌 응답이 내려옴 (HTML/리다이렉트/에러페이지 가능)");
//             }
//
//             return JSON.parse(text);
//         })
//         .then((list) => {
//             console.log("[flights data sample]", list?.[0]);
//             if (!Array.isArray(list)) {
//                 console.error("응답이 배열이 아님:", list);
//                 return;
//             }
//             renderFlights(list);
//         })
//         .catch((err) => console.error(err));
// }

function renderSegment(f) {
    const flightNumber = f.flightNumber ?? f.flight_number ?? "-";
    const depAirport = f.departureAirport ?? f.departure_airport ?? "-";
    const arrAirport = f.arrivalAirport ?? f.arrival_airport ?? "-";
    const seatCount = f.seatCount ?? "-";
    const depTime = formatTimeArray(f.departureTime ?? f.departure_time);
    const arrTime = formatTimeArray(f.arrivalTime ?? f.arrival_time);

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
    const seatCount = option.totalSeats ?? "-";
    return `
      <div class="flight-footer">
        <div class="flight-actions">
          <button class="action-button">가격 변동 그래프</button>
          <button class="action-button">여정 상세</button>
        </div>
        <div class="seats-remaining">${seatCount}석 남음</div>
        <div class="flight-price" tabindex="0">
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

function renderOneWay(options) {
    const el = document.getElementById("resultList");
    if (!el) return;

    if (!options || options.length === 0) {
        el.innerHTML = `<div class="empty">검색 결과가 없습니다.</div>`;
        return;
    }

    displayedOptions = options;

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

    displayedOptions = options;

    el.innerHTML = options
        .map(option => createRoundTripCard(option))
        .join("");
}


function renderByTripType(data) {
    // [수정] 들어온 data가 배열이면 그대로 쓰고, 객체면 .options를 꺼내 쓴다.
    const options = Array.isArray(data) ? data : (data.options ?? []);

    if (state.tripType === "OW") {
        renderOneWay(options);
    } else {
        renderRoundTrip(options);
    }
}

// document.addEventListener("click", (e) => {
//     const priceBtn = e.target.closest(".flight-price");
//     if (!priceBtn) return;
//
//     if (typeof isUserLoggedIn !== 'undefined' && !isUserLoggedIn) {
//         if (confirm("로그인이 필요한 서비스입니다")) {
//             location.href = `${CONTEXT_PATH}/login`;
//         }
//         return;
//     }
//
//     void toReservation(priceBtn);
// });
//
// async function toReservation(priceBtn) {
//     // id 검증 필요
//     const card = priceBtn.closest(".flight-card");
//     if (!card) return;
//
//     const outId = card.dataset.outId;
//     const inId  = card.dataset.inId || null;
//
//
//     const payload = {
//         tripType: state.tripType,
//         outFlightId: outId,
//         passengerCount: state.passengers,
//         cabinClassCode: state.cabin
//     };
//     if (state.tripType === "RT") {
//         payload.inFlightId = inId;
//     }
//     try {
//         // 3) 검색 API 호출 (POST)
//         const res = await fetch(`${CONTEXT_PATH}/api/public/reservation/prepare`, {
//             method: "POST",
//             headers: {
//                 "Content-Type": "application/json",
//                 "Accept": "application/json"
//             },
//             body: JSON.stringify(payload)
//         });
//
//         const json = await res.json();
//         const rid = json.rid;
//
//         console.log(json);
//
//         //location.href = `${CONTEXT_PATH}/reservations/agreement/${rid}`;
//
//         const targetUrl = `${CONTEXT_PATH}/reservations/agreement/${rid}`;
//
//         const formParams = {
//             rid: json.rid,
//             outFlightId: json.outFlightId,
//             inFlightId: json.inFlightId,       // 편도면 null일 수 있음 -> sendPost에서 처리됨
//             passengerCount: json.passengerCount,
//             cabinClassCode: json.cabinClassCode
//         };
//
//         // POST 방식으로 페이지 이동
//         sendPost(targetUrl, formParams);
//     } catch (err) {
//         console.error(err);
//         alert("네트워크 오류가 발생했습니다.");
//     }
// }


// departureTime: [2026,2,1,8,45] → "08:45"
function formatTimeArray(arr) {
    if (!Array.isArray(arr) || arr.length < 5) return "-";
    const hh = String(arr[3]).padStart(2, "0");
    const mm = String(arr[4]).padStart(2, "0");
    return `${hh}:${mm}`;
}


document.getElementById("resultList").addEventListener("click", (e) => {
    if (e.target.closest(".action-button") || e.target.closest("button")) {
        return;
    }

    const card = e.target.closest(".flight-card");
    if (!card) return;

    // hidden 폼에 값 설정
    document.getElementById("hiddenOutFlightId").value = card.dataset.outId;
    document.getElementById("hiddenInFlightId").value = card.dataset.inId || "";
    document.getElementById("hiddenPassengerCount").value = state.passengers;
    document.getElementById("hiddenCabinClassCode").value = state.cabin;

    // 폼 제출 → /reservations/draft → 동의 페이지로 redirect
    document.getElementById("reservationForm").submit();
});


// document.addEventListener("click", (e) => {
//     const card = e.target.closest(".flight-price");
//     if (!card) return;
//
//     if (typeof isUserLoggedIn !== 'undefined' && !isUserLoggedIn) {
//         if (confirm("로그인이 필요한 서비스입니다")) {
//             location.href = `${CONTEXT_PATH}/login`;
//         }
//         return;
//     }
//
//     // hidden 폼에 값 설정
//     document.getElementById("hiddenOutFlightId").value = card.dataset.outId;
//     document.getElementById("hiddenInFlightId").value = card.dataset.inId || "";
//     document.getElementById("hiddenPassengerCount").value = state.passengers;
//     document.getElementById("hiddenCabinClassCode").value = state.cabin;
//
//     // 폼 제출 → /reservations/draft → 동의 페이지로 redirect
//     document.getElementById("reservationForm").submit();
// });
// document.addEventListener("click", (e) => {
//     const priceBtn = e.target.closest(".flight-price");
//     if (!priceBtn) return;
//
//     if (typeof isUserLoggedIn !== 'undefined' && !isUserLoggedIn) {
//         if (confirm("로그인이 필요한 서비스입니다")) {
//             location.href = `${CONTEXT_PATH}/login`;
//         }
//         return;
//     }
//
//     void toReservation(priceBtn);
// });