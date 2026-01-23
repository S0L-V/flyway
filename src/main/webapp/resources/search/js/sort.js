let currentSortType = null;

document.addEventListener("DOMContentLoaded", () => {
    initSortButton();
});

function sorting(options) {
    if(currentSortType === null || currentSortType === 'price-asc') {
        currentSortType = 'price-desc';
        options.sort((a, b) => a.totalSeats - b.totalSeats);
    } else if(currentSortType === 'price-desc') {
        currentSortType = 'price-asc'
        options.sort((a, b) => b.totalSeats - a.totalSeats);
    }

    renderByTripType(options);
}

function initSortButton() {
    document.querySelector(".sort-button").addEventListener("click", () => {
        sorting(displayedOptions);
    });
}