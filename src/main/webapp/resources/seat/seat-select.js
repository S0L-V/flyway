// 좌석 선택 페이지 (좌석 맵 조회 + 렌더링 + HOLD API 연동)

// 응답을 JSON으로 읽고 HTTP 에러면 메시지 뽑아서 throw
async function safeJson(res) {
    const ct = res.headers.get("content-type") || "";
    const text = await res.text();

    if (!ct.includes("application/json")) {
        throw new Error(`JSON이 아닌 응답: ${res.status} ${res.statusText} / ${ct} / 미리보기: ${text.slice(0, 80)}`);
    }

    let body;
    try {
        body = text ? JSON.parse(text) : null;
    } catch (e) {
        throw new Error(`JSON 파싱 실패: ${res.status} ${res.statusText} / 미리보기: ${text.slice(0, 80)}`);
    }

    // HTTP 에러면 여기서 예외로 올려서 상위에서 catch 가능하게
    if (!res.ok) {
        const msg =
            body?.message ||
            body?.error ||
            body?.msg ||
            `HTTP ${res.status} ${res.statusText}`;
        throw new Error(msg);
    }

    return body;
}

document.addEventListener("DOMContentLoaded", () => {
    const seatGrid = document.getElementById("seat-grid");
    if (!seatGrid) {
        console.error("seat-grid element not found");
        return;
    }

    const ctx = seatGrid.dataset.ctx || "";
    const reservationId = seatGrid.dataset.rid;
    const segmentId = seatGrid.dataset.sid;

    // HOLD에 필요한 passengerId
    const passengerId = seatGrid.dataset.pid;

    const cabinClassCode = seatGrid.dataset.cabin || null;

    if (!reservationId || !segmentId) {
        console.error("Missing reservationId or segmentId", { reservationId, segmentId });
        seatGrid.innerHTML = `
      <p class="seat-grid__loading">
        좌석 정보를 불러올 수 없습니다.<br/>(예약 정보 또는 구간 정보 없음)
      </p>`;
        return;
    }

    // passengerId 없으면 HOLD 테스트 불가
    if (!passengerId) {
        console.error("Missing passengerId (data-pid)", { passengerId });
        seatGrid.innerHTML = `
      <p class="seat-grid__loading">
        좌석 HOLD를 위해 승객 정보가 필요합니다.<br/>(data-pid 누락)
      </p>`;
        return;
    }

    let selectedSeatNo = null;
    let isHolding = false; // 중복 클릭 방지

    // 최초 로딩
    (async () => {
        try {
            await refreshAndRender();
        } catch (err) {
            console.error("초기 좌석 로딩 실패", err);
            seatGrid.innerHTML = `
        <p class="seat-grid__loading">
          좌석 정보를 불러오지 못했습니다.<br/>
          (${escapeHtml(err.message)})
        </p>`;
        }
    })();

    // 좌석 클릭 → HOLD/RELEASE 연동
    seatGrid.addEventListener("click", async (e) => {
        const btn = e.target.closest("button.seat-item");
        if (!btn) return;

        const seatNo = btn.dataset.seatNo;
        if (!seatNo) return;

        // HOLD 좌석이라도 내가 선택한 좌석이면 해제 클릭 허용
        if (btn.disabled && !(selectedSeatNo && seatNo === selectedSeatNo)) return;

        if (isHolding) return;

        try {
            isHolding = true;

            // 같은 좌석 다시 누르면 HOLD 해제
            if (selectedSeatNo === seatNo) {
                await releaseHold(ctx, reservationId, segmentId, passengerId);
                selectedSeatNo = null;

                await refreshAndRender();
                renderSelectedSummary(null);
                return;
            }

            // 다른 좌석을 누르면 기존 HOLD 해제 후 새 HOLD
            if (selectedSeatNo) {
                await releaseHold(ctx, reservationId, segmentId, passengerId);
            }

            // HOLD 호출
            await holdSeat(ctx, reservationId, segmentId, {
                passengerId,
                seatNo,
            });

            selectedSeatNo = seatNo;

            await refreshAndRender();
            renderSelectedSummary(selectedSeatNo);
        } catch (err) {
            console.error("HOLD 처리 실패", err);
            alert(`좌석 처리 실패: ${err.message}`);
            try {
                await refreshAndRender();
            } catch (e2) {
                console.error("실패 후 재로딩도 실패", e2);
            }
        } finally {
            isHolding = false;
        }
    });

    // 좌석 목록 다시 받아서 렌더링하는 함수 (HOLD/RELEASE 후 서버 상태 반영)
    async function refreshAndRender() {
        const seats = await fetchSeatMap(ctx, reservationId, segmentId);

        const filtered = cabinClassCode
            ? seats.filter(
                (s) =>
                    String(s.cabinClassCode || "").toUpperCase() ===
                    String(cabinClassCode).toUpperCase()
            )
            : seats;

        renderSeatGrid(filtered, selectedSeatNo);
    }
});

