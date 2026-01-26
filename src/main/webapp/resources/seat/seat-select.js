// 좌석 선택 페이지에서 좌석 맵 조회 + 렌더링 담당

document.addEventListener("DOMContentLoaded", () => {
    // 좌석 그리드 컨테이너
    const seatGrid = document.getElementById("seat-grid");
    if (!seatGrid) {
        console.error("좌석 배치를 찾을 수 없습니다.");
        return;
    }

    // JSP에서 data-* 로 내려준 값 읽기
    const ctx = seatGrid.dataset.ctx || "";              // contextPath
    const reservationId = seatGrid.dataset.rid;          // reservationId
    const segmentId = seatGrid.dataset.sid;              // reservationSegmentId

    // 필수 값 검증
    if (!reservationId || !segmentId) {
        console.error("해당 예약이나 구간이 존재하지 않습니다.", {
            reservationId,
            segmentId
        });

        seatGrid.innerHTML =
            `<p class="seat-grid__loading">
                좌석 정보를 불러올 수 없습니다.<br/>
                (예약 정보 또는 구간 정보 없음)
             </p>`;
        return;
    }

    // 좌석 API 호출
    fetchSeatMap(ctx, reservationId, segmentId);
});

// 좌석 맵 조회 API 호출
function fetchSeatMap(ctx, reservationId, segmentId) {
    const seatGrid = document.getElementById("seat-grid");

    const url =
        `${ctx}/api/public/reservations/` +
        `${encodeURIComponent(reservationId)}` +
        `/segments/${encodeURIComponent(segmentId)}/seats`;

    fetch(url)
        .then((res) => {
            if (!res.ok) {
                throw new Error(`HTTP ${res.status}`);
            }
            return res.json();
        })
        .then((data) => {
            // 서버 응답 구조: { success, data }
            renderSeatGrid(data?.data ?? []);
        })
        .catch((err) => {
            console.error("좌석 조회 실패", err);
            seatGrid.innerHTML =
                `<p class="seat-grid__loading">
                    좌석 조회 실패: ${err.message}
                 </p>`;
        });
}

// 좌석 그리드 렌더링
function renderSeatGrid(seats) {
    const seatGrid = document.getElementById("seat-grid");
    seatGrid.innerHTML = "";

    if (!seats || seats.length === 0) {
        seatGrid.innerHTML =
            `<p class="seat-grid__loading">좌석 정보가 없습니다.</p>`;
        return;
    }

    // rowNo 기준으로 좌석 그룹핑
    const rows = new Map();
    seats.forEach((seat) => {
        const rowNo = seat.rowNo;
        if (!rows.has(rowNo)) {
            rows.set(rowNo, []);
        }
        rows.get(rowNo).push(seat);
    });

    // row 번호 오름차순 정렬
    [...rows.keys()]
        .sort((a, b) => Number(a) - Number(b))
        .forEach((rowNo) => {
            const rowDiv = document.createElement("div");
            rowDiv.className = "seat-row";

            // 행 번호
            const rowNumber = document.createElement("div");
            rowNumber.className = "seat-row__number";
            rowNumber.textContent = rowNo;
            rowDiv.appendChild(rowNumber);

            // 좌석 영역
            const seatArea = document.createElement("div");
            seatArea.className = "seat-row__seats";

            // 같은 행 내 좌석을 A, B, C… 순으로 정렬
            const rowSeats = rows.get(rowNo);
            rowSeats.sort((a, b) =>
                String(a.colNo).localeCompare(String(b.colNo))
            );

            rowSeats.forEach((seat) => {
                const seatBtn = document.createElement("button");
                seatBtn.type = "button";
                seatBtn.className = "seat-item";
                seatBtn.textContent = seat.seatNo;

                // 좌석 상태별 처리
                switch (seat.seatStatus) {
                    case "AVAILABLE":
                        seatBtn.classList.add("seat-item--available");
                        break;

                    case "HELD":
                        seatBtn.classList.add("seat-item--hold");
                        seatBtn.disabled = true;
                        break;

                    default:
                        seatBtn.classList.add("seat-item--unavailable");
                        seatBtn.disabled = true;
                        break;
                }

                seatArea.appendChild(seatBtn);
            });

            rowDiv.appendChild(seatArea);
            seatGrid.appendChild(rowDiv);
        });
}
