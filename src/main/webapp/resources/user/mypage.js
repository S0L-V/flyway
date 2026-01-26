import { fetchWithRefresh } from "../common/js/authFetch.js";

async function loadProfile() {
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

const withdrawButton = document.getElementById("withdrawButton");
if (withdrawButton) {
    withdrawButton.addEventListener("click", async () => {
        const ok = window.confirm("탈퇴하시겠습니까?");
        if (!ok) return;

        try {
            const response = await fetchWithRefresh("/api/user/withdraw", {
                method: "POST",
                headers: {
                    "Accept": "application/json",
                },
            });

            if (!response.ok) {
                throw new Error("회원탈퇴 실패: " + response.status);
            }

            alert("회원탈퇴가 완료되었습니다.");
            window.location.replace(window.APP?.contextPath || "/");
        } catch (e) {
            console.error(e);
            alert("회원탈퇴에 실패했습니다.");
        }
    });
}
