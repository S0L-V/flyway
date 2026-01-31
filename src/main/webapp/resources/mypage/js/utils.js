export function $(id) {
    return document.getElementById(id);
}

export function getContextPath() {
    return window.APP?.contextPath ?? "";
}

export function toMypageUrl(query) {
    const base = getContextPath();
    const path = `${base}/mypage`;
    if (!query) return path;
    return query.startsWith("?") ? `${path}${query}` : `${path}?${query}`;
}

export function textOrDash(value) {
    return value == null || value === "" ? "-" : String(value);
}

export function formatReservationId(value) {
    if (!value) return "-";
    const str = String(value);
    if (str.length <= 12) return str;
    return `${str.slice(0, 8)}…${str.slice(-4)}`;
}

export function splitKoreanName(name) {
    if (!name) return { last: "", first: "" };
    if (name.length === 1) return { last: name, first: "" };
    return { last: name.substring(0, 1), first: name.substring(1) };
}

export function formatDate(value) {
    if (!value) return "-";
    const str = String(value);
    return str.length >= 10 ? str.substring(0, 10) : str;
}

export function formatTime(value) {
    if (!value) return "-";
    const str = String(value);
    return str.length >= 16 ? str.substring(11, 16) : str;
}

export function formatDateTime(value) {
    if (!value) return "-";
    return `${formatDate(value)} ${formatTime(value)}`;
}

export function formatCurrency(value) {
    if (value == null || value === "") return "-";
    try {
        return `₩${Number(value).toLocaleString("ko-KR")}`;
    } catch (e) {
        return String(value);
    }
}

export function formatDurationMinutes(minutes) {
    if (minutes == null || Number.isNaN(Number(minutes))) return "-";
    const mins = Number(minutes);
    const h = Math.floor(mins / 60);
    const m = mins % 60;
    if (h <= 0) return `${m}분`;
    if (m === 0) return `${h}시간`;
    return `${h}시간 ${m}분`;
}

export function diffMinutes(start, end) {
    if (!start || !end) return null;
    const s = new Date(start);
    const e = new Date(end);
    const diff = Math.round((e - s) / 60000);
    return diff > 0 ? diff : null;
}

export function mapStatus(status) {
    const key = (status || "").toUpperCase();
    if (["CONFIRMED", "PAID", "ACTIVE"].includes(key)) {
        return { label: "예약확정", className: "bg-green-50 text-green-700 border-green-200" };
    }
    if (["CANCELLED", "CANCELED"].includes(key)) {
        return { label: "예약취소", className: "bg-red-50 text-red-700 border-red-200" };
    }
    if (["EXPIRED"].includes(key)) {
        return { label: "만료", className: "bg-slate-100 text-slate-600 border-slate-200" };
    }
    if (["PENDING", "UNPAID"].includes(key)) {
        return { label: "결제대기", className: "bg-amber-50 text-amber-700 border-amber-200" };
    }
    return { label: status || "-", className: "bg-slate-50 text-slate-700 border-slate-200" };
}

export function getReservationStatus(item) {
    if (!item) return null;
    return item.reservationStatus || item.status || null;
}

export function applyStatusBadge(el, status) {
    if (!el) return;
    const mapped = mapStatus(status);
    el.textContent = mapped.label;
    el.className = `px-2.5 py-0.5 rounded-full text-xs font-semibold border ${mapped.className}`;
}
