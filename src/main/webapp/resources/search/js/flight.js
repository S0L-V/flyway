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
    return `
      <div class="flight-footer">
        <div class="flight-actions">
          <button class="action-button">가격 변동 그래프</button>
          <button class="action-button" onclick="openDetailPage(${index})">여정 상세</button>
        </div>
        <div class="seats-remaining">${seatCount}석 남음</div>
        <div class="flight-price" tabindex="0">
          <span class="price">${totalPrice}원</span>
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
    </article>
  `;
}

function createRoundTripCard(option, index) {
    const o = option.outbound;
    const i = option.inbound;

    return `
    <article class="flight-card"
      data-out-id="${o.flightId}"
      data-in-id="${i.flightId}"
    >
      ${renderSegment(o)}
      ${renderSegment(i)}
      ${renderFooter(option, index)}
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

    displayedOptions = options;

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