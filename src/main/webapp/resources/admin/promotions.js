document.addEventListener('DOMContentLoaded', function() {
    // --- Initial Setup ---
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }

    let allAirports = [];
    const state = {
        from: null,  // { code, name }
        to: null     // { code, name }
    };
    const pagination = {
        currentPage: 1,
        pageSize: 10,
        totalCount: 0,
        totalPages: 0
    };

    // --- Element Selectors ---
    const promotionModal = document.getElementById('promotion-modal');
    const flightCrudModal = document.getElementById('flight-crud-modal');
    const flightListBody = document.getElementById('flight-list-body');
    const promotionListBody = document.getElementById('promotion-list-body');
    const flightFilterSearchBtn = document.getElementById('search-flights-btn');
    const addFlightBtn = document.getElementById('add-flight-btn');
    const promotionForm = document.getElementById('promotion-form');
    const flightForm = document.getElementById('flight-form');

    // --- Utility Functions ---
    // XSS 방지용 HTML 이스케이프
    const escapeHtml = (str) => {
        if (str == null) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    };

    const handleFetchError = (targetBody, colspan) => (err) => {
        console.error("Failed to fetch data:", err);
        targetBody.innerHTML = `<tr><td colspan="${colspan}" class="text-center py-10 text-red-500">데이터를 불러오는 데 실패했습니다.</td></tr>`;
    };
    // LocalDateTime이 배열([2026,2,1,9,0]) 또는 ISO 문자열로 올 수 있음
    const formatDisplayDateTime = (dateValue) => {
        if (!dateValue) return '';
        let date;
        if (Array.isArray(dateValue)) {
            // [year, month, day, hour, minute, second?]
            const [year, month, day, hour = 0, minute = 0] = dateValue;
            date = new Date(year, month - 1, day, hour, minute);
        } else {
            date = new Date(dateValue);
        }
        if (isNaN(date.getTime())) return '';
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    };
    const formatDateTimeForInput = (dateValue) => {
        if (!dateValue) return '';
        if (Array.isArray(dateValue)) {
            const [year, month, day, hour = 0, minute = 0] = dateValue;
            return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}T${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
        }
        return dateValue.substring(0, 16);
    };

    // --- Dropdown Logic ---
    function initDropdowns() {
        document.querySelectorAll('.filter-dropdown').forEach(dd => {
            const toggle = dd.querySelector('.dropdown-toggle');
            const panel = dd.querySelector('.dropdown-panel');
            const searchInput = dd.querySelector('.dropdown-search');

            toggle.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();

                // Close other dropdowns
                document.querySelectorAll('.filter-dropdown').forEach(other => {
                    if (other !== dd) other.classList.remove('open');
                });

                // Toggle this dropdown
                dd.classList.toggle('open');

                if (dd.classList.contains('open') && searchInput) {
                    searchInput.focus();
                    searchInput.value = '';
                    renderAirportList(dd, allAirports);
                }
            });
        });

        // Close dropdowns when clicking outside
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.filter-dropdown')) {
                document.querySelectorAll('.filter-dropdown').forEach(dd => {
                    dd.classList.remove('open');
                });
            }
        });
    }

    function renderAirportList(dropdown, airports) {
        const ul = dropdown.querySelector('[data-list]');
        if (!ul) return;

        if (airports.length === 0) {
            ul.innerHTML = '<li class="p-3 text-sm text-slate-500">결과 없음</li>';
            return;
        }

        // Group by country
        const grouped = airports.reduce((acc, a) => {
            const country = a.country || '기타';
            if (!acc[country]) acc[country] = [];
            acc[country].push(a);
            return acc;
        }, {});

        ul.innerHTML = Object.entries(grouped).map(([country, list]) => `
            <li class="country-group">
                <div class="px-3 py-1 text-xs font-semibold text-slate-400 bg-slate-50">${country}</div>
                <ul>
                    ${list.map(a => `
                        <li class="airport-item px-3 py-2 hover:bg-blue-50 cursor-pointer text-sm"
                            data-code="${escapeHtml(a.airportId)}" data-name="${escapeHtml(a.city)}">
                            ${escapeHtml(a.city)} (${escapeHtml(a.airportId)})
                        </li>
                    `).join('')}
                </ul>
            </li>
        `).join('');
    }

    function setupAirportDropdown(fieldName) {
        const dd = document.querySelector(`.filter-dropdown[data-field="${fieldName}"]`);
        if (!dd) return;

        const searchInput = dd.querySelector('.dropdown-search');
        const ul = dd.querySelector('[data-list]');

        // Search filtering
        searchInput.addEventListener('input', () => {
            const query = searchInput.value.toLowerCase().trim();
            const filtered = allAirports.filter(a =>
                a.city.toLowerCase().includes(query) ||
                a.airportId.toLowerCase().includes(query) ||
                (a.country && a.country.toLowerCase().includes(query))
            );
            renderAirportList(dd, filtered);
        });

        // Selection
        ul.addEventListener('click', (e) => {
            const item = e.target.closest('.airport-item');
            if (!item) return;

            const code = item.dataset.code;
            const name = item.dataset.name;

            state[fieldName] = { code, name };
            dd.querySelector('[data-value]').textContent = `${name} (${code})`;
            dd.classList.remove('open');
        });
    }

    // --- Data Fetching ---
    async function initPage() {
        try {
            const res = await fetch(`${window.CONTEXT_PATH}/admin/api/utils/airports`);
            const json = await res.json();
            if (json.success) {
                allAirports = json.data;
                initDropdowns();
                setupAirportDropdown('from');
                setupAirportDropdown('to');
            } else {
                throw new Error(json.message);
            }
            fetchFlights();
            fetchPromotions();
        } catch (err) {
            console.error("Failed to initialize page data:", err);
            flightListBody.innerHTML = `<tr><td colspan="4" class="text-center py-10 text-red-500">페이지 초기화 실패: ${escapeHtml(err.message)}</td></tr>`;
        }
    }

    function fetchFlights(page = 1) {
        pagination.currentPage = page;
        const departureAirport = state.from ? state.from.code : '';
        const arrivalAirport = state.to ? state.to.code : '';
        const query = new URLSearchParams({
            departureAirport,
            arrivalAirport,
            page: pagination.currentPage,
            size: pagination.pageSize
        }).toString();

        flightListBody.innerHTML = `<tr><td colspan="4" class="text-center py-10 text-slate-500">항공편을 조회 중입니다...</td></tr>`;
        fetch(`${window.CONTEXT_PATH}/admin/api/flights?${query}`)
            .then(res => res.json())
            .then(res => {
                if (res.success) {
                    pagination.totalCount = res.data.totalCount;
                    pagination.totalPages = res.data.totalPages;
                    renderFlightTable(res.data.list);
                    renderFlightPagination();
                } else {
                    throw new Error(res.message);
                }
            })
            .catch(handleFetchError(flightListBody, 4));
    }

    function fetchPromotions() {
        promotionListBody.innerHTML = `<tr><td colspan="6" class="text-center py-10 text-slate-500">특가 상품을 조회 중입니다...</td></tr>`;
        fetch(`${window.CONTEXT_PATH}/admin/promotions/api/list`)
            .then(res => res.json())
            .then(res => {
                if (res.success) {
                    renderPromotionTable(res.data.list);
                } else {
                    throw new Error(res.message);
                }
            })
            .catch(handleFetchError(promotionListBody, 6));
    }

    // --- Rendering ---
    function renderFlightTable(flights) {
        flightListBody.innerHTML = '';
        if (!flights || flights.length === 0) {
            flightListBody.innerHTML = `<tr><td colspan="4" class="text-center py-10 text-slate-500">조회된 항공편이 없습니다.</td></tr>`;
            return;
        }
        flights.forEach(f => {
            const row = document.createElement('tr');
            // 변수에 escapeHtml 적용
            row.innerHTML = `
                <td class="px-4 py-3 text-sm font-medium text-slate-900">${escapeHtml(f.flightNumber)}</td>
                <td class="px-4 py-3 text-sm text-slate-500">${escapeHtml(f.departureAirport)} → ${escapeHtml(f.arrivalAirport)}</td>
                <td class="px-4 py-3 text-sm text-slate-500">${formatDisplayDateTime(f.departureTime)}</td>
                <td class="px-4 py-3 text-sm font-medium flex space-x-2">
                    <button class="make-promo-btn px-2 py-1 bg-blue-100 text-blue-700 text-xs font-bold rounded-full hover:bg-blue-200" 
                        data-flight-id="${escapeHtml(f.flightId)}" 
                        data-flight-info="${escapeHtml(f.flightNumber)} (${escapeHtml(f.departureAirport)} → ${escapeHtml(f.arrivalAirport)})">특가 만들기</button>
                    <button class="edit-flight-btn px-2 py-1 bg-gray-100 text-gray-700 text-xs font-bold rounded-full hover:bg-gray-200" 
                        data-id="${escapeHtml(f.flightId)}">수정</button>
                    <button class="delete-flight-btn px-2 py-1 bg-red-100 text-red-700 text-xs font-bold rounded-full hover:bg-red-200" 
                        data-id="${escapeHtml(f.flightId)}">삭제</button>
                </td>`;
            flightListBody.appendChild(row);
        });
    }

    function renderFlightPagination() {
        // 기존 페이지네이션 제거
        const existingPagination = document.getElementById('flight-pagination');
        if (existingPagination) existingPagination.remove();

        if (pagination.totalPages <= 1) return;

        const paginationDiv = document.createElement('div');
        paginationDiv.id = 'flight-pagination';
        paginationDiv.className = 'flex items-center justify-center gap-2 mt-4 pb-4';

        // 이전 버튼
        const prevBtn = document.createElement('button');
        prevBtn.className = `px-3 py-1 rounded text-sm ${pagination.currentPage === 1 ? 'bg-slate-100 text-slate-400 cursor-not-allowed' : 'bg-slate-200 text-slate-700 hover:bg-slate-300'}`;
        prevBtn.textContent = '이전';
        prevBtn.disabled = pagination.currentPage === 1;
        prevBtn.addEventListener('click', () => {
            if (pagination.currentPage > 1) fetchFlights(pagination.currentPage - 1);
        });
        paginationDiv.appendChild(prevBtn);

        // 페이지 번호
        const startPage = Math.max(1, pagination.currentPage - 2);
        const endPage = Math.min(pagination.totalPages, startPage + 4);

        for (let i = startPage; i <= endPage; i++) {
            const pageBtn = document.createElement('button');
            pageBtn.className = `px-3 py-1 rounded text-sm font-medium ${i === pagination.currentPage ? 'bg-blue-500 text-white' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'}`;
            pageBtn.textContent = i;
            pageBtn.addEventListener('click', () => fetchFlights(i));
            paginationDiv.appendChild(pageBtn);
        }

        // 다음 버튼
        const nextBtn = document.createElement('button');
        nextBtn.className = `px-3 py-1 rounded text-sm ${pagination.currentPage === pagination.totalPages ? 'bg-slate-100 text-slate-400 cursor-not-allowed' : 'bg-slate-200 text-slate-700 hover:bg-slate-300'}`;
        nextBtn.textContent = '다음';
        nextBtn.disabled = pagination.currentPage === pagination.totalPages;
        nextBtn.addEventListener('click', () => {
            if (pagination.currentPage < pagination.totalPages) fetchFlights(pagination.currentPage + 1);
        });
        paginationDiv.appendChild(nextBtn);

        // 총 건수 표시
        const totalInfo = document.createElement('span');
        totalInfo.className = 'ml-4 text-sm text-slate-500';
        totalInfo.textContent = `총 ${pagination.totalCount}건`;
        paginationDiv.appendChild(totalInfo);

        // 테이블 부모에 추가
        flightListBody.closest('.overflow-x-auto').appendChild(paginationDiv);
    }

    function renderPromotionTable(promotions) {
        promotionListBody.innerHTML = '';
        if (!promotions || promotions.length === 0) {
            promotionListBody.innerHTML = `<tr><td colspan="6" class="text-center py-10 text-slate-500">생성된 특가 상품이 없습니다.</td></tr>`;
            return;
        }
        promotions.forEach(p => {
            const row = document.createElement('tr');
            const totalSalePrice = (p.totalSalePrice || 0).toLocaleString('ko-KR');

            // 변수에 escapeHtml 적용
            row.innerHTML = `
                <td class="px-4 py-3 text-sm font-medium text-slate-900">${escapeHtml(p.title)}</td>
                <td class="px-4 py-3 text-sm text-slate-500">${escapeHtml(p.departureAirportName || '')} → ${escapeHtml(p.arrivalAirportName || '')}</td>
                <td class="px-4 py-3 text-sm text-slate-500">${escapeHtml(p.passengerCount)}명</td>
                <td class="px-4 py-3 text-sm text-slate-800 font-semibold">₩${escapeHtml(totalSalePrice)}</td>
                <td class="px-4 py-3 text-sm"><span class="px-2 py-1 text-xs font-medium rounded-full ${p.isActive === 'Y' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">${p.isActive === 'Y' ? '활성' : '비활성'}</span></td>
                <td class="px-4 py-3 text-sm font-medium"><button class="promo-delete-btn text-red-600 hover:text-red-800" data-id="${escapeHtml(p.promotionId)}">삭제</button></td>`;
            promotionListBody.appendChild(row);
        });
    }

    // --- Event Listeners & Form Handlers ---
    // 모달 닫기 함수
    const closeModal = (modal) => {
        if (modal) modal.classList.add('hidden');
    };

    // 모달 닫기 버튼 (모달 ID로 직접 찾기)
    document.querySelectorAll('.modal-close-btn').forEach(btn => btn.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        const modal = e.currentTarget.closest('#flight-crud-modal, #promotion-modal');
        closeModal(modal);
    }));

    // 모달 배경 클릭 시 닫기
    [promotionModal, flightCrudModal].forEach(modal => {
        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    closeModal(modal);
                }
            });
        }
    });

    flightFilterSearchBtn.addEventListener('click', () => fetchFlights(1));

    // 모달 열 때 lucide 아이콘 재초기화
    const initModalIcons = () => {
        if (typeof lucide !== 'undefined') {
            setTimeout(() => lucide.createIcons(), 50);
        }
    };

    // 라디오 버튼 값 설정 헬퍼
    const setRadioValue = (name, value) => {
        const radio = flightForm.querySelector(`input[name="${name}"][value="${value || ''}"]`);
        if (radio) radio.checked = true;
    };

    addFlightBtn.addEventListener('click', () => {
        flightForm.reset();
        flightForm.querySelector('#flight-crud-id').value = '';
        setRadioValue('routeType', 'DOMESTIC');
        setRadioValue('terminalNo', '');
        flightCrudModal.querySelector('#flight-modal-title').textContent = '새 항공편 등록';
        flightCrudModal.classList.remove('hidden');
        initModalIcons();
    });

    flightListBody.addEventListener('click', e => {
        const target = e.target;
        const flightId = target.dataset.id || target.dataset.flightId;

        if (target.classList.contains('make-promo-btn')) {
            promotionForm.reset();
            promotionForm.querySelector('#flightId').value = target.dataset.flightId;
            promotionModal.querySelector('#modal-flight-info').textContent = target.dataset.flightInfo;
            promotionModal.classList.remove('hidden');
            initModalIcons();
        } else if (target.classList.contains('edit-flight-btn')) {
            fetch(`${window.CONTEXT_PATH}/admin/api/flights/${flightId}`).then(res => res.json()).then(res => {
                if (res.success) {
                    const f = res.data;
                    flightForm.reset();
                    flightForm.querySelector('#flight-crud-id').value = f.flightId;
                    flightForm.querySelector('#flightNumber').value = f.flightNumber;
                    flightForm.querySelector('#departureAirport').value = f.departureAirport;
                    flightForm.querySelector('#arrivalAirport').value = f.arrivalAirport;
                    flightForm.querySelector('#departureTime').value = formatDateTimeForInput(f.departureTime);
                    flightForm.querySelector('#arrivalTime').value = formatDateTimeForInput(f.arrivalTime);
                    setRadioValue('terminalNo', f.terminalNo || '');
                    setRadioValue('routeType', f.routeType || 'DOMESTIC');
                    flightCrudModal.querySelector('#flight-modal-title').textContent = '항공편 수정';
                    flightCrudModal.classList.remove('hidden');
                    initModalIcons();
                } else {
                    alert('항공편 정보 로딩 실패: ' + res.message);
                }
            });
        } else if (target.classList.contains('delete-flight-btn')) {
            if (confirm('정말 이 항공편을 삭제하시겠습니까?')) {
                fetch(`${window.CONTEXT_PATH}/admin/api/flights/${flightId}`, { method: 'DELETE' }).then(res => res.json()).then(res => {
                    if (res.success) {
                        alert('항공편이 삭제되었습니다.');
                        fetchFlights();
                        fetchPromotions();
                    } else {
                        alert('항공편 삭제 실패: ' + res.message);
                    }
                });
            }
        }
    });

    promotionListBody.addEventListener('click', e => {
        if (e.target.classList.contains('promo-delete-btn')) {
            const id = e.target.dataset.id;
            if (confirm('정말 이 특가 상품을 삭제하시겠습니까?')) {
                fetch(`${window.CONTEXT_PATH}/admin/promotions/api/${id}`, { method: 'DELETE' }).then(res => res.json()).then(res => {
                    if (res.success) {
                        alert('삭제되었습니다.');
                        fetchPromotions();
                    } else {
                        alert('삭제 실패: ' + res.message);
                    }
                });
            }
        }
    });

    promotionForm.addEventListener('submit', e => {
        e.preventDefault();
        const data = Object.fromEntries(new FormData(e.target).entries());
        data.passengerCount = parseInt(data.passengerCount, 10);
        data.discountPercentage = parseInt(data.discountPercentage, 10);

        fetch(`${window.CONTEXT_PATH}/admin/promotions/api`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        }).then(res => res.json()).then(res => {
            if (res.success) {
                alert('특가 상품이 생성되었습니다.');
                promotionModal.classList.add('hidden');
                fetchPromotions();
            } else {
                alert('생성 실패: ' + res.message);
            }
        });
    });

    flightForm.addEventListener('submit', e => {
        e.preventDefault();
        const data = Object.fromEntries(new FormData(e.target).entries());
        const flightId = data.flightId;
        const url = flightId ? `${window.CONTEXT_PATH}/admin/api/flights/${flightId}` : `${window.CONTEXT_PATH}/admin/api/flights`;
        const method = flightId ? 'PUT' : 'POST';

        fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        }).then(res => res.json()).then(res => {
            if (res.success) {
                alert('항공편이 저장되었습니다.');
                flightCrudModal.classList.add('hidden');
                fetchFlights();
            } else {
                alert('저장 실패: ' + res.message);
            }
        });
    });

    // --- Initial Load ---
    initPage();
});
