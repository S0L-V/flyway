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

            // 서버에서 이미 AVAILABLE로 바뀐 좌석은 로컬 선택에서도 제거
            const seatStatusBySeatNo = new Map();
            (seats || []).forEach((s) => {
                const seatNo = String(s.seatNo || "");
                const st = String(s.seatStatus || "").toUpperCase();
                if (seatNo) seatStatusBySeatNo.set(seatNo, st);
            });

            const currentSegmentSeats = state.selectedSeatsBySegment[segId] || {};
            Object.entries(currentSegmentSeats).forEach(([pid, seatNo]) => {
                const st = seatStatusBySeatNo.get(String(seatNo)) || "AVAILABLE";
                // 서버가 AVAILABLE로 보여주면(만료/해제) 로컬도 제거
                if (st === "AVAILABLE") {
                    delete state.selectedSeatsBySegment[segId][pid];
                }
            });

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
