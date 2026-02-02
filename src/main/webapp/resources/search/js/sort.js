let currentSortType = null;

document.addEventListener("DOMContentLoaded", () => {
    initSortButton();
});

function sorting() {
    const btn = document.getElementById('sortPriceBtn');
    const label = document.getElementById('sortLabel');
    const icon = document.getElementById('sortIcon');

    // 3단계 토글: none -> asc -> desc -> none
    if (currentSortType === null || currentSortType === 'none') {
        currentSortType = 'price-asc';
        displayedOptions.sort((a, b) => a.totalPrice - b.totalPrice);
        if (label) label.textContent = '낮은순';
        if (icon) icon.setAttribute('data-lucide', 'chevron-up');
        btn?.classList.remove('desc');
        btn?.classList.add('asc');
    } else if (currentSortType === 'price-asc') {
        currentSortType = 'price-desc';
        displayedOptions.sort((a, b) => b.totalPrice - a.totalPrice);
        if (label) label.textContent = '높은순';
        if (icon) icon.setAttribute('data-lucide', 'chevron-down');
        btn?.classList.remove('asc');
        btn?.classList.add('desc');
    } else {
        currentSortType = 'none';
        // 원래 순서로 복원 (필터 다시 적용)
        if (typeof updateFilterStateAndRender === 'function') {
            updateFilterStateAndRender();
        }
        if (label) label.textContent = '가격순';
        if (icon) icon.setAttribute('data-lucide', 'chevrons-up-down');
        btn?.classList.remove('asc', 'desc');
        if (typeof lucide !== 'undefined') lucide.createIcons();
        return;
    }

    if (typeof lucide !== 'undefined') lucide.createIcons();
    renderPage(1, false);
}

function initSortButton() {
    const btn = document.getElementById('sortPriceBtn');
    if (btn) {
        btn.addEventListener("click", sorting);
    }
}

function resetSort() {
    currentSortType = null;
    const btn = document.getElementById('sortPriceBtn');
    const label = document.getElementById('sortLabel');
    const icon = document.getElementById('sortIcon');

    if (label) label.textContent = '가격순';
    if (icon) icon.setAttribute('data-lucide', 'chevrons-up-down');
    btn?.classList.remove('asc', 'desc');

    if (typeof lucide !== 'undefined') lucide.createIcons();
}