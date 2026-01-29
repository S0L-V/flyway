document.addEventListener("DOMContentLoaded", () => {
    const seatGridEl = document.getElementById("seat-grid");
    const summaryEl = document.getElementById("selected-summary");
    const actionEl = document.getElementById("seat-action");

    const termsToggle = document.getElementById("terms-toggle");
    const termsContent = document.getElementById("terms-content");
    const termsCheckbox = document.getElementById("terms-checkbox");

    const btnConfirm = document.getElementById("btn-confirm");
    const btnCancel = document.getElementById("btn-cancel");

    // 좌석 페이지가 아니면 종료
    if (!seatGridEl) return;

    const ctx = seatGridEl.dataset.ctx || "";
    const reservationId = seatGridEl.dataset.rid;
    const segmentId = seatGridEl.dataset.sid;

    // JSP의 data-pid는 기본 승객 1명
    const defaultPassengerId = String(seatGridEl.dataset.pid || "");

    const cabinClassCode = seatGridEl.dataset.cabin || null;

    // 다인원: 승객별 선택 좌석 저장 { passengerId: seatNo }
    const selectedSeatsByPassenger = {};

    // 승객 목록/이름 seat-main.jspf의 #passenger-source에서 읽음
    const passengers = []; // [{ passengerId, name }]
    const passengerNameById = {}; // { passengerId: "홍길동" }

    document.querySelectorAll("#passenger-source .passenger-source").forEach((el) => {
        const pid = String(el.dataset.passengerId || "");
        const name = (el.dataset.passengerName || "").trim();
        if (!pid) return;

        passengers.push({ passengerId: pid, name });
        passengerNameById[pid] = name;
    });

    // activePassengerId
    let activePassengerId =
        (passengers[0] && passengers[0].passengerId) || defaultPassengerId;

    // =========================
    // 약관/완료 버튼 로직
    // =========================
    function syncConfirmButtonState() {
        if (!btnConfirm) return;

        // 약관 체크박스가 화면에 없으면 동의로 취급
        const agreed = termsCheckbox ? !!termsCheckbox.checked : true;

        // 한 명이라도 좌석 선택되어 있으면 완료 가능
        const hasSeat = Object.keys(selectedSeatsByPassenger).length > 0;

        btnConfirm.disabled = !(agreed && hasSeat);
    }
    syncConfirmButtonState();

    termsCheckbox?.addEventListener("change", syncConfirmButtonState);

    if (termsToggle && termsContent) {
        termsToggle.addEventListener("click", (e) => {
            e.preventDefault();
            e.stopPropagation();

            const willExpand = termsContent.hidden;
            termsContent.hidden = !willExpand;

            const wrapper = termsToggle.closest(".seat-terms__header-wrapper");
            wrapper?.classList.toggle("seat-terms__header--expanded", willExpand);
            termsToggle.setAttribute("aria-expanded", String(willExpand));
        });
    }

    // =========================
    // 현재 선택된 구간 정보(우측 카드 헤더에 보여줄 값)
    // - segment-tab--active에서 읽어옴
    // =========================
    function getActiveSegmentInfo() {
        const activeSegBtn = document.querySelector("#segment-tabs .segment-tab--active");
        if (!activeSegBtn) return { routeText: "", dateTimeText: "" };

        const dep = activeSegBtn.dataset.dep || "";
        const arr = activeSegBtn.dataset.arr || "";
        const depTimeText = activeSegBtn.dataset.deptime || "";

        return {
            routeText: dep && arr ? `${dep} → ${arr}` : (activeSegBtn.querySelector(".segment-tab__route")?.textContent || "").trim(),
            dateTimeText: depTimeText || (activeSegBtn.querySelector(".segment-tab__datetime")?.textContent || "").trim(),
        };
    }

    // =========================
    // 렌더/갱신
    // =========================
    let isHolding = false;

    async function refreshAndRender() {
        const seats = await SeatAPI.fetchSeatMap(ctx, reservationId, segmentId);

        // 현재 승객의 선택 좌석만 selected로 표시
        const activeSeatNo = selectedSeatsByPassenger[activePassengerId] || null;

        SeatGrid.renderSeatGrid(seatGridEl, seats, activeSeatNo, cabinClassCode);

        // 현재 구간 정보(노선/시간)
        const segInfo = getActiveSegmentInfo();

        // =========================
        // 좌석 선택 내역 카드 렌더(승객 리스트)
        // =========================
        SeatGrid.renderSelectedSummary(summaryEl, {
            passengers,
            activePassengerId,
            selectedSeatsByPassenger,
            segment: segInfo,
            seatPrice: 0,
        });

        // =========================
        // 액션 카드 렌더(버튼/가격)
        // =========================
        SeatGrid.renderSeatAction(actionEl, {
            passengers,
            activePassengerId,
            selectedSeatsByPassenger,
            segment: segInfo,
            seatPrice: 0,
        });

        // 총액(지금은 0원 고정이지만 구조는 합산으로)
        const total = Object.keys(selectedSeatsByPassenger).length * 0;
        const totalEl = document.querySelector(".selection-total__amount");
        if (totalEl) totalEl.textContent = `₩${total.toLocaleString("ko-KR")}`;

        syncConfirmButtonState();
    }


    refreshAndRender().catch(console.error);

    // =========================
    // 우측 카드에서 좌석 선택 버튼 클릭 → activePassengerId 변경
    // =========================
    summaryEl?.addEventListener("click", async (e) => {
        const btn = e.target.closest("button.js-seat-select");
        if (!btn) return;

        const pid = String(btn.dataset.passengerId || "");
        if (!pid) return;

        activePassengerId = pid;
        await refreshAndRender().catch(() => {});
    });

    // =========================
    // 좌석 클릭 → HOLD/RELEASE (activePassengerId 기준)
    // =========================
    seatGridEl.addEventListener("click", async (e) => {
        const btn = e.target.closest("button.seat-item");
        if (!btn || isHolding) return;

        const seatNo = btn.dataset.seatNo;
        if (!seatNo) return;

        const currentSeatNo = selectedSeatsByPassenger[activePassengerId] || null;

        // 같은 화면 내 중복 선택 방지(다른 승객이 이미 선택한 좌석이면 막기)
        const occupiedByOther = Object.entries(selectedSeatsByPassenger).some(
            ([pid, s]) => pid !== String(activePassengerId) && s === seatNo
        );
        if (occupiedByOther) {
            alert("다른 승객이 이미 선택한 좌석입니다.");
            return;
        }

        // 비활성 좌석 클릭은 무시(현재 선택 좌석은 해제 허용)
        if (btn.disabled && !(currentSeatNo && seatNo === currentSeatNo)) return;

        try {
            isHolding = true;

            // 같은 좌석 재클릭 → 해제
            if (currentSeatNo === seatNo) {
                await SeatAPI.releaseHold(ctx, reservationId, segmentId, activePassengerId);
                delete selectedSeatsByPassenger[activePassengerId];
            } else {
                // 기존 좌석이 있으면 먼저 해제
                if (currentSeatNo) {
                    await SeatAPI.releaseHold(ctx, reservationId, segmentId, activePassengerId);
                }

                // 새 좌석 HOLD
                await SeatAPI.holdSeat(ctx, reservationId, segmentId, {
                    passengerId: activePassengerId,
                    seatNo,
                });

                selectedSeatsByPassenger[activePassengerId] = seatNo;
            }

            await refreshAndRender();
        } catch (err) {
            alert(`좌석 처리 실패: ${err.message}`);
            await refreshAndRender().catch(() => {});
        } finally {
            isHolding = false;
        }
    });

    // =========================
    // 취소 - 현재 HOLD 전부 해제 후 닫기/뒤로가기
    // =========================
    btnCancel?.addEventListener("click", async () => {
        const pids = Object.keys(selectedSeatsByPassenger);

        for (const pid of pids) {
            try {
                await SeatAPI.releaseHold(ctx, reservationId, segmentId, pid);
            } catch (e) {}
        }

        window.opener ? window.close() : history.back();
    });

    // =========================
    // 미니맵 zone 전환 (front/mid/rear) - 기존 유지
    // =========================
    const minimapEl = document.getElementById("seat-minimap");
    const zoneButtons = minimapEl ? minimapEl.querySelectorAll(".minimap__zone") : [];

    function setZone(zone) {
        seatGridEl.classList.remove("seat-grid--front", "seat-grid--mid", "seat-grid--rear");

        if (zone === "front") seatGridEl.classList.add("seat-grid--front");
        else if (zone === "mid") seatGridEl.classList.add("seat-grid--mid");
        else if (zone === "rear") seatGridEl.classList.add("seat-grid--rear");

        zoneButtons.forEach((b) => b.classList.toggle("is-active", b.dataset.zone === zone));

        seatGridEl.scrollIntoView({ block: "start", behavior: "smooth" });
    }

    if (zoneButtons.length) setZone("front");

    zoneButtons.forEach((btn) => {
        btn.addEventListener("click", () => {
            const zone = btn.dataset.zone;
            if (!zone) return;
            setZone(zone);
        });
    });

    actionEl?.addEventListener("click", async (e) => {
        const btn = e.target.closest("button.js-seat-action");
        if (!btn) return;

        const pid = String(btn.dataset.passengerId || "");
        if (!pid) return;

        // 버튼 누르면 해당 승객을 활성화(버튼 강조/선택 흐름)
        activePassengerId = pid;

        await refreshAndRender().catch(() => {});
    });

});