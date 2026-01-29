(function (global) {
    const LEFT_COLS = ["A", "B", "C"];
    const RIGHT_COLS = ["D", "E", "F"];

    function renderColumnHeader(seatGridEl) {
        const headerRow = document.createElement("div");
        headerRow.className = "seat-row seat-row--header";

        const left = document.createElement("div");
        left.className = "seat-row__cols seat-row__cols--left";
        LEFT_COLS.forEach((c) => {
            const s = document.createElement("div");
            s.className = "seat-col";
            s.textContent = c;
            left.appendChild(s);
        });

        const mid = document.createElement("div");
        mid.className = "seat-row__number seat-row__number--mid";
        mid.textContent = "";

        const right = document.createElement("div");
        right.className = "seat-row__cols seat-row__cols--right";
        RIGHT_COLS.forEach((c) => {
            const s = document.createElement("div");
            s.className = "seat-col";
            s.textContent = c;
            right.appendChild(s);
        });

        headerRow.appendChild(left);
        headerRow.appendChild(mid);
        headerRow.appendChild(right);

        seatGridEl.appendChild(headerRow);
    }

    function buildSeatButton(seat, selectedSeatNo) {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "seat-item";
        btn.textContent = seat.seatNo;
        btn.dataset.seatNo = seat.seatNo;

        // 상태별 클래스
        if (seat.seatStatus === "AVAILABLE") btn.classList.add("seat-item--available");
        else if (seat.seatStatus === "HOLD") btn.classList.add("seat-item--hold");
        else {
            btn.classList.add("seat-item--unavailable");
            btn.disabled = true;
        }

        // 현재 선택중(활성 승객의 선택 좌석만 selected로 표시)
        if (selectedSeatNo === seat.seatNo) btn.classList.add("seat-item--selected");
        return btn;
    }

    function calcSectionByIndex(idx, total) {
        if (total <= 0) return "front";

        const cut1 = Math.ceil(total / 3);
        const cut2 = Math.ceil((total * 2) / 3);

        if (idx < cut1) return "front";
        if (idx < cut2) return "mid";
        return "rear";
    }

    function renderSeatGrid(seatGridEl, seats, selectedSeatNo, cabinClassCode) {
        if (!seatGridEl) return;

        seatGridEl.innerHTML = "";

        if (!seats || !seats.length) {
            seatGridEl.innerHTML = `<p class="seat-grid__loading">좌석 정보가 없습니다.</p>`;
            return;
        }

        // cabinClass 필터
        const filtered = cabinClassCode
            ? seats.filter(
                (s) => String(s.cabinClassCode).toUpperCase() === String(cabinClassCode).toUpperCase()
            )
            : seats;

        // row 단위로 그룹핑
        const rows = new Map();
        filtered.forEach((s) => {
            if (!rows.has(s.rowNo)) rows.set(s.rowNo, []);
            rows.get(s.rowNo).push(s);
        });

        const sortedRowNos = [...rows.keys()].sort((a, b) => Number(a) - Number(b));

        // 컬럼 헤더 1줄
        renderColumnHeader(seatGridEl);

        sortedRowNos.forEach((rowNo, idx) => {
            const rowDiv = document.createElement("div");
            rowDiv.className = "seat-row";
            rowDiv.dataset.section = calcSectionByIndex(idx, sortedRowNos.length);

            const leftWrap = document.createElement("div");
            leftWrap.className = "seat-row__seats seat-row__seats--left";

            const mid = document.createElement("div");
            mid.className = "seat-row__number seat-row__number--mid";
            mid.textContent = rowNo;

            const rightWrap = document.createElement("div");
            rightWrap.className = "seat-row__seats seat-row__seats--right";

            const list = rows
                .get(rowNo)
                .sort((a, b) => String(a.colNo).localeCompare(String(b.colNo)));

            list.forEach((seat) => {
                const col = String(seat.colNo).toUpperCase();
                const btn = buildSeatButton(seat, selectedSeatNo);

                if (LEFT_COLS.includes(col)) leftWrap.appendChild(btn);
                else rightWrap.appendChild(btn);
            });

            rowDiv.appendChild(leftWrap);
            rowDiv.appendChild(mid);
            rowDiv.appendChild(rightWrap);
            seatGridEl.appendChild(rowDiv);
        });
    }

    function escapeHtml(s) {
        return String(s ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function formatMoneyKRW(amount) {
        const n = Number(amount || 0);
        return `₩${n.toLocaleString("ko-KR")}`;
    }

    // 좌석 선택 내역 카드 렌더
    function renderSelectedSummary(boxEl, opts) {
        if (!boxEl) return;

        const passengers = Array.isArray(opts?.passengers) ? opts.passengers : [];
        const activePassengerId = String(opts?.activePassengerId || "");
        const selectedSeatsByPassenger =
            opts?.selectedSeatsByPassenger && typeof opts.selectedSeatsByPassenger === "object"
                ? opts.selectedSeatsByPassenger
                : {};
        const seatPrice = Number(opts?.seatPrice || 0);

        const routeText = (opts?.segment?.routeText || "").trim();
        const dateTimeText = (opts?.segment?.dateTimeText || "").trim();

        // 승객이 없으면 빈 UI
        if (!passengers.length) {
            boxEl.innerHTML = `
            <div class="selected-summary__empty">
                <p>승객 정보가 없습니다.</p>
            </div>
        `;
            return;
        }

        // 승객별 라인: 좌석 있으면 좌석번호 + 가격 + 좌석 변경 버튼
        // 좌석 없으면 좌석 선택 버튼
        const rowsHtml = passengers
            .map((p) => {
                const pid = String(p.passengerId);
                const name = (p.name || "").trim() || `승객 ${pid.slice(0, 8)}`;

                const seatNo = selectedSeatsByPassenger[pid] || "";
                const isActive = pid === activePassengerId;

                const btnLabel = seatNo ? "좌석 변경" : "좌석 선택";

                return `
            <div class="seat-summary-row ${isActive ? "is-active" : ""}">
                <div class="seat-summary-row__left">
                    <div class="seat-summary-row__name">${escapeHtml(name)}</div>
                    <div class="seat-summary-row__seat ${seatNo ? "" : "is-empty"}">${seatNo ? escapeHtml(seatNo) : ""}</div>
                </div>

                <div class="seat-summary-row__right">
                    <div class="seat-summary-row__price ${seatNo ? "" : "is-hidden"}">
                        ${formatMoneyKRW(seatPrice)}
                    </div>
                </div>
            </div>
        `;
            })
            .join("");

        boxEl.innerHTML = `
        <div class="seat-summary-card">
            <div class="seat-summary-card__header">
                <div class="seat-summary-card__route">${escapeHtml(routeText)}</div>
                <div class="seat-summary-card__datetime">${escapeHtml(dateTimeText)}</div>
            </div>

            <div class="seat-summary-card__divider"></div>

            <div class="seat-summary-card__body">
                ${rowsHtml}
            </div>
        </div>
    `;
    }

    // =========================
    // 액션 카드 렌더(승객별 버튼/가격)
    // =========================
    function renderSeatAction(actionEl, opts) {
        if (!actionEl) return;

        const passengers = Array.isArray(opts?.passengers) ? opts.passengers : [];
        const activePassengerId = String(opts?.activePassengerId || "");
        const selectedSeatsByPassenger =
            opts?.selectedSeatsByPassenger && typeof opts.selectedSeatsByPassenger === "object"
                ? opts.selectedSeatsByPassenger
                : {};
        const seatPrice = Number(opts?.seatPrice || 0);

        // 승객이 없으면 아무것도 표시하지 않음
        if (!passengers.length) {
            actionEl.innerHTML = "";
            return;
        }

        const rowsHtml = passengers.map((p) => {
            const pid = String(p.passengerId || "");
            const name = (p.name || "").trim() || `승객 ${pid.slice(0, 8)}`;
            const seatNo = selectedSeatsByPassenger[pid] || "";
            const btnLabel = seatNo ? "좌석 변경" : "좌석 선택";
            const isActive = pid === activePassengerId;

            return `
            <div class="seat-action-row ${isActive ? "is-active" : ""}">
                <div class="seat-action-row__left">
                    <div class="seat-action-row__name">${escapeHtml(name)}</div>
                </div>

                <div class="seat-action-row__right">
                    <button type="button"
                            class="seat-action-row__btn js-seat-action ${isActive ? "is-active" : ""}"
                            data-passenger-id="${escapeHtml(pid)}">
                        ${btnLabel}
                    </button>
                </div>
            </div>
        `;
        }).join("");

        actionEl.innerHTML = `
        <div class="seat-action-card">
            ${rowsHtml}
        </div>
     `;
    }

    global.SeatGrid = { renderSeatGrid, renderSelectedSummary, renderSeatAction };
})(window);