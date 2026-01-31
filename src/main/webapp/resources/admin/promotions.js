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

    // 클라이언트 사이드 페이지네이션용 상태
    const loadState = {
        flights: {
            isLoading: false,
            allData: null,       // 전체 데이터 (최대 100개)
            searchKey: null,     // 현재 검색 조건 (출발|도착)
            isTruncated: false   // 100개 제한으로 잘렸는지 여부
        },
        promotions: {
            isLoading: false,
            isLoaded: false,
            cachedData: null
        }
    };

    // 데이터 잘림 경고 표시
    function showTruncationWarning(loadedCount, totalCount) {
        // 기존 경고 제거
        const existingWarning = document.getElementById('truncation-warning');
        if (existingWarning) existingWarning.remove();

        // 경고 메시지 생성
        const warningDiv = document.createElement('div');
        warningDiv.id = 'truncation-warning';
        warningDiv.className = 'bg-amber-50 border border-amber-200 text-amber-800 px-4 py-3 rounded-lg mb-4 flex items-center gap-2';
        warningDiv.innerHTML = `
            <i data-lucide="alert-triangle" class="w-5 h-5 flex-shrink-0"></i>
            <span class="text-sm">
                검색 결과가 많아 <strong>${loadedCount}개</strong>만 표시됩니다.
                ${totalCount > loadedCount ? `(전체 약 ${totalCount}개)` : ''}
                더 정확한 결과를 위해 출발/도착 공항을 선택해주세요.
            </span>
        `;

        // 항공편 테이블 위에 삽입
        const flightSection = flightListBody.closest('.bg-white');
        if (flightSection) {
            flightSection.parentNode.insertBefore(warningDiv, flightSection);
            if (typeof lucide !== 'undefined') lucide.createIcons();
        }
    }

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

    // --- Skeleton UI Functions ---
    // 항공편 목록 스켈레톤 (4열)
    const renderFlightSkeleton = (count = 5) => {
        let html = '';
        for (let i = 0; i < count; i++) {
            html += `
                <tr class="border-b border-slate-100">
                    <td class="px-4 py-4">
                        <div class="h-4 skeleton-shimmer rounded w-20"></div>
                    </td>
                    <td class="px-4 py-4">
                        <div class="h-4 skeleton-shimmer rounded w-28"></div>
                    </td>
                    <td class="px-4 py-4">
                        <div class="h-4 skeleton-shimmer rounded w-32"></div>
                    </td>
                    <td class="px-4 py-4">
                        <div class="flex items-center space-x-2">
                            <div class="h-6 skeleton-shimmer rounded-full w-16"></div>
                            <div class="h-6 skeleton-shimmer rounded-full w-12"></div>
                            <div class="h-7 w-7 skeleton-shimmer rounded-full"></div>
                        </div>
                    </td>
                </tr>
            `;
        }
        flightListBody.innerHTML = html;
    };

    // 특가 상품 목록 스켈레톤 (7열)
    const renderPromotionSkeleton = (count = 8) => {
        let html = '';
        for (let i = 0; i < count; i++) {
            html += `
                <tr class="border-b border-slate-100">
                    <td class="px-2 py-4">
                        <div class="flex items-center gap-1">
                            <div class="h-4 w-4 skeleton-shimmer rounded"></div>
                            <div class="h-4 w-6 skeleton-shimmer rounded"></div>
                        </div>
                    </td>
                    <td class="px-4 py-4">
                        <div class="space-y-2">
                            <div class="h-4 skeleton-shimmer rounded w-32"></div>
                            <div class="h-3 skeleton-shimmer rounded w-24"></div>
                        </div>
                    </td>
                    <td class="px-4 py-4">
                        <div class="h-4 skeleton-shimmer rounded w-28"></div>
                    </td>
                    <td class="px-4 py-4">
                        <div class="h-4 skeleton-shimmer rounded w-12"></div>
                    </td>
                    <td class="px-4 py-4">
                        <div class="h-4 skeleton-shimmer rounded w-20"></div>
                    </td>
                    <td class="px-4 py-4 text-center">
                        <div class="h-6 w-11 skeleton-shimmer rounded-full mx-auto"></div>
                    </td>
                    <td class="px-4 py-4 text-center">
                        <div class="h-7 w-7 skeleton-shimmer rounded-full mx-auto"></div>
                    </td>
                </tr>
            `;
        }
        promotionListBody.innerHTML = html;
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
                            data-code="${a.airportId}" data-name="${a.city}">
                            ${a.city} (${a.airportId})
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

    function fetchFlights(page = 1, forceRefresh = false) {
        const departureAirport = state.from ? state.from.code : '';
        const arrivalAirport = state.to ? state.to.code : '';
        const searchKey = `${departureAirport}|${arrivalAirport}`;

        console.log('[Flights] 조회 요청:', { page, searchKey, hasData: !!loadState.flights.allData, forceRefresh });

        // 강제 새로고침 시 데이터 초기화
        if (forceRefresh) {
            loadState.flights.allData = null;
            loadState.flights.searchKey = null;
        }

        // 검색 조건이 바뀌면 데이터 초기화
        if (loadState.flights.searchKey !== searchKey) {
            loadState.flights.allData = null;
            loadState.flights.searchKey = searchKey;
            // 기존 경고 제거
            const existingWarning = document.getElementById('truncation-warning');
            if (existingWarning) existingWarning.remove();
        }

        // 이미 전체 데이터가 있으면 클라이언트에서 페이지네이션 (API 호출 안함!)
        if (loadState.flights.allData) {
            console.log('[Flights] 클라이언트 페이지네이션:', page);
            pagination.currentPage = page;
            renderFlightTableFromCache(page);
            renderFlightPagination();
            return;
        }

        // 로딩 중이면 스킵
        if (loadState.flights.isLoading) {
            console.log('[Flights] 이미 로딩 중, 스킵');
            return;
        }

        console.log('[Flights] API에서 전체 데이터 로드');

        // 전체 100개를 한 번에 로드
        const query = new URLSearchParams({
            departureAirport,
            arrivalAirport,
            page: 1,
            size: 100  // 전체 로드
        }).toString();

        loadState.flights.isLoading = true;
        renderFlightSkeleton(pagination.pageSize);

        fetch(`${window.CONTEXT_PATH}/admin/api/flights?${query}`)
            .then(res => res.json())
            .then(res => {
                if (res.success) {
                    // 전체 데이터 저장
                    loadState.flights.allData = res.data.list;
                    loadState.flights.searchKey = searchKey;

                    // 서버에서 반환한 실제 총 개수 vs 로드된 개수 비교
                    const loadedCount = res.data.list.length;
                    const serverTotalCount = res.data.totalCount || loadedCount;

                    // 100개 제한으로 인해 일부만 로드된 경우 경고 표시
                    if (serverTotalCount > loadedCount || loadedCount >= 100) {
                        loadState.flights.isTruncated = true;
                        console.warn(`[Flights] 데이터가 잘렸을 수 있음: 로드=${loadedCount}, 전체=${serverTotalCount}`);
                    } else {
                        loadState.flights.isTruncated = false;
                    }

                    // 페이지네이션 정보 계산 (로드된 데이터 기준)
                    pagination.totalCount = loadedCount;
                    pagination.totalPages = Math.ceil(loadedCount / pagination.pageSize);
                    pagination.currentPage = page;

                    // 현재 페이지 렌더링
                    renderFlightTableFromCache(page);
                    renderFlightPagination();

                    // 잘린 데이터 경고 표시
                    if (loadState.flights.isTruncated) {
                        showTruncationWarning(loadedCount, serverTotalCount);
                    }
                } else {
                    throw new Error(res.message);
                }
            })
            .catch(handleFetchError(flightListBody, 4))
            .finally(() => {
                loadState.flights.isLoading = false;
            });
    }

    // 캐시된 전체 데이터에서 현재 페이지만 추출하여 렌더링
    function renderFlightTableFromCache(page) {
        const startIndex = (page - 1) * pagination.pageSize;
        const endIndex = startIndex + pagination.pageSize;
        const pageData = loadState.flights.allData.slice(startIndex, endIndex);
        renderFlightTable(pageData);
    }

    function fetchPromotions(forceRefresh = false) {
        console.log('[Promotions] 조회 요청:', { cached: !!loadState.promotions.cachedData, forceRefresh });

        // 강제 새로고침 시 캐시 삭제
        if (forceRefresh) {
            loadState.promotions.cachedData = null;
            loadState.promotions.isLoaded = false;
        }

        // 캐시에 데이터가 있으면 바로 렌더링
        if (!forceRefresh && loadState.promotions.cachedData) {
            console.log('[Promotions] 캐시에서 로드');
            renderPromotionTable(loadState.promotions.cachedData);
            return;
        }

        // 로딩 중이면 스킵
        if (loadState.promotions.isLoading) {
            console.log('[Promotions] 이미 로딩 중, 스킵');
            return;
        }

        console.log('[Promotions] API 조회 실행');

        // 로딩 상태 설정
        loadState.promotions.isLoading = true;

        // 스켈레톤 UI 표시
        renderPromotionSkeleton(8);
        fetch(`${window.CONTEXT_PATH}/admin/promotions/api/list`)
            .then(res => res.json())
            .then(res => {
                if (res.success) {
                    renderPromotionTable(res.data.list);
                    // 캐시에 저장
                    loadState.promotions.cachedData = res.data.list;
                    loadState.promotions.isLoaded = true;
                } else {
                    throw new Error(res.message);
                }
            })
            .catch(handleFetchError(promotionListBody, 6))
            .finally(() => {
                loadState.promotions.isLoading = false;
            });
    }

    // --- Rendering ---
    function renderFlightTable(flights) {
        flightListBody.innerHTML = '';
        if (!flights || flights.length === 0) {
            flightListBody.innerHTML = `<tr><td colspan="4" class="text-center py-10 text-glass-muted">조회된 항공편이 없습니다.</td></tr>`;
            return;
        }
        flights.forEach(f => {
            const row = document.createElement('tr');
            row.className = 'hover:bg-white/5 transition-colors';
            const flightNumber = escapeHtml(f.flightNumber);
            const departureAirport = escapeHtml(f.departureAirport);
            const arrivalAirport = escapeHtml(f.arrivalAirport);
            const flightId = escapeHtml(f.flightId);
            const flightInfo = escapeHtml(`${f.flightNumber} (${f.departureAirport} → ${f.arrivalAirport})`);
            row.innerHTML = `
                <td class="px-4 py-3 text-sm font-medium text-glass-primary">${flightNumber}</td>
                <td class="px-4 py-3 text-sm text-glass-secondary">${departureAirport} → ${arrivalAirport}</td>
                <td class="px-4 py-3 text-sm text-glass-secondary">${formatDisplayDateTime(f.departureTime)}</td>
                <td class="px-4 py-3 text-sm font-medium flex items-center space-x-2">
                    <button class="make-promo-btn px-2 py-1 bg-blue-500/20 text-blue-400 text-xs font-bold rounded-full hover:bg-blue-500/30 transition-colors border border-blue-500/30" data-flight-id="${flightId}" data-flight-info="${flightInfo}">특가 만들기</button>
                    <button class="edit-flight-btn px-2 py-1 bg-white/10 text-glass-secondary text-xs font-bold rounded-full hover:bg-white/20 transition-colors border border-white/10" data-id="${flightId}">수정</button>
                    <button class="delete-flight-btn w-7 h-7 flex items-center justify-center rounded-full bg-red-500/20 text-red-400 hover:bg-red-500/30 transition-colors border border-red-500/30" data-id="${flightId}" title="삭제">
                        <i data-lucide="x" class="w-4 h-4 pointer-events-none"></i>
                    </button>
                </td>`;
            flightListBody.appendChild(row);
        });
        // Lucide 아이콘 재초기화
        if (typeof lucide !== 'undefined') {
            lucide.createIcons();
        }
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
        prevBtn.className = `px-3 py-1 rounded text-sm transition-colors ${pagination.currentPage === 1 ? 'bg-white/5 text-glass-muted cursor-not-allowed' : 'bg-white/10 text-glass-secondary hover:bg-white/20'}`;
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
            pageBtn.className = `px-3 py-1 rounded text-sm font-medium transition-colors ${i === pagination.currentPage ? 'bg-blue-500/80 text-white shadow-lg shadow-blue-500/30' : 'bg-white/10 text-glass-secondary hover:bg-white/20'}`;
            pageBtn.textContent = i;
            pageBtn.addEventListener('click', () => fetchFlights(i));
            paginationDiv.appendChild(pageBtn);
        }

        // 다음 버튼
        const nextBtn = document.createElement('button');
        nextBtn.className = `px-3 py-1 rounded text-sm transition-colors ${pagination.currentPage === pagination.totalPages ? 'bg-white/5 text-glass-muted cursor-not-allowed' : 'bg-white/10 text-glass-secondary hover:bg-white/20'}`;
        nextBtn.textContent = '다음';
        nextBtn.disabled = pagination.currentPage === pagination.totalPages;
        nextBtn.addEventListener('click', () => {
            if (pagination.currentPage < pagination.totalPages) fetchFlights(pagination.currentPage + 1);
        });
        paginationDiv.appendChild(nextBtn);

        // 총 건수 표시
        const totalInfo = document.createElement('span');
        totalInfo.className = 'ml-4 text-sm text-glass-muted';
        totalInfo.textContent = `총 ${pagination.totalCount}건`;
        paginationDiv.appendChild(totalInfo);

        // 테이블 부모에 추가
        flightListBody.closest('.overflow-x-auto').appendChild(paginationDiv);
    }

    function renderPromotionTable(promotions) {
        promotionListBody.innerHTML = '';
        if (!promotions || promotions.length === 0) {
            promotionListBody.innerHTML = `<tr><td colspan="7" class="text-center py-10 text-glass-muted">생성된 특가 상품이 없습니다.</td></tr>`;
            return;
        }
        promotions.forEach((p, index) => {
            const row = document.createElement('tr');
            row.dataset.id = p.promotionId;
            row.className = 'hover:bg-white/5 transition-colors';
            const title = escapeHtml(p.title);
            const departureAirportName = escapeHtml(p.departureAirportName || '');
            const arrivalAirportName = escapeHtml(p.arrivalAirportName || '');
            const promotionId = escapeHtml(p.promotionId);
            const passengerCount = parseInt(p.passengerCount, 10) || 0;
            const totalSalePrice = (p.totalSalePrice || 0).toLocaleString('ko-KR');
            const isActive = p.isActive === 'Y';
            row.innerHTML = `
                <td class="px-2 py-3">
                    <div class="drag-handle inline-flex items-center gap-1 px-2 py-1 rounded hover:bg-white/10 transition-colors cursor-grab" title="드래그하여 순서 변경">
                        <i data-lucide="grip-vertical" class="w-4 h-4 text-glass-muted"></i>
                        <span class="text-sm font-semibold text-glass-secondary">${index + 1}</span>
                    </div>
                </td>
                <td class="px-4 py-3 text-sm font-medium text-glass-primary">${title}</td>
                <td class="px-4 py-3 text-sm text-glass-secondary">${departureAirportName} → ${arrivalAirportName}</td>
                <td class="px-4 py-3 text-sm text-glass-secondary">${passengerCount}명</td>
                <td class="px-4 py-3 text-sm text-glass-primary font-semibold">₩${totalSalePrice}</td>
                <td class="px-4 py-3 text-center">
                    <button class="ios-toggle ${isActive ? 'active' : ''}" data-id="${promotionId}" data-active="${isActive ? 'Y' : 'N'}" title="${isActive ? '클릭하여 비활성화' : '클릭하여 활성화'}">
                        <span class="ios-toggle-thumb"></span>
                    </button>
                </td>
                <td class="px-4 py-3 text-center">
                    <button class="promo-delete-btn w-7 h-7 flex items-center justify-center rounded-full bg-red-500/20 text-red-400 hover:bg-red-500/30 transition-colors border border-red-500/30" data-id="${promotionId}" title="삭제">
                        <i data-lucide="x" class="w-4 h-4 pointer-events-none"></i>
                    </button>
                </td>`;
            promotionListBody.appendChild(row);
        });

        // Lucide 아이콘 재초기화
        if (typeof lucide !== 'undefined') {
            lucide.createIcons();
        }

        // SortableJS 초기화
        initSortable();
    }

    // --- Sortable (드래그 앤 드롭) ---
    let sortableInstance = null;

    function initSortable() {
        if (sortableInstance) {
            sortableInstance.destroy();
        }

        if (typeof Sortable === 'undefined') {
            console.warn('SortableJS not loaded');
            return;
        }

        sortableInstance = new Sortable(promotionListBody, {
            handle: '.drag-handle',
            animation: 150,
            ghostClass: 'sortable-ghost',
            chosenClass: 'sortable-chosen',
            onEnd: function(evt) {
                if (evt.oldIndex === evt.newIndex) return;

                // 새로운 순서로 displayOrder 업데이트
                const rows = promotionListBody.querySelectorAll('tr[data-id]');
                const updates = [];

                rows.forEach((row, index) => {
                    // UI 순서 번호 즉시 업데이트
                    const orderSpan = row.querySelector('.drag-handle span');
                    if (orderSpan) {
                        orderSpan.textContent = index + 1;
                    }

                    updates.push({
                        promotionId: row.dataset.id,
                        displayOrder: index + 1
                    });
                });

                // 서버에 순서 업데이트 요청
                updateDisplayOrders(updates);
            }
        });
    }

    function updateDisplayOrders(updates) {
        // 각 항목의 displayOrder를 개별적으로 업데이트
        const promises = updates.map(item =>
            fetch(`${window.CONTEXT_PATH}/admin/promotions/api/${item.promotionId}/order`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ displayOrder: item.displayOrder })
            })
        );

        Promise.all(promises)
            .then(() => {
                console.log('Display order updated');
            })
            .catch(err => {
                console.error('Failed to update display order:', err);
                alert('순서 변경에 실패했습니다.');
                fetchPromotions(true); // 실패 시 다시 로드
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
                        fetchFlights(pagination.currentPage, true);
                        fetchPromotions(true);
                    } else {
                        alert('항공편 삭제 실패: ' + res.message);
                    }
                });
            }
        }
    });

    promotionListBody.addEventListener('click', e => {
        const target = e.target.closest('.ios-toggle, .promo-delete-btn');
        if (!target) return;

        const id = target.dataset.id;

        // iOS 토글 버튼 클릭
        if (target.classList.contains('ios-toggle')) {
            const currentActive = target.dataset.active === 'Y';
            const newActive = !currentActive;

            // UI 즉시 업데이트 (낙관적 업데이트)
            target.dataset.active = newActive ? 'Y' : 'N';
            target.classList.toggle('active', newActive);
            target.title = newActive ? '클릭하여 비활성화' : '클릭하여 활성화';

            // 서버에 상태 변경 요청
            fetch(`${window.CONTEXT_PATH}/admin/promotions/api/${id}/toggle`, { method: 'POST' })
                .then(res => res.json())
                .then(res => {
                    if (!res.success) {
                        // 실패 시 롤백
                        alert('상태 변경 실패: ' + res.message);
                        fetchPromotions(true);
                    }
                })
                .catch(err => {
                    console.error('Toggle failed:', err);
                    fetchPromotions(true);
                });
        }

        // 삭제 버튼 클릭
        if (target.classList.contains('promo-delete-btn')) {
            if (confirm('정말 이 특가 상품을 삭제하시겠습니까?')) {
                fetch(`${window.CONTEXT_PATH}/admin/promotions/api/${id}`, { method: 'DELETE' }).then(res => res.json()).then(res => {
                    if (res.success) {
                        alert('삭제되었습니다.');
                        fetchPromotions(true);
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
                fetchPromotions(true);
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
                fetchFlights(pagination.currentPage, true);
            } else {
                alert('저장 실패: ' + res.message);
            }
        });
    });

    // --- Initial Load ---
    initPage();
});