// 좌석 맵 조회 content-type 체크 + safeJson 사용
function fetchSeatMap(ctx, reservationId, segmentId) {
    const url =
        `${ctx}/api/public/reservations/` +
        `${encodeURIComponent(reservationId)}` +
        `/segments/${encodeURIComponent(segmentId)}/seats`;

    return fetch(url)
        .then((res) => safeJson(res)) // safeJson에서 res.ok 체크 후 에러면 throw
        .then((data) => data?.data ?? []);
}

// HOLD API 호출
function holdSeat(ctx, reservationId, segmentId, body) {
    const url =
        `${ctx}/api/public/reservations/` +
        `${encodeURIComponent(reservationId)}` +
        `/segments/${encodeURIComponent(segmentId)}/seats/hold`;

    return fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Accept": "application/json",
        },
        body: JSON.stringify(body),
    })
        .then((res) => safeJson(res))
        .then((data) => data);
}

// RELEASE API 호출
function releaseHold(ctx, reservationId, segmentId, passengerId) {
    const url =
        `${ctx}/api/public/reservations/` +
        `${encodeURIComponent(reservationId)}` +
        `/segments/${encodeURIComponent(segmentId)}` +
        `/seats/hold/${encodeURIComponent(passengerId)}`;

    return fetch(url, {
        method: "DELETE",
        headers: { "Accept": "application/json" },
    })
        .then((res) => safeJson(res))
        .then((data) => data);
}

// 좌석 그리드 렌더링
function renderSeatGrid(seats, selectedSeatNo) {
    const seatGrid = document.getElementById("seat-grid");
    seatGrid.innerHTML = "";

    if (!seats || seats.length === 0) {
        seatGrid.innerHTML = `<p class="seat-grid__loading">좌석 정보가 없습니다.</p>`;
        return;
    }

    const rows = new Map();
    seats.forEach((seat) => {
        const rowNo = seat.rowNo ?? -1;
        if (!rows.has(rowNo)) rows.set(rowNo, []);
        rows.get(rowNo).push(seat);
    });

    [...rows.keys()]
        .sort((a, b) => Number(a) - Number(b))
        .forEach((rowNo) => {
            const rowDiv = document.createElement("div");
            rowDiv.className = "seat-row";

            const rowNumber = document.createElement("div");
            rowNumber.className = "seat-row__number";
            rowNumber.textContent = rowNo;
            rowDiv.appendChild(rowNumber);

            const seatArea = document.createElement("div");
            seatArea.className = "seat-row__seats";

            const rowSeats = rows.get(rowNo);
            rowSeats.sort((a, b) => String(a.colNo).localeCompare(String(b.colNo)));

            rowSeats.forEach((seat) => {
                const seatBtn = document.createElement("button");
                seatBtn.type = "button";
                seatBtn.className = "seat-item";
                seatBtn.textContent = seat.seatNo;
                seatBtn.dataset.seatNo = seat.seatNo;

                // 상태별 클래스/클릭 가능 여부 결정
                if (seat.seatStatus === "AVAILABLE") {
                    seatBtn.classList.add("seat-item--available");
                    seatBtn.disabled = false;
                } else if (seat.seatStatus === "HOLD") {
                    seatBtn.classList.add("seat-item--hold");

                    // HOLD라도 내가 선택한 좌석이면 해제 클릭 가능하게 열어둠
                    seatBtn.disabled = !(selectedSeatNo && seat.seatNo === selectedSeatNo);
                } else {
                    seatBtn.classList.add("seat-item--unavailable");
                    seatBtn.disabled = true;
                }

                // 내가 선택한 좌석 표시
                if (selectedSeatNo && seat.seatNo === selectedSeatNo) {
                    seatBtn.classList.add("seat-item--selected");
                }

                seatArea.appendChild(seatBtn);
            });

            rowDiv.appendChild(seatArea);
            seatGrid.appendChild(rowDiv);
        });
}

// 우측 요약 패널 렌더
function renderSelectedSummary(selectedSeatNo) {
    const box = document.getElementById("selected-summary");
    if (!box) return;

    if (!selectedSeatNo) {
        box.innerHTML = `
      <div class="selected-summary__empty">
        <p>선택된 좌석이 없습니다.</p>
      </div>`;
        return;
    }

    box.innerHTML = `
    <div class="selected-summary__item">
      <div class="selected-summary__label">선택 좌석</div>
      <div class="selected-summary__value">${escapeHtml(selectedSeatNo)}</div>
    </div>`;
}

function escapeHtml(str) {
    return String(str)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
