import { fetchJson } from "./api.js";
import { state } from "./state.js";

async function loadProfile({ needDashboard, needProfile }) {
    try {
        const res = await fetchJson("/api/profile");
        state.profile = res.data;
        if (needDashboard) {
            const { updateDashboardProfile } = await import("./render/dashboard.js");
            updateDashboardProfile(res.data);
        }
        if (needProfile) {
            const { updateProfileTab } = await import("./profile.js");
            updateProfileTab(res.data);
        }
    } catch (e) {
        console.error(e);
    }
}

async function loadReservations({ needDashboard, needBookings }) {
    try {
        const res = await fetchJson("/api/users/me/reservations?page=1&size=20");
        const list = Array.isArray(res.data) ? res.data : [];
        state.reservations = list;
        if (needDashboard) {
            const { renderRecentBookings } = await import("./render/dashboard.js");
            renderRecentBookings(list.slice(0, 3));
        }
        if (needBookings) {
            const { renderBookingList } = await import("./render/bookings.js");
            renderBookingList(list);
        }
    } catch (e) {
        console.error(e);
    }
}

async function loadReservationPassengers(reservationId) {
    if (!reservationId) return;
    try {
        const res = await fetchJson(`/api/users/me/reservations/${reservationId}/passengers`);
        const passengers = res.data?.passengers || [];
        const { renderSeatSummary, renderSeatModalPassengers } = await import("./render/passengers.js");
        renderSeatSummary(passengers);
        renderSeatModalPassengers(passengers);
    } catch (e) {
        console.error(e);
    }
}

async function init() {
    const params = new URLSearchParams(window.location.search);
    const tab = params.get("tab") || "dashboard";
    const reservationId = params.get("id");

    const needDashboard = tab === "dashboard";
    const needBookings = tab === "bookings";
    const needProfile = tab === "profile";

    if (needDashboard || needProfile) {
        await loadProfile({ needDashboard, needProfile });
    }
    if (needDashboard || needBookings) {
        await loadReservations({ needDashboard, needBookings });
    }

    if (tab === "booking_detail" && reservationId) {
        try {
            const res = await fetchJson(`/api/users/me/reservations/${reservationId}`);
            const { updateReservationDetail, initDetailRouteLabels } = await import("./render/detail.js");
            updateReservationDetail(res.data);
            initDetailRouteLabels(res.data);
        } catch (e) {
            console.error(e);
        }
        await loadReservationPassengers(reservationId);
    }

    if (needProfile) {
        const { initProfileSave, initProfileInputGuards, initWithdrawHandler } = await import("./profile.js");
        initProfileInputGuards();
        initProfileSave();
        initWithdrawHandler();
    }
}

init();
