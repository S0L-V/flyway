document.addEventListener("DOMContentLoaded", () => {
    loadHotSixAirport();
    setInterval(loadHotSixAirport, 30_000);
})

async function loadHotSixAirport() {
    try {
        const res = await fetch(`${CONTEXT_PATH}/api/public/rank/realtime`);
        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`);
        }
        const data = await res.json();
        renderHotSix(data);
    } catch (e) {
        console.error("실시간 랭킹 로딩 실패", e);
    }
}

function renderHotSix(list) {
    const grid = document.querySelector(".trending-grid");
    grid.innerHTML = "";

    list.forEach(item => {
        const div = document.createElement("div");
        div.className = "trend-item";

        div.innerHTML = `
            <div class="rank-num ${getRankClass(item.rank)}">${item.rank}</div>
            <div class="trend-thumb" 
                style="background-image: url('${item.imageUrl}');">
            </div>
            <div class="trend-info">
                <div class="trend-city">${item.city}</div>
                <span class="trend-tags">#유니버셜 스튜디오</span>
            </div>
            <div class="diff ${getDiffClass(item)}">
                ${renderDiff(item)}
            </div>
            <div class="trend-score">
                <span class="score-label">Score</span>
                <div class="score-val ${getRankClass(item.rank)}">${item.searchCount.toLocaleString()}</div>
            </div>
        `;

        grid.appendChild(div);
    })
}

function renderDiff(item) {
    if (item.new) return "NEW";
    if (item.diff > 0) return `▲ ${item.diff}`;
    if (item.diff < 0) return `▼ ${Math.abs(item.diff)}`;
    return "-";
}

function getDiffClass(item) {
    if (item.new) return "new";
    if (item.diff > 0) return "up";
    if (item.diff < 0) return "down";
    return "same";
}

function getRankClass(rank) {
    if(rank > 3) {
        return "bottom";
    }
    return "";
}