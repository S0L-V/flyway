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
        renderBentoGrid(data);
    } catch (e) {
        console.error("실시간 랭킹 로딩 실패", e);
    }
}

function renderBentoGrid(list) {
    const grid = document.getElementById("trendingBentoGrid");
    if (!grid) return;

    grid.innerHTML = "";

    // 최대 8개 표시
    const items = list.slice(0, 8);

    items.forEach((item, index) => {
        const card = createTrendCard(item, index);
        grid.appendChild(card);
    });
}

function createTrendCard(item, index) {
    const div = document.createElement("div");

    // 카드 타입 결정: 0=hero(2x2), 1-4=small, 5-6=wide, 7=full
    let cardType = 'small';
    if (index === 0) cardType = 'hero';
    else if (index === 5 || index === 6) cardType = 'wide';
    else if (index === 7) cardType = 'full';

    div.className = `trend-card ${cardType}`;

    const isHero = index === 0;
    const isTop3 = item.rank <= 3;

    div.innerHTML = `
        <div class="trend-card-inner">
            <!-- Rank Ribbon -->
            <div class="trend-rank-ribbon">
                <span class="rank-label">Rank</span>
                <span class="rank-number">0${item.rank}</span>
            </div>

            <!-- Content -->
            <div class="trend-card-content">
                <div class="trend-card-bg" style="background-image: url('${item.imageUrl}');"></div>
                <div class="trend-card-overlay"></div>

                <!-- Top Stats -->
                <div class="trend-card-top">
                    ${renderDiffBadge(item)}
                    ${isHero ? '<span class="hot-badge">HOT 🔥</span>' : ''}
                </div>

                <!-- Bottom Info -->
                <div class="trend-card-bottom">
                    <p class="trend-country">${item.country || ''}</p>
                    <h3 class="trend-city-name">${item.city}</h3>
                    <p class="trend-tagline">${renderTags(item.tag)}</p>

                    <div class="live-views">
                        <span class="dot"></span>
                        <span>${formatNumber(item.searchCount)} Views</span>
                    </div>
                </div>
            </div>
        </div>
    `;

    return div;
}

function renderDiffBadge(item) {
    if (item.new) {
        return '<span class="diff-badge new">NEW</span>';
    }
    if (item.diff > 0) {
        return `<span class="diff-badge up">▲ ${item.diff}</span>`;
    }
    if (item.diff < 0) {
        return `<span class="diff-badge down">▼ ${Math.abs(item.diff)}</span>`;
    }
    return '<span class="diff-badge same">-</span>';
}

function formatNumber(num) {
    if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'K';
    }
    return num.toLocaleString();
}

function parseTags(tagString) {
    if(!tagString) {
        return [];
    }

    return tagString
        .split('#')
        .map(t => t.trim())
        .filter(t => t.length > 0);
}

function renderTags(tagString) {
    const tags = parseTags(tagString);
    if (tags.length === 0) return '';

    return tags
        .slice(0, 2)
        .map(tag => `#${tag}`)
        .join(' ');
}
