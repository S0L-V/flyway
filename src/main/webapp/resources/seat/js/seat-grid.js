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

    function buildSeatButton(seat, activePassengerId) {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "seat-item";
        btn.textContent = seat.seatNo;
        btn.dataset.seatNo = seat.seatNo;

        const status = String(seat.seatStatus || "").toUpperCase();
        const holderPid = seat.passengerId ? String(seat.passengerId) : "";

        if (status === "AVAILABLE") {
            btn.classList.add("seat-item--available");
            return btn;
        }

        if (status === "HOLD") {
            // 활성 승객이 잡은 HOLD만 주황색 + 클릭 가능(해제/변경)
            if (holderPid && activePassengerId && holderPid === String(activePassengerId)) {
                btn.classList.add("seat-item--hold");
                return btn;
            }

            // 그 외 HOLD(다른 승객/다른 사용자) 는 회색 + 클릭 불가
            btn.classList.add("seat-item--unavailable");
            btn.disabled = true;
            return btn;
        }

        // BOOKED 등 나머지
        btn.classList.add("seat-item--unavailable");
        btn.disabled = true;
        return btn;
    }

    function buildEmptySeatCell() {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "seat-item seat-item--empty";
        btn.disabled = true;
        btn.setAttribute("disabled", "true");
        btn.tabIndex = -1;
        return btn;
    }

    function calcSectionByIndex(idx, total) {
        if (total <= 10) return "front";

        const cut1 = Math.ceil(total / 3);
        const cut2 = Math.ceil((total * 2) / 3);

        if (idx < cut1) return "front";
        if (idx < cut2) return "mid";
        return "rear";
    }

    function renderSeatGrid(seatGridEl, seats, activePassengerId, cabinClassCode) {

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

            // colNo -> seat 매핑 (없는 컬럼은 빈칸으로 처리)
            const seatByCol = new Map();
            list.forEach((s) => {
                const col = String(s.colNo || "").toUpperCase();
                if (col) seatByCol.set(col, s);
            });

            // A,B,C 3칸 렌더 (없으면 empty cell)
            LEFT_COLS.forEach((col) => {
                const seat = seatByCol.get(col);
                const cell = seat ? buildSeatButton(seat, activePassengerId) : buildEmptySeatCell();
                leftWrap.appendChild(cell);
            });

            // D,E,F 3칸 렌더 (없으면 empty cell)
            RIGHT_COLS.forEach((col) => {
                const seat = seatByCol.get(col);
                const cell = seat ? buildSeatButton(seat, activePassengerId) : buildEmptySeatCell();
                rightWrap.appendChild(cell);
            });

            rowDiv.appendChild(leftWrap);
            rowDiv.appendChild(mid);
            rowDiv.appendChild(rightWrap);
            seatGridEl.appendChild(rowDiv);
        });

        // 실제 존재하는 섹션(front/mid/rear) 기록
        const present = new Set();
        seatGridEl.querySelectorAll(".seat-row[data-section]").forEach((el) => {
            if (el.dataset.section) present.add(el.dataset.section);
        });

        // row가 0이거나 이상할 때 최소 front 보장
        if (!present.size) present.add("front");

        seatGridEl.dataset.presentSections = Array.from(present).join(",");
    }

    function escapeHtml(s) {
        return String(s ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
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

    // =========================
    // 개별 좌석 UI 업데이트 (전체 렌더링 없이)
    // =========================
    function updateSeatUI(seatGridEl, seatNo, status) {
        if (!seatGridEl || !seatNo) return;

        const btn = seatGridEl.querySelector(`button.seat-item[data-seat-no="${seatNo}"]`);
        if (!btn) return;

        // 기존 상태 클래스 제거
        btn.classList.remove(
            "seat-item--available",
            "seat-item--hold",
            "seat-item--selected",
            "seat-item--unavailable"
        );
        btn.disabled = false;

        // 클릭 애니메이션 효과
        btn.style.transform = "scale(0.9)";
        requestAnimationFrame(() => {
            btn.style.transition = "transform 0.15s ease";
            btn.style.transform = "";
        });

        // 새 상태 적용
        if (status === "HOLD" || status === "SELECTED") {
            btn.classList.add("seat-item--hold");
        } else if (status === "AVAILABLE") {
            btn.classList.add("seat-item--available");
        } else {
            btn.classList.add("seat-item--unavailable");
            btn.disabled = true;
        }
    }

    global.SeatGrid = {
        renderSeatGrid,
        renderSelectedSummary,
        renderSeatAction,
        updateSeatUI
    };
})(window);