import {
    $,
    textOrDash,
    formatDate,
    formatTime,
    formatDurationMinutes,
    formatCurrency,
    formatDateTime,
    getReservationStatus,
    applyStatusBadge,
    getContextPath,
} from "../utils.js";
import { fetchJson } from "../api.js";

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

function applyAirlineInfo({ logoEl, nameEl, flightEl, airlineMap, airlineId, flightNumber }) {
    const trimmedId = airlineId ? String(airlineId).trim() : "";
    let key = trimmedId ? trimmedId.toUpperCase() : "";
    if (!key && flightNumber) {
        const flightStr = String(flightNumber).trim();
        const match = flightStr.match(/^[A-Za-z]{1,3}/);
        key = match ? match[0].toUpperCase() : "";
        if (!key && flightStr) {
            key = flightStr.slice(0, 2).toUpperCase();
        }
    }
    const info = key && airlineMap ? airlineMap[key] : null;
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

export function updateReservationDetail(detail) {
    if (!detail) return;

    const res = detail.reservation || {};
    const itinerary = detail.itinerary || {};
    const outbound = itinerary.outbound || null;
    const inbound = itinerary.inbound || null;
    const payment = detail.payment || {};

    const reservedAt = $("detailReservedAt");
    const reservationNo = $("detailReservationNo");
    const statusEl = $("detailStatus");
    if (reservedAt) reservedAt.textContent = `예약일: ${formatDate(res.reservedAt)}`;
    if (reservationNo) reservationNo.textContent = `${res.reservationId || "-"}`;
    applyStatusBadge(statusEl, getReservationStatus(res));

    getAirlineMap().then((airlineMap) => {
        if (outbound) {
            applyAirlineInfo({
                logoEl: $("detailOutboundAirlineLogo"),
                nameEl: $("detailOutboundAirlineName"),
                flightEl: $("detailOutboundFlightNo"),
                airlineMap,
                airlineId: outbound.airlineId,
                flightNumber: outbound.flightNumber,
            });
        }
        if (inbound) {
            applyAirlineInfo({
                logoEl: $("detailInboundAirlineLogo"),
                nameEl: $("detailInboundAirlineName"),
                flightEl: $("detailInboundFlightNo"),
                airlineMap,
                airlineId: inbound.airlineId,
                flightNumber: inbound.flightNumber,
            });
        }
    });

    if (outbound) {
        const outDate = $("detailOutboundDate");
        const outDepartTime = $("detailOutboundDepartTime");
        const outDepartAirport = $("detailOutboundDepartAirport");
        const outDepartCity = $("detailOutboundDepartCity");
        const outTerminal = $("detailOutboundTerminalNo");
        const outArrivalTime = $("detailOutboundArrivalTime");
        const outArrivalAirport = $("detailOutboundArrivalAirport");
        const outArrivalCity = $("detailOutboundArrivalCity");
        const outDuration = $("detailOutboundDuration");

        if (outDate) outDate.textContent = formatDate(outbound.departure?.time);
        if (outDepartTime) outDepartTime.textContent = formatTime(outbound.departure?.time);
        if (outDepartAirport) outDepartAirport.textContent = textOrDash(outbound.departure?.airportId);
        if (outDepartCity) outDepartCity.textContent = textOrDash(outbound.departure?.city);
        if (outTerminal) outTerminal.textContent = outbound.terminalNo ? `터미널 ${outbound.terminalNo}` : "-";
        if (outArrivalTime) outArrivalTime.textContent = formatTime(outbound.arrival?.time);
        if (outArrivalAirport) outArrivalAirport.textContent = textOrDash(outbound.arrival?.airportId);
        if (outArrivalCity) outArrivalCity.textContent = textOrDash(outbound.arrival?.city);
        if (outDuration) outDuration.textContent = formatDurationMinutes(outbound.durationMinutes);
    }

    const inboundSection = $("detailInboundSection");
    if (inbound && inboundSection) {
        inboundSection.classList.remove("hidden");
        const inDate = $("detailInboundDate");
        const inDepartTime = $("detailInboundDepartTime");
        const inDepartAirport = $("detailInboundDepartAirport");
        const inDepartCity = $("detailInboundDepartCity");
        const inTerminal = $("detailInboundTerminalNo");
        const inArrivalTime = $("detailInboundArrivalTime");
        const inArrivalAirport = $("detailInboundArrivalAirport");
        const inArrivalCity = $("detailInboundArrivalCity");
        const inDuration = $("detailInboundDuration");

        if (inDate) inDate.textContent = formatDate(inbound.departure?.time);
        if (inDepartTime) inDepartTime.textContent = formatTime(inbound.departure?.time);
        if (inDepartAirport) inDepartAirport.textContent = textOrDash(inbound.departure?.airportId);
        if (inDepartCity) inDepartCity.textContent = textOrDash(inbound.departure?.city);
        if (inTerminal) inTerminal.textContent = inbound.terminalNo ? `터미널 ${inbound.terminalNo}` : "-";
        if (inArrivalTime) inArrivalTime.textContent = formatTime(inbound.arrival?.time);
        if (inArrivalAirport) inArrivalAirport.textContent = textOrDash(inbound.arrival?.airportId);
        if (inArrivalCity) inArrivalCity.textContent = textOrDash(inbound.arrival?.city);
        if (inDuration) inDuration.textContent = formatDurationMinutes(inbound.durationMinutes);
    } else if (inboundSection) {
        inboundSection.classList.add("hidden");
    }

    const payMethod = $("detailPaymentMethod");
    const payStatus = $("detailPaymentStatus");
    const payAmount = $("detailPaidAmount");
    const payAt = $("detailPaidAt");
    if (payMethod) payMethod.textContent = textOrDash(payment.method);
    if (payStatus) payStatus.textContent = textOrDash(payment.status);
    if (payAmount) payAmount.textContent = formatCurrency(payment.paidAmount);
    if (payAt) payAt.textContent = formatDateTime(payment.paidAt);
}

function renderAmountRow(container, label, meta, amount) {
    if (!container) return;
    const row = document.createElement("div");
    row.className = "flex items-center justify-between gap-3";

    const left = document.createElement("div");
    left.className = "flex flex-col";
    const title = document.createElement("span");
    title.className = "text-sm font-semibold text-slate-700";
    title.textContent = label || "-";
    left.appendChild(title);
    if (meta) {
        const sub = document.createElement("span");
        sub.className = "text-xs text-slate-400";
        sub.textContent = meta;
        left.appendChild(sub);
    }

    const right = document.createElement("span");
    right.className = "text-sm font-semibold text-slate-800";
    right.textContent = formatCurrency(amount);

    row.appendChild(left);
    row.appendChild(right);
    container.appendChild(row);
}

function formatFareMeta(segment) {
    if (!segment) return "";
    const parts = [];
    if (segment.flightNumber) parts.push(segment.flightNumber);
    if (segment.cabinClass) parts.push(segment.cabinClass);
    if (segment.passengerCount) parts.push(`${segment.passengerCount}명`);
    if (segment.pricePerPerson != null) parts.push(`1인 ${formatCurrency(segment.pricePerPerson)}`);
    return parts.join(" · ");
}

export function updatePaymentBreakdown(detail) {
    if (!detail) return;
    const fareList = $("detailPaymentFareList");
    const serviceList = $("detailPaymentServiceList");
    const totalAmountEl = $("detailPaymentTotalAmount");
    const paidAmountEl = $("detailPaidAmount");

    if (fareList) fareList.innerHTML = "";
    if (serviceList) serviceList.innerHTML = "";

    const fare = detail.fare || {};
    if (fare.outbound) {
        renderAmountRow(
            fareList,
            "항공 운임 (가는 편)",
            formatFareMeta(fare.outbound),
            fare.outbound.total
        );
    }
    if (fare.inbound) {
        renderAmountRow(
            fareList,
            "항공 운임 (오는 편)",
            formatFareMeta(fare.inbound),
            fare.inbound.total
        );
    }
    if (fareList && !fareList.children.length) {
        renderAmountRow(fareList, "항공 운임", "", detail.totalAmount);
    }

    const services = Array.isArray(detail.services) ? detail.services : [];
    if (services.length) {
        services.forEach((svc) => {
            renderAmountRow(serviceList, svc.name || "부가서비스", "", svc.amount);
        });
    } else {
        const empty = document.createElement("div");
        empty.className = "text-xs text-slate-400";
        empty.textContent = "부가서비스 없음";
        if (serviceList) serviceList.appendChild(empty);
    }

    if (totalAmountEl) totalAmountEl.textContent = formatCurrency(detail.totalAmount);
    if (paidAmountEl) paidAmountEl.textContent = formatCurrency(detail.paidAmount);
}

export function initRefundButton(payment) {
    const button = $("detailRefundButton");
    const hint = $("detailRefundHint");
    if (!button) return;
    if (button.dataset.bound === "true") return;
    button.dataset.bound = "true";

    const paymentId = payment?.paymentId;
    const status = (payment?.status || "").toUpperCase();
    const amount = payment?.amount;

    function setState({ label, disabled, message }) {
        button.textContent = label;
        button.disabled = disabled;
        if (hint) {
            if (message) {
                hint.textContent = message;
                hint.classList.remove("hidden");
            } else {
                hint.classList.add("hidden");
                hint.textContent = "";
            }
        }
    }

    if (!paymentId) {
        setState({ label: "예약 취소 및 환불", disabled: true, message: "결제 정보를 찾을 수 없습니다." });
        return;
    }
    if (status === "PAID") {
        setState({ label: "예약 취소 및 환불", disabled: false });
    } else if (status === "REFUNDED" || status === "CANCELLED") {
        setState({ label: "환불 완료", disabled: true, message: "환불 처리 완료" });
    } else {
        setState({ label: "환불 불가", disabled: true, message: "현재 상태에서는 환불할 수 없습니다." });
    }

    button.addEventListener("click", async () => {
        if (button.disabled) return;
        const reason = window.prompt("환불 사유를 입력해주세요.");
        if (!reason) return;
        const confirmMessage = amount
            ? `환불 금액 ${formatCurrency(amount)}이(가) 요청됩니다. 진행하시겠습니까?`
            : "환불 요청을 진행하시겠습니까?";
        const ok = window.confirm(confirmMessage);
        if (!ok) return;

        const originalText = button.textContent;
        setState({ label: "환불 처리 중...", disabled: true });
        try {
            const res = await fetchJson(`/api/payments/${paymentId}/refund`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ cancelReason: reason }),
            });
            const nextStatus = (res?.payment?.status || "REFUNDED").toUpperCase();
            if (nextStatus === "PAID") {
                setState({ label: originalText || "환불 요청", disabled: false });
                return;
            }
            setState({ label: "환불 완료", disabled: true, message: "환불 처리 완료" });
            if (typeof window.showToast === "function") {
                window.showToast("환불이 완료되었습니다.");
            } else {
                alert("환불이 완료되었습니다.");
            }
        } catch (e) {
            console.error(e);
            setState({ label: originalText || "환불 요청", disabled: false, message: "환불 요청에 실패했습니다." });
            if (typeof window.showToast === "function") {
                window.showToast("환불 요청에 실패했습니다.");
            } else {
                alert("환불 요청에 실패했습니다.");
            }
        }
    });
}

export function initDetailRouteLabels(detail) {
    if (!detail) return;
    const outbound = detail?.itinerary?.outbound;
    const inbound = detail?.itinerary?.inbound;
    const outRoute = $("seatOutboundRoute");
    const inRoute = $("seatInboundRoute");
    if (outRoute && outbound) {
        outRoute.textContent = `${textOrDash(outbound.departure?.airportId)} → ${textOrDash(outbound.arrival?.airportId)}`;
    }
    if (inRoute && inbound) {
        inRoute.textContent = `${textOrDash(inbound.departure?.airportId)} → ${textOrDash(inbound.arrival?.airportId)}`;
    }
}
