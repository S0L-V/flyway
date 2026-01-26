// seat-select.js (좌석 렌더링 전용)

document.addEventListener("DOMContentLoaded", () => {
    const seatGrid = document.getElementById("seat-grid");
    if (!seatGrid) return;

    // JSP에서 주입받는 값 (없으면 기본값)
    const ctx = seatGrid.dataset.ctx ?? "";
    const reservationId = seatGrid.dataset.rid ?? "R001";
    const segmentId = seatGrid.dataset.sid ?? "S001";

    fetchSeatMap(ctx, reservationId, segmentId);
});

function fetchSeatMap(ctx, reservationId, segmentId) {
    const seatGrid = document.getElementById("seat-grid");

    fetch(`${ctx}/api/public/reservations/${reservationId}/segments/${segmentId}/seats`)
        .then((res) => {
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            return res.json();
        })
        .then((data) => {
            renderSeatGrid(data?.data ?? []);
        })
        .catch((err) => {
            console.error("좌석 조회 실패", err);
            seatGrid.innerHTML = `<p class="seat-grid__loading">좌석 조회 실패: ${err.message}</p>`;
        });
}

function renderSeatGrid(seats) {
    const seatGrid = document.getElementById("seat-grid");
    seatGrid.innerHTML = "";

    if (!seats || seats.length === 0) {
        seatGrid.innerHTML = `<p class="seat-grid__loading">좌석 정보가 없습니다.</p>`;
        return;
    }

    // row 기준 그룹핑
    const rows = new Map();
    seats.forEach((seat) => {
        const rowNo = seat.rowNo;
        if (!rows.has(rowNo)) rows.set(rowNo, []);
        rows.get(rowNo).push(seat);
    });

    // row 오름차순
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

            // 같은 row 안에서 좌석 정렬 (A,B,C... 기준)
            const rowSeats = rows.get(rowNo);
            rowSeats.sort((s1, s2) => String(s1.colNo).localeCompare(String(s2.colNo)));

            rowSeats.forEach((seat) => {
                const seatBtn = document.createElement("button");
                seatBtn.className = "seat-item";
                seatBtn.type = "button";
                seatBtn.textContent = seat.seatNo;

                // 상태별 클래스
                if (seat.seatStatus === "AVAILABLE") {
                    seatBtn.classList.add("seat-item--available");
                } else if (seat.seatStatus === "HELD") {
                    seatBtn.classList.add("seat-item--hold");
                    seatBtn.disabled = true;
                } else {
                    seatBtn.classList.add("seat-item--unavailable");
                    seatBtn.disabled = true;
                }

                seatArea.appendChild(seatBtn);
            });

            rowDiv.appendChild(seatArea);
            seatGrid.appendChild(rowDiv);
        });
}
