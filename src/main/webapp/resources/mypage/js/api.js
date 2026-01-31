import { fetchWithRefresh } from "../../common/js/authFetch.js";

export async function fetchJson(url, options) {
    const { headers: customHeaders, ...restOptions } = options || {};
    const res = await fetchWithRefresh(url, {
        headers: {
            Accept: "application/json",
            ...customHeaders,
        },
        ...restOptions,
    });

    if (!res.ok) {
        throw new Error(`HTTP ${res.status}`);
    }

    if (res.status === 204) {
        return null;
    }

    const text = await res.text();
    if (!text) {
        return null;
    }

    const data = JSON.parse(text);
    if (data && data.success === false) {
        throw new Error(data.message || "API error");
    }
    return data;
}

export async function fetchOk(url, options) {
    const { headers: customHeaders, ...restOptions } = options || {};
    const res = await fetchWithRefresh(url, {
        headers: {
            Accept: "application/json",
            ...customHeaders,
        },
        ...restOptions,
    });

    if (!res.ok) {
        throw new Error(`HTTP ${res.status}`);
    }
    return res;
}
