import {
    $,
    textOrDash,
    splitKoreanName,
    formatDate,
    formatDateTime,
    getReservationStatus,
    applyStatusBadge,
    toMypageUrl,
    formatReservationId,
    getContextPath,
} from "../utils.js";

export function updateDashboardProfile(profile) {
    const name = profile?.name || "";
    const kr = splitKoreanName(name);
    const enLast = profile?.lastName || "";
    const enFirst = profile?.firstName || "";
    const initials = (enLast || enFirst)
        ? `${enLast ? enLast[0] : ""}${enFirst ? enFirst[0] : ""}`.toUpperCase()
        : (name || "-").substring(0, 2);

    const elInitials = $("dashboardInitials");
    const elKrName = $("dashboardKrName");
    const elEnName = $("dashboardEnName");
    const elEmail = $("dashboardEmail");

    if (elInitials) elInitials.textContent = initials || "-";
    if (elKrName) elKrName.textContent = name ? `${kr.last}${kr.first}` : "-";
    if (elEnName) elEnName.textContent = enLast || enFirst ? `/ ${enLast} ${enFirst}`.trim() : "/ -";
    if (elEmail) elEmail.textContent = textOrDash(profile?.email);
}

export async function renderRecentBookings(items) {
    const container = $("recentBookings");
    const empty = $("recentBookingsEmpty");
    const template = $("recentBookingTemplate");

    if (!container || !template) return;
    container.innerHTML = "";

    if (!items || items.length === 0) {
        if (empty) empty.classList.remove("hidden");
        return;
    }
    if (empty) empty.classList.add("hidden");

    const airlineMap = await getAirlineMap();

    for (const item of items) {
        const node = template.content.cloneNode(true);
        const root = node.querySelector("div");
        if (root) {
            root.addEventListener("click", () => {
                window.location.href = toMypageUrl(`tab=booking_detail&id=${item.reservationId}`);
            });
        }

        const statusEl = node.querySelector('[data-field="status"]');
        applyStatusBadge(statusEl, getReservationStatus(item));
        const noEl = node.querySelector('[data-field="reservationNo"]');
        if (noEl) {
            const fullId = textOrDash(item.reservationId);
            noEl.textContent = `${formatReservationId(fullId)}`;
            noEl.setAttribute("title", fullId);
        }
        const reservedAtEl = node.querySelector('[data-field="reservedAt"]');
        if (reservedAtEl) reservedAtEl.textContent = formatDate(item.reservedAt);
        const depAirport = node.querySelector('[data-field="depAirport"]');
        const depCity = node.querySelector('[data-field="depCity"]');
        const arrAirport = node.querySelector('[data-field="arrAirport"]');
        const arrCity = node.querySelector('[data-field="arrCity"]');
        if (depAirport) depAirport.textContent = textOrDash(item.outDepartureAirport);
        if (depCity) depCity.textContent = textOrDash(item.outDepartureCity);
        if (arrAirport) arrAirport.textContent = textOrDash(item.outArrivalAirport);
        if (arrCity) arrCity.textContent = textOrDash(item.outArrivalCity);
        const outDepartAt = node.querySelector('[data-field="outDepartAt"]');
        if (outDepartAt) outDepartAt.textContent = formatDateTime(item.outDepartureTime);
        setAirlineInfo(node, "out", airlineMap, item.outAirlineId, item.outFlightNumber);
        const inboundRow = node.querySelector('[data-field="inboundRow"]');
        const hasInbound = !!item.inFlightId;
        if (inboundRow) {
            if (hasInbound) inboundRow.classList.remove("hidden");
            else inboundRow.classList.add("hidden");
        }
        if (hasInbound) {
            const inDepAirport = node.querySelector('[data-field="inDepAirport"]');
            const inDepCity = node.querySelector('[data-field="inDepCity"]');
            const inArrAirport = node.querySelector('[data-field="inArrAirport"]');
            const inArrCity = node.querySelector('[data-field="inArrCity"]');
            if (inDepAirport) inDepAirport.textContent = textOrDash(item.inDepartureAirport);
            if (inDepCity) inDepCity.textContent = textOrDash(item.inDepartureCity);
            if (inArrAirport) inArrAirport.textContent = textOrDash(item.inArrivalAirport);
            if (inArrCity) inArrCity.textContent = textOrDash(item.inArrivalCity);
            const inDepartAt = node.querySelector('[data-field="inDepartAt"]');
            if (inDepartAt) inDepartAt.textContent = formatDateTime(item.inDepartureTime);
            setAirlineInfo(node, "in", airlineMap, item.inAirlineId, item.inFlightNumber);
            const inboundAirline = node.querySelector('[data-field="inAirlineBlock"]');
            if (inboundAirline) inboundAirline.classList.remove("hidden");
        }

        container.appendChild(node);
    }

    if (window.lucide?.createIcons) window.lucide.createIcons();
}

function toAssetUrl(path) {
    if (!path) return "";
    if (/^https?:\/\//i.test(path)) return path;
    const base = getContextPath();
    if (!base) return path;
    return path.startsWith("/") ? `${base}${path}` : `${base}/${path}`;
}

function getAirlineMap() {
    const url = `${getContextPath()}/resources/mypage/json/airline.json`;
    return fetch(url, { headers: { Accept: "application/json" } })
        .then((res) => (res.ok ? res.json() : {}))
        .catch(() => ({}));
}

function setAirlineInfo(node, prefix, airlineMap, airlineId, flightNumber) {
    const key = (airlineId || (flightNumber ? String(flightNumber).slice(0, 2) : "") || "").toUpperCase();
    const info = key && airlineMap ? airlineMap[key] : null;
    const logoEl = node.querySelector(`[data-field="${prefix}AirlineLogo"]`);
    const nameEl = node.querySelector(`[data-field="${prefix}AirlineName"]`);
    const flightEl = node.querySelector(`[data-field="${prefix}FlightNumber"]`);

    if (logoEl) {
        if (info?.image) {
            logoEl.src = toAssetUrl(info.image);
            logoEl.classList.remove("hidden");
        } else {
            logoEl.classList.add("hidden");
        }
    }
    if (nameEl) nameEl.textContent = info?.name || (key || "-");
    if (flightEl) flightEl.textContent = flightNumber || "-";
}
