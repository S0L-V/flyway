let currentSortType = null;

document.addEventListener("DOMContentLoaded", () => {
    initSortButton();
});

function sorting() {
    if(currentSortType === null || currentSortType === 'price-asc') {
        currentSortType = 'price-desc';
        displayedOptions.sort((a, b) => a.totalPrice - b.totalPrice);
    } else if(currentSortType === 'price-desc') {
        currentSortType = 'price-asc'
        displayedOptions.sort((a, b) => b.totalPrice - a.totalPrice);
    }

    renderPage(1);
}

function initSortButton() {
    document.querySelector(".sort-button").addEventListener("click", () => {
        sorting();
    });
}