window.SeatState = (() => {
    function init() {
        const seatGridEl = document.getElementById("seat-grid");
        if (!seatGridEl) return null;

        const dom = {
            seatGridEl,
            summaryEl: document.getElementById("selected-summary"),
            actionEl: document.getElementById("seat-action"),
            segmentTabsEl: document.getElementById("segment-tabs"),

            termsToggle: document.getElementById("terms-toggle"),
            termsContent: document.getElementById("terms-content"),
            termsCheckbox: document.getElementById("terms-checkbox"),

            btnConfirm: document.getElementById("btn-confirm"),
            btnCancel: document.getElementById("btn-cancel"),

            minimapEl: document.getElementById("seat-minimap"),
        };

        const ctx = {
            base: seatGridEl.dataset.ctx || "",
            reservationId: seatGridEl.dataset.rid,
            segmentId: seatGridEl.dataset.sid,
            defaultPassengerId: String(seatGridEl.dataset.pid || ""),
            cabinClassCode: seatGridEl.dataset.cabin || null,
        };

        // 세그먼트 목록 수집
        const segments = [];
        document.querySelectorAll("#segment-tabs .segment-tab").forEach((tab) => {
            segments.push({
                segmentId: tab.dataset.segmentId,
                dep: tab.dataset.dep,
                arr: tab.dataset.arr,
                deptime: tab.dataset.deptime,
            });
        });

        const state = {
            isHolding: false,
            activePassengerId: "",
            activeSegmentId: ctx.segmentId,
            segments: segments,

            // 세그먼트별 선택 좌석 저장 { segmentId: { passengerId: seatNo } }
            selectedSeatsBySegment: {},

            // 현재 세그먼트의 선택 좌석 (편의용 getter/setter)
            get selectedSeatsByPassenger() {
                return this.selectedSeatsBySegment[this.activeSegmentId] || {};
            },
            set selectedSeatsByPassenger(val) {
                this.selectedSeatsBySegment[this.activeSegmentId] = val;
            },

            passengers: [],
            passengerNameById: {},
        };

        // 각 세그먼트 초기화
        segments.forEach((seg) => {
            state.selectedSeatsBySegment[seg.segmentId] = {};
        });

        // 승객 목록
        document.querySelectorAll("#passenger-source .passenger-source").forEach((el) => {
            const pid = String(el.dataset.passengerId || "");
            const name = (el.dataset.passengerName || "").trim();
            if (!pid) return;

            state.passengers.push({ passengerId: pid, name });
            state.passengerNameById[pid] = name;
        });

        // activePassengerId 초기값
        state.activePassengerId =
            (state.passengers[0] && state.passengers[0].passengerId) || ctx.defaultPassengerId;

        return { dom, ctx, state };
    }

    return { init };
})();
