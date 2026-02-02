window.SeatRenderer = (() => {
    function create(app, confirm) {
        const { dom, ctx, state } = app;

        function getActiveSegmentInfo() {
            const activeSegBtn = document.querySelector("#segment-tabs .segment-tab--active");
            if (!activeSegBtn) return { routeText: "", dateTimeText: "" };

            const dep = activeSegBtn.dataset.dep || "";
            const arr = activeSegBtn.dataset.arr || "";
            const depTimeText = activeSegBtn.dataset.deptime || "";

            return {
                routeText:
                    dep && arr
                        ? `${dep} → ${arr}`
                        : (activeSegBtn.querySelector(".segment-tab__route")?.textContent || "").trim(),
                dateTimeText:
                    depTimeText ||
                    (activeSegBtn.querySelector(".segment-tab__datetime")?.textContent || "").trim(),
            };
        }

        async function refreshAndRender() {
            const segId = state.activeSegmentId;
            const seats = await SeatAPI.fetchSeatMap(ctx.base, ctx.reservationId, segId);

            // 서버 HOLD -> 로컬 선택 좌석 동기화 (팝업 재진입/새로고침 대비)
            const myPassengerIds = new Set((state.passengers || []).map((p) => String(p.passengerId)));
            const serverSelected = {}; // { passengerId: seatNo }

            (seats || []).forEach((s) => {
                const st = String(s.seatStatus || "").toUpperCase();
                const pid = s.passengerId != null ? String(s.passengerId) : "";
                const seatNo = s.seatNo != null ? String(s.seatNo) : "";
                if (st === "HOLD" && pid && seatNo && myPassengerIds.has(pid)) {
                    serverSelected[pid] = seatNo;
                }
            });

            // 서버 기준으로 덮어쓰기 (불일치/유령 선택 방지)
            state.selectedSeatsBySegment[segId] = serverSelected;

            SeatGrid.renderSeatGrid(dom.seatGridEl, seats, state.activePassengerId, ctx.cabinClassCode);

            const segInfo = getActiveSegmentInfo();

            SeatGrid.renderSelectedSummary(dom.summaryEl, {
                passengers: state.passengers,
                activePassengerId: state.activePassengerId,
                selectedSeatsByPassenger: state.selectedSeatsBySegment[segId] || {},
                segment: segInfo,
            });

            SeatGrid.renderSeatAction(dom.actionEl, {
                passengers: state.passengers,
                activePassengerId: state.activePassengerId,
                selectedSeatsByPassenger: state.selectedSeatsBySegment[segId] || {},
                segment: segInfo,
            });

            confirm?.sync?.();
        }

        return { refreshAndRender };
    }

    return { create };
})();