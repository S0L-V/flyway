async function loadProfile() {
    try {
        const base = window.APP?.contextPath ?? "";
        console.log(base);

        const response = await fetch(
            `${base}/api/profile`,
            {
                method: "GET",
                credentials: "same-origin",
                headers: {
                    "Accept": "application/json"
                }
            }
        );

        if (response.status === 401) {
            // 로그인 안 된 경우
            window.location.href = `${base}/login`;
            return;
        }

        if (!response.ok) {
            throw new Error("프로필 조회 실패: " + response.status);
        }

        const res = await response.json();
        const data = res.data;

        console.log(data);

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
// 페이지 로드 시 실행
loadProfile();