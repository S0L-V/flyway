import { csrfFetch } from "./csrfFetch.js";

const CONFIG = {
    REFRESH_URL: "/auth/refresh",
    LOGIN_URL: "/login",
};

let refreshPromise = null;

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

/**
 * 인증 필요 API 전용
 * - 401이면 refresh 시도 후 재요청
 * - refresh 실패/재시도 후에도 401이면 /login 이동
 */
export async function fetchWithRefresh(input, init = {}) {
    const doFetch = async (overrideInit = {}) => {
        const merged = { ...init, ...overrideInit };
        // csrfFetch가 GET/POST 알아서 처리함
        return csrfFetch(input, merged);
    };

    let res = await doFetch();
    if (res.status !== 401) return res;

    if (!refreshPromise) {
        refreshPromise = (async () => {
            try {
                const refreshRes = await csrfFetch(CONFIG.REFRESH_URL, { method: "POST" });
                return refreshRes.ok;
            } catch (e) {
                return false;
            } finally {
                refreshPromise = null;
            }
        })();
    }

    const refreshed = await refreshPromise;

    if (!refreshed) {
        window.location.href = toFetchUrl(CONFIG.LOGIN_URL);
        throw new Error("Unauthorized (refresh failed)");
    }

    res = await doFetch();

    if (res.status === 401) {
        window.location.href = toFetchUrl(CONFIG.LOGIN_URL);
        throw new Error("Unauthorized (still 401 after refresh)");
    }

    return res;
}
