let currentPage = 1;
const pageSize = 10;

function renderPage(pageNum) {
    if (pageNum < 1) pageNum = 1;
    currentPage = pageNum;
    const totalPages = Math.ceil(displayedOptions.length / pageSize);

    const start = (pageNum -  1) * pageSize;
    const end = start + pageSize;
    const showItems = displayedOptions.slice(start, end);

    renderByTripType(showItems);

    renderPagination(currentPage, totalPages);

    const listEl = document.getElementById('resultList');
    if(listEl) listEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function goToPage(pageNum) {
    renderPage(pageNum); // 해당 페이지로 이동
}

function renderPagination(currentPage, totalPages) {
    const container = document.getElementById('paginationBox');
    const numbersContainer = document.getElementById('paginationNumbers');

    if (totalPages <= 0) {
        container.hidden = true;
        return;
    }
    container.hidden = false;

    const maxButtons = 5;
    const currentGroup = Math.ceil(currentPage / maxButtons);

    let startPage = (currentGroup - 1) * maxButtons + 1;
    let endPage = startPage + maxButtons - 1;
    if (endPage > totalPages) endPage = totalPages;

    const btnFirst = container.querySelector('[data-action="first"]');
    const btnPrev = container.querySelector('[data-action="prev"]');
    const btnNext = container.querySelector('[data-action="next"]');
    const btnLast = container.querySelector('[data-action="last"]');

    btnFirst.disabled = currentPage === 1;
    btnPrev.disabled = startPage === 1;
    btnNext.disabled = endPage >= totalPages;
    btnLast.disabled = currentPage === totalPages;

    btnFirst.onclick = () => goToPage(1);
    btnPrev.onclick = () => {
        const prevGroupStart = startPage - maxButtons;
        goToPage(prevGroupStart);
    };
    btnNext.onclick = () => {
        const nextGroupStart = startPage + maxButtons;
        goToPage(nextGroupStart);
    };
    btnLast.onclick = () => goToPage(totalPages);

    let html = '';
    for (let i = startPage; i <= endPage; i++) {
        if (i === currentPage) {
            html += `<button class="pg-btn active">${i}</button>`;
        } else {
            html += `<button class="pg-btn" onclick="goToPage(${i})">${i}</button>`;
        }
    }

    numbersContainer.innerHTML = html;
}