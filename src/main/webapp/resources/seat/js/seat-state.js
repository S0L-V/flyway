window.SeatState = (() => {
    function init() {
        const seatGridEl = document.getElementById("seat-grid");
        if (!seatGridEl) return null;

        const dom = {
            seatGridEl,
            summaryEl: document.getElementById("selected-summary"),
            actionEl: document.getElementById("seat-action"),

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

        const state = {
            isHolding: false,
            activePassengerId: "",
            selectedSeatsByPassenger: {}, // { passengerId: seatNo }
            passengers: [], // [{ passengerId, name }]
            passengerNameById: {},
        };

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
