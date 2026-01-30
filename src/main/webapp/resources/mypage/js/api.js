import { fetchWithRefresh } from "../../common/js/authFetch.js";

export async function fetchJson(url, options) {
    const res = await fetchWithRefresh(url, {
        headers: {
            Accept: "application/json",
            ...options?.headers,
        },
        ...options,
    });

    if (!res.ok) {
        throw new Error(`HTTP ${res.status}`);
    }

    const data = await res.json();
    if (data && data.success === false) {
        throw new Error(data.message || "API error");
    }
    return data;
}
