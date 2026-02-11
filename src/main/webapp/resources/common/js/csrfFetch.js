const CONFIG = {
    CSRF_COOKIE_NAME: "XSRF-TOKEN",
    CSRF_HEADER_NAME: "X-XSRF-TOKEN",
    CSRF_BOOTSTRAP_URL: "/auth/csrf",
};

let csrfFetchReady = false;
const pendingCsrfFetchCalls = [];

function enqueueCsrfFetchCall(args) {
    return new Promise((resolve, reject) => {
        pendingCsrfFetchCalls.push({ args, resolve, reject });
    });
}

function flushQueuedCsrfFetchCalls() {
    while (pendingCsrfFetchCalls.length > 0) {
        const call = pendingCsrfFetchCalls.shift();
        csrfFetch(...call.args).then(call.resolve).catch(call.reject);
    }
}

if (typeof window !== "undefined") {
    window.csrfFetch = (...args) => {
        if (csrfFetchReady) {
            return csrfFetch(...args);
        }
        return enqueueCsrfFetchCall(args);
    };
}

function getBasePath() {
    return window.APP?.contextPath ?? "";
}

function isAbsoluteHttpUrl(url) {
    return typeof url === "string" && /^https?:\/\//i.test(url);
}

function joinBasePath(base, path) {
    if (!base) return path;
    const b = base.endsWith("/") ? base.slice(0, -1) : base;
    const p = path.startsWith("/") ? path : `/${path}`;
    return `${b}${p}`;
}

function normalizeInputToUrl(input) {
    if (input instanceof Request) return input.url;
    if (typeof input === "string") return input;
    return String(input);
}

function toFetchUrl(input) {
    const raw = normalizeInputToUrl(input);
    if (isAbsoluteHttpUrl(raw)) return raw;
    return joinBasePath(getBasePath(), raw);
}

function readCookieRaw(name) {
    const found = document.cookie
        .split("; ")
        .find((row) => row.startsWith(name + "="));
    if (!found) return null;
    const idx = found.indexOf("=");
    return found.substring(idx + 1);
}

function readCookie(name) {
    const raw = readCookieRaw(name);
    return raw ? decodeURIComponent(raw) : null;
}

function isStateChanging(method) {
    const m = (method || "GET").toUpperCase();
    return ["POST", "PUT", "PATCH", "DELETE"].includes(m);
}

async function ensureCsrfCookie() {
    const existing = readCookie(CONFIG.CSRF_COOKIE_NAME);
    if (existing) return existing;

    try {
        await fetch(toFetchUrl(CONFIG.CSRF_BOOTSTRAP_URL), {
            method: "GET",
            credentials: "same-origin",
            cache: "no-store",
        });
    } catch (e) {}

    return readCookie(CONFIG.CSRF_COOKIE_NAME);
}

function withCsrfHeader(init = {}) {
    const method = (init.method || "GET").toUpperCase();
    if (!isStateChanging(method)) return init;

    const headers = new Headers(init.headers || {});
    if (!headers.has(CONFIG.CSRF_HEADER_NAME)) {
        const token = readCookie(CONFIG.CSRF_COOKIE_NAME);
        if (token) headers.set(CONFIG.CSRF_HEADER_NAME, token);
    }

    return { ...init, headers };
}

/**
 * csrfFetch
 * - GET/HEAD/OPTIONS: 그냥 fetch
 * - POST/PUT/PATCH/DELETE: CSRF 쿠키 보장 + 헤더 자동첨부
 * - 401 처리/refresh/redirect 절대 안 함
 */
export async function csrfFetch(input, init = {}) {
    const merged = { credentials: "same-origin", ...init };

    if (isStateChanging(merged.method)) {
        const token = await ensureCsrfCookie();
        if (!token) throw new Error("CSRF token cookie not found (bootstrap failed)");
    }

    return fetch(toFetchUrl(input), withCsrfHeader(merged));
}

csrfFetchReady = true;

if (typeof window !== "undefined") {
    window.csrfFetch = (...args) => csrfFetch(...args);
    flushQueuedCsrfFetchCalls();
}
