import {
    $,
    textOrDash,
    formatDate,
    formatDateTime,
    formatTime,
    diffMinutes,
    formatDurationMinutes,
    getReservationStatus,
    applyStatusBadge,
    toMypageUrl,
    formatReservationId,
    getContextPath,
} from "../utils.js";

let airlineMapPromise = null;

function toAssetUrl(path) {
    if (!path) return "";
    if (/^https?:\/\//i.test(path)) return path;
    const base = getContextPath();
    if (!base) return path;
    return path.startsWith("/") ? `${base}${path}` : `${base}/${path}`;
}

async function getAirlineMap() {
    if (airlineMapPromise) return airlineMapPromise;
    const url = `${getContextPath()}/resources/mypage/json/airline.json`;
    airlineMapPromise = fetch(url, { headers: { Accept: "application/json" } })
        .then((res) => (res.ok ? res.json() : {}))
        .catch(() => ({}));
    return airlineMapPromise;
}

function setAirlineInfo(node, prefix, airlineMap, airlineId, flightNumber) {
    const key = (airlineId || (flightNumber ? String(flightNumber).slice(0, 2) : "") || "").toUpperCase();
    const info = key && airlineMap ? airlineMap[key] : null;
    const logoEl = node.querySelector(`[data-field=\"${prefix}AirlineLogo\"]`);
    const nameEl = node.querySelector(`[data-field=\"${prefix}AirlineName\"]`);
    const flightEl = node.querySelector(`[data-field=\"${prefix}FlightNumber\"]`);

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

export async function renderBookingList(items, pageInfo) {
    const container = $("bookingList");
    const empty = $("bookingListEmpty");
    const pagination = $("bookingPagination");
    const template = $("bookingItemTemplate");
    if (!container || !template) return;
    container.innerHTML = "";
    if (pagination) pagination.innerHTML = "";

    if (!items || items.length === 0) {
        if (empty) empty.classList.remove("hidden");
        if (pagination) pagination.classList.add("hidden");
        return;
    }
    if (empty) empty.classList.add("hidden");

    const airlineMap = await getAirlineMap();

    items.forEach((item) => {
        const node = template.content.cloneNode(true);
        const root = node.querySelector("div");
        if (root) {
            root.addEventListener("click", () => {
                window.location.href = toMypageUrl(`tab=booking_detail&id=${item.reservationId}`);
            });
        }

        const noEl = node.querySelector('[data-field="reservationNo"]');
        if (noEl) {
            const fullId = textOrDash(item.reservationId);
            noEl.textContent = `${formatReservationId(fullId)}`;
            noEl.setAttribute("title", fullId);
        }
        const reservedEl = node.querySelector('[data-field="reservedAt"]');
        if (reservedEl) reservedEl.textContent = `예약일: ${formatDate(item.reservedAt)}`;
        const statusEl = node.querySelector('[data-field="status"]');
        applyStatusBadge(statusEl, getReservationStatus(item));

        const outDateTime = node.querySelector('[data-field="outDateTime"]');
        if (outDateTime) outDateTime.textContent = formatDateTime(item.outDepartureTime);
        const outDepartTime = node.querySelector('[data-field="outDepartTime"]');
        if (outDepartTime) outDepartTime.textContent = formatTime(item.outDepartureTime);
        const outDepartAirport = node.querySelector('[data-field="outDepartAirport"]');
        if (outDepartAirport) outDepartAirport.textContent = formatAirportCity(item.outDepartureAirport, item.outDepartureCity);
        const outArrivalTime = node.querySelector('[data-field="outArrivalTime"]');
        if (outArrivalTime) outArrivalTime.textContent = formatTime(item.outArrivalTime);
        const outArrivalAirport = node.querySelector('[data-field="outArrivalAirport"]');
        if (outArrivalAirport) outArrivalAirport.textContent = formatAirportCity(item.outArrivalAirport, item.outArrivalCity);
        const outDuration = node.querySelector('[data-field="outDuration"]');
        const minutes = diffMinutes(item.outDepartureTime, item.outArrivalTime);
        if (outDuration) outDuration.textContent = minutes ? formatDurationMinutes(minutes) : "-";
        setAirlineInfo(node, "out", airlineMap, item.outAirlineId, item.outFlightNumber);

        const inboundBlock = node.querySelector('[data-field="inboundBlock"]');
        const hasInbound = !!item.inFlightId;
        if (inboundBlock) {
            if (hasInbound) inboundBlock.classList.remove("hidden");
            else inboundBlock.classList.add("hidden");
        }
        if (hasInbound) {
            const inDateTime = node.querySelector('[data-field="inDateTime"]');
            if (inDateTime) inDateTime.textContent = formatDateTime(item.inDepartureTime);
            const inDepartTime = node.querySelector('[data-field="inDepartTime"]');
            if (inDepartTime) inDepartTime.textContent = formatTime(item.inDepartureTime);
            const inDepartAirport = node.querySelector('[data-field="inDepartAirport"]');
            if (inDepartAirport) inDepartAirport.textContent = formatAirportCity(item.inDepartureAirport, item.inDepartureCity);
            const inArrivalTime = node.querySelector('[data-field="inArrivalTime"]');
            if (inArrivalTime) inArrivalTime.textContent = formatTime(item.inArrivalTime);
            const inArrivalAirport = node.querySelector('[data-field="inArrivalAirport"]');
            if (inArrivalAirport) inArrivalAirport.textContent = formatAirportCity(item.inArrivalAirport, item.inArrivalCity);
            const inDuration = node.querySelector('[data-field="inDuration"]');
            const inMinutes = diffMinutes(item.inDepartureTime, item.inArrivalTime);
            if (inDuration) inDuration.textContent = inMinutes ? formatDurationMinutes(inMinutes) : "-";
            setAirlineInfo(node, "in", airlineMap, item.inAirlineId, item.inFlightNumber);
        }

        container.appendChild(node);
    });

    if (window.lucide) window.lucide.createIcons();

    renderBookingPagination(pageInfo);
}

function formatAirportCity(airport, city) {
    const a = textOrDash(airport);
    const c = textOrDash(city);
    if (a !== "-" && c !== "-") return `${a} / ${c}`;
    if (a !== "-") return a;
    if (c !== "-") return c;
    return "-";
}

function renderBookingPagination(pageInfo) {
    const container = $("bookingPagination");
    if (!container) return;
    if (!pageInfo || !pageInfo.totalPages || pageInfo.totalPages <= 1) {
        container.classList.add("hidden");
        return;
    }
    container.classList.remove("hidden");
    container.innerHTML = "";

    const current = Number(pageInfo.page) || 1;
    const total = Number(pageInfo.totalPages) || 1;

    const makeButton = (label, targetPage, disabled, isActive = false) => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.textContent = label;
        btn.className = isActive
            ? "px-3 py-1 rounded text-sm font-semibold bg-primary-600 text-white"
            : "px-3 py-1 rounded text-sm font-medium bg-white border border-slate-200 text-slate-600 hover:border-primary-500 hover:text-primary-600";
        if (disabled) {
            btn.disabled = true;
            btn.className = "px-3 py-1 rounded text-sm font-medium bg-slate-100 text-slate-400 cursor-not-allowed";
        } else {
            btn.addEventListener("click", () => navigateToPage(targetPage));
        }
        return btn;
    };

    container.appendChild(makeButton("이전", Math.max(1, current - 1), !pageInfo.hasPrevious));

    const startPage = Math.max(1, current - 2);
    const endPage = Math.min(total, startPage + 4);
    for (let p = startPage; p <= endPage; p += 1) {
        container.appendChild(makeButton(String(p), p, false, p === current));
    }

    container.appendChild(makeButton("다음", Math.min(total, current + 1), !pageInfo.hasNext));
}

function navigateToPage(page) {
    const url = new URL(window.location.href);
    url.searchParams.set("tab", "bookings");
    url.searchParams.set("page", String(page));
    window.location.href = url.toString();
}
