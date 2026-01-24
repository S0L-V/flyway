import { fetchWithRefresh } from "../common/authFetch";

async function loadProfile() {
    const base = window.APP?.contextPath ?? "";
    let retried = false;

    async function requestProfile() {
        const response = await fetchWithRefresh("/api/profile", {
            method: "GET",
            headers: {
                "Accept": "application/json",
            },
        });

        if (!response.ok) {
            throw new Error("프로필 조회 실패: " + response.status);
        }

        return response.json();
    }

    try {
        const res = await requestProfile();
        if (!res) return;

        const data = res.data;

        document.getElementById("profileName").textContent =
            data.name ?? "-";
        document.getElementById("profileEmail").textContent =
            data.email ?? "-";
        document.getElementById("profileCreatedAt").textContent =
            data.createdAt ?? "-";
        document.getElementById("profileStatus").textContent =
            data.status ?? "-";

    } catch (e) {
        console.error(e);
        alert("프로필 정보를 불러오지 못했습니다.");
    }
}

loadProfile();
