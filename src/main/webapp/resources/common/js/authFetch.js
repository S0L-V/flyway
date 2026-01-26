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

    const base = getBasePath();
    return joinBasePath(base, raw);
}

export async function fetchWithRefresh(input, init = {}) {
    const doFetch = async () =>
        fetch(toFetchUrl(input), {
            credentials: "same-origin",
            ...init,
        });

    let res = await doFetch();

    if (res.status !== 401) return res;

    if (!refreshPromise) {
        refreshPromise = (async () => {
            try {
                const refreshRes = await fetch(toFetchUrl(CONFIG.REFRESH_URL), {
                    method: "POST",
                    credentials: "same-origin",
                });
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

    // refresh 성공 → 재시도
    res = await doFetch();

    if (res.status === 401) {
        window.location.href = toFetchUrl(CONFIG.LOGIN_URL);
        throw new Error("Unauthorized (still 401 after refresh)");
    }

    return res;
}
