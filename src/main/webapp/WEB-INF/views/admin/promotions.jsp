<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="layout/head.jsp" %>
<%@ include file="layout/sidebar.jsp" %>
<%@ include file="layout/topbar.jsp" %>

<main class="pl-64 pt-16 min-h-screen">
    <div class="p-8 max-w-[1600px] mx-auto space-y-8">
        <!-- Header -->
        <div class="flex items-center justify-between">
            <div>
                <h1 class="text-2xl font-bold text-slate-900">항공편 및 특가 관리</h1>
                <p class="text-slate-500">항공편을 조회하고, 특가를 생성하거나 항공편 정보를 관리합니다.</p>
            </div>
            <button id="add-flight-btn" class="px-4 py-2 bg-green-600 text-white text-sm font-bold rounded-lg shadow-lg shadow-green-500/20 hover:bg-green-700 transition-colors">
                <i data-lucide="plus" class="w-4 h-4 mr-2 inline-block"></i> 새 항공편 등록
            </button>
        </div>

        <!-- Top Panel: Flight Management -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm">
            <div class="p-6 border-b border-slate-100 flex items-center justify-between">
                <h2 class="text-lg font-bold text-slate-800">항공편 목록</h2>
                <div id="flight-filters" class="flex items-center space-x-2">
                    <input type="text" id="filter-flightNumber" placeholder="항공편 번호" class="p-2 border border-slate-300 rounded-lg text-sm w-32">
                    <input type="text" id="filter-departureAirport" placeholder="출발 공항" class="p-2 border border-slate-300 rounded-lg text-sm w-28">
                    <input type="text" id="filter-arrivalAirport" placeholder="도착 공항" class="p-2 border border-slate-300 rounded-lg text-sm w-28">
                    <button id="search-flights-btn" class="px-3 py-2 bg-blue-500 text-white rounded-lg text-sm hover:bg-blue-600">
                        <i data-lucide="search" class="w-4 h-4 inline-block mr-1"></i> 검색
                    </button>
                </div>
            </div>
            <div class="p-6">
                <div class="overflow-x-auto">
                    <table class="min-w-full divide-y divide-slate-200">
                        <thead class="bg-slate-50">
                        <tr>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">항공편 번호</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">경로</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">출발 시각</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">관리</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase"></th>
                        </tr>
                        </thead>
                        <tbody id="flight-list-body" class="bg-white divide-y divide-slate-100">
                        <!-- Flight data will be rendered here -->
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Bottom Panel: Promotion Management -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm">
            <div class="p-6 border-b border-slate-100">
                <h2 class="text-lg font-bold text-slate-800">생성된 특가 상품 목록</h2>
            </div>
            <div class="p-6">
                <div class="overflow-x-auto">
                    <table class="min-w-full divide-y divide-slate-200">
                        <thead class="bg-slate-50">
                        <tr>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">제목</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">항공편</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">인원</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">할인가(총)</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">상태</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">관리</th>
                        </tr>
                        </thead>
                        <tbody id="promotion-list-body" class="bg-white divide-y divide-slate-100">
                        <!-- Promotion data will be rendered here -->
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</main>

<!-- Modal for Add/Edit Promotion -->
<div id="promotion-modal" class="fixed inset-0 z-50 hidden bg-black/50 backdrop-blur-sm">
    <div class="fixed inset-0 flex items-center justify-center p-4">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg">
            <form id="promotion-form">
                <input type="hidden" id="flightId" name="flightId" required>
                <div class="p-6 border-b">
                    <h2 id="modal-title" class="text-lg font-bold">새 특가 상품 만들기</h2>
                </div>
                <div class="p-6 space-y-4">
                    <div>
                        <label class="block text-sm font-medium text-slate-700">대상 항공편</label>
                        <p id="modal-flight-info" class="mt-1 text-sm text-slate-900 font-semibold"></p>
                    </div>
                    <div>
                        <label for="title" class="block text-sm font-medium text-slate-700">프로모션 제목</label>
                        <input type="text" id="title" name="title" required placeholder="예: 4인 가족 제주 특가" class="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
                    </div>
                    <div>
                        <label for="passengerCount" class="block text-sm font-medium text-slate-700">인원수</label>
                        <input type="number" id="passengerCount" name="passengerCount" required class="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" min="1" value="1">
                    </div>
                    <div>
                        <label for="discountPercentage" class="block text-sm font-medium text-slate-700">할인율 (%)</label>
                        <input type="number" id="discountPercentage" name="discountPercentage" required class="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" min="0" max="100">
                    </div>
                    <div>
                        <label for="tags" class="block text-sm font-medium text-slate-700">태그 (콤마로 구분)</label>
                        <input type="text" id="tags" name="tags" class="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
                    </div>
                </div>
                <div class="p-6 bg-slate-50 rounded-b-2xl flex justify-end gap-3">
                    <button type="button" id="modal-close-btn" class="px-4 py-2 rounded-lg bg-white border border-slate-300 text-sm font-medium hover:bg-slate-50">취소</button>
                    <button type="submit" class="px-4 py-2 rounded-lg bg-blue-600 text-white text-sm font-medium hover:bg-blue-700">저장</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal for Flight CRUD -->
<div id="flight-crud-modal" class="fixed inset-0 z-50 hidden bg-black/50 backdrop-blur-sm">
    <div class="fixed inset-0 flex items-center justify-center p-4">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg">
            <form id="flight-form">
                <input type="hidden" id="flight-crud-id" name="flightId">
                <div class="p-6 border-b">
                    <h2 id="flight-modal-title" class="text-lg font-bold">새 항공편 등록</h2>
                </div>
                <div class="p-6 space-y-4">
                    <div>
                        <label for="flightNumber" class="block text-sm font-medium text-slate-700">항공편 번호</label>
                        <input type="text" id="flightNumber" name="flightNumber" required class="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
                    </div>
                    <div>
                        <label for="departureAirport" class="block text-sm font-medium text-slate-700">출발 공항</label>
                        <input type="text" id="departureAirport" name="departureAirport" required class="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
                    </div>
                    <div>
                        <label for="arrivalAirport" class="block text-sm font-medium text-slate-700">도착 공항</label>
                        <input type="text" id="arrivalAirport" name="arrivalAirport" required class="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
                    </div>
                    <div>
                        <label for="departureTime" class="block text-sm font-medium text-slate-700">출발 시각</label>
                        <input type="datetime-local" id="departureTime" name="departureTime" required class="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
                    </div>
                    <div>
                        <label for="arrivalTime" class="block text-sm font-medium text-slate-700">도착 시각</label>
                        <input type="datetime-local" id="arrivalTime" name="arrivalTime" required class="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
                    </div>
                    <div>
                        <label for="terminalNo" class="block text-sm font-medium text-slate-700">터미널 번호</label>
                        <input type="text" id="terminalNo" name="terminalNo" class="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
                    </div>
                    <div>
                        <label for="routeType" class="block text-sm font-medium text-slate-700">노선 타입</label>
                        <select id="routeType" name="routeType" required class="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
                            <option value="DOMESTIC">DOMESTIC</option>
                            <option value="INTERNATIONAL">INTERNATIONAL</option>
                        </select>
                    </div>
                </div>
                <div class="p-6 bg-slate-50 rounded-b-2xl flex justify-end gap-3">
                    <button type="button" id="flight-modal-close-btn" class="px-4 py-2 rounded-lg bg-white border border-slate-300 text-sm font-medium hover:bg-slate-50">취소</button>
                    <button type="submit" class="px-4 py-2 rounded-lg bg-blue-600 text-white text-sm font-medium hover:bg-blue-700">저장</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        lucide.createIcons();

        // --- Common Elements ---
        // Promotion Modal
        const promotionModal = document.getElementById('promotion-modal');
        const promotionModalCloseBtn = promotionModal.querySelector('#modal-close-btn');
        const promotionForm = promotionModal.querySelector('#promotion-form');
        const promotionModalTitle = promotionModal.querySelector('#modal-title');
        const modalFlightInfo = promotionModal.querySelector('#modal-flight-info');
        const promotionFlightIdInput = promotionModal.querySelector('#flightId');

        // Flight CRUD Modal
        const flightCrudModal = document.getElementById('flight-crud-modal');
        const flightCrudModalCloseBtn = flightCrudModal.querySelector('#flight-modal-close-btn');
        const flightForm = flightCrudModal.querySelector('#flight-form');
        const flightModalTitle = flightCrudModal.querySelector('#flight-modal-title');
        const flightCrudIdInput = flightCrudModal.querySelector('#flight-crud-id');

        // Top Panel: Flights List
        const flightListBody = document.getElementById('flight-list-body');
        const flightFilterSearchBtn = document.getElementById('search-flights-btn');
        const addFlightBtn = document.getElementById('add-flight-btn');

        // Bottom Panel: Promotions List
        const promotionListBody = document.getElementById('promotion-list-body');

        // --- Modal Management ---
        const openPromotionModal = () => promotionModal.classList.remove('hidden');
        const closePromotionModal = () => promotionModal.classList.add('hidden');
        promotionModalCloseBtn.addEventListener('click', closePromotionModal);

        const openFlightCrudModal = () => flightCrudModal.classList.remove('hidden');
        const closeFlightCrudModal = () => flightCrudModal.classList.add('hidden');
        flightCrudModalCloseBtn.addEventListener('click', closeFlightCrudModal);

        // --- Utility Functions ---
        function handleFetchError(targetBody, colspan) {
            return (err) => {
                console.error("Failed to fetch data:", err);
                targetBody.innerHTML = `<tr><td colspan="\${colspan}" class="text-center py-10 text-red-500">데이터를 불러오는 데 실패했습니다.</td></tr>`;
            }
        }

        function formatDateTimeForInput(isoString) {
            if (!isoString) return '';
            // "2026-01-27T10:30:00" -> "2026-01-27T10:30" for datetime-local input
            return isoString.substring(0, 16);
        }

        function formatDisplayDateTime(isoString) {
            if (!isoString) return '';
            const date = new Date(isoString);
            return `\${date.getFullYear()}-\${String(date.getMonth() + 1).padStart(2, '0')}-\${String(date.getDate()).padStart(2, '0')} \${String(date.getHours()).padStart(2, '0')}:\${String(date.getMinutes()).padStart(2, '0')}`;
        }

        // --- Data Fetching ---
        function fetchFlights() {
            const flightNumber = document.getElementById('filter-flightNumber').value;
            const departureAirport = document.getElementById('filter-departureAirport').value;
            const arrivalAirport = document.getElementById('filter-arrivalAirport').value;
            const query = new URLSearchParams({ flightNumber, departureAirport, arrivalAirport }).toString();

            flightListBody.innerHTML = '<tr><td colspan="5" class="text-center py-10 text-slate-500">항공편을 조회 중입니다...</td></tr>';
            fetch(`\${pageContext.request.contextPath}/admin/api/flights?\${query}`) // Use AdminFlightController
                .then(res => res.json())
                .then(res => renderFlightTable(res.data))
                .catch(handleFetchError(flightListBody, 5));
        }

        function fetchPromotions() {
            promotionListBody.innerHTML = '<tr><td colspan="6" class="text-center py-10 text-slate-500">특가 상품을 조회 중입니다...</td></tr>';
            fetch('${pageContext.request.contextPath}/admin/promotions/api/list')
                .then(res => res.json())
                .then(res => renderPromotionTable(res.data.list)) // Assuming res.data contains {list:[], totalCount:...}
                .catch(handleFetchError(promotionListBody, 6));
        }

        // --- Rendering ---
        function renderFlightTable(flights) {
            flightListBody.innerHTML = '';
            if (!flights || flights.length === 0) {
                flightListBody.innerHTML = '<tr><td colspan="5" class="text-center py-10 text-slate-500">조회된 항공편이 없습니다.</td></tr>';
                return;
            }
            flights.forEach(f => {
                const row = document.createElement('tr');
                row.innerHTML = `
                <td class="px-4 py-3 text-sm font-medium text-slate-900">\${f.flightNumber}</td>
                <td class="px-4 py-3 text-sm text-slate-500">\${f.departureAirport} → \${f.arrivalAirport}</td>
                <td class="px-4 py-3 text-sm text-slate-500">\${formatDisplayDateTime(f.departureTime)}</td>
                <td class="px-4 py-3 text-sm font-medium flex space-x-2">
                    <button class="make-promo-btn px-2 py-1 bg-blue-100 text-blue-700 text-xs font-bold rounded-full hover:bg-blue-200"
                            data-flight-id="\${f.flightId}"
                            data-flight-info="\${f.flightNumber} (\${f.departureAirport} → \${f.arrivalAirport})">
                        특가 만들기
                    </button>
                    <button class="edit-flight-btn px-2 py-1 bg-gray-100 text-gray-700 text-xs font-bold rounded-full hover:bg-gray-200"
                            data-id="\${f.flightId}">
                        수정
                    </button>
                    <button class="delete-flight-btn px-2 py-1 bg-red-100 text-red-700 text-xs font-bold rounded-full hover:bg-red-200"
                            data-id="\${f.flightId}">
                        삭제
                    </button>
                </td>
                <td class="px-4 py-3 text-sm font-medium"></td>
            `;
                flightListBody.appendChild(row);
            });
        }

        function renderPromotionTable(promotions) {
            promotionListBody.innerHTML = '';
            if (!promotions || promotions.length === 0) {
                promotionListBody.innerHTML = '<tr><td colspan="6" class="text-center py-10 text-slate-500">생성된 특가 상품이 없습니다.</td></tr>';
                return;
            }
            promotions.forEach(p => {
                const row = document.createElement('tr');
                const totalSalePrice = p.totalSalePrice.toLocaleString('ko-KR');
                row.innerHTML = `
                <td class="px-4 py-3 text-sm font-medium text-slate-900">\${p.title}</td>
                <td class="px-4 py-3 text-sm text-slate-500">\${p.departureAirportName} → \${p.arrivalAirportName}</td>
                <td class="px-4 py-3 text-sm text-slate-500">\${p.passengerCount}명</td>
                <td class="px-4 py-3 text-sm text-slate-800 font-semibold">₩\${totalSalePrice}</td>
                <td class="px-4 py-3 text-sm"><span class="px-2 py-1 text-xs font-medium rounded-full \${p.isActive === 'Y' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">\${p.isActive === 'Y' ? '활성' : '비활성'}</span></td>
                <td class="px-4 py-3 text-sm font-medium flex space-x-2">
                     <button class="promo-delete-btn px-2 py-1 bg-red-100 text-red-700 text-xs font-bold rounded-full hover:bg-red-200" data-id="\${p.promotionId}">삭제</button>
                </td>`;
                promotionListBody.appendChild(row);
            });
        }

        // --- Event Listeners (Top Panel: Flights) ---
        flightFilterSearchBtn.addEventListener('click', fetchFlights);
        addFlightBtn.addEventListener('click', function() {
            flightForm.reset();
            flightCrudIdInput.value = ''; // Clear ID for new flight creation
            flightModalTitle.textContent = '새 항공편 등록';
            openFlightCrudModal();
        });

        flightListBody.addEventListener('click', function(e) {
            // Make Promotion button
            if (e.target.classList.contains('make-promo-btn')) {
                promotionForm.reset(); // Reset promotion form
                // Ensure no promotionId for creation mode
                const promoIdHiddenInput = promotionForm.querySelector('input[name="promotionId"]');
                if(promoIdHiddenInput && promoIdHiddenInput.type === 'hidden') promoIdHiddenInput.remove(); // Remove if it was from edit mode

                // Recreate hidden input for flightId
                const flightIdHiddenInput = document.createElement('input');
                flightIdHiddenInput.type = 'hidden';
                flightIdHiddenInput.id = 'flightId';
                flightIdHiddenInput.name = 'flightId';
                promotionForm.prepend(flightIdHiddenInput);

                promotionFlightIdInput.value = e.target.dataset.flightId;
                modalFlightInfo.textContent = e.target.dataset.flightInfo;
                promotionModalTitle.textContent = '새 특가 상품 만들기';
                openPromotionModal();
            }
            // Edit Flight button
            else if (e.target.classList.contains('edit-flight-btn')) {
                const flightId = e.target.dataset.id;
                fetch(`\${pageContext.request.contextPath}/admin/api/flights/\${flightId}`)
                    .then(res => res.json())
                    .then(res => {
                        if (res.success) {
                            const f = res.data;
                            flightForm.reset();
                            flightCrudIdInput.value = f.flightId;
                            flightForm.querySelector('#flightNumber').value = f.flightNumber;
                            flightForm.querySelector('#departureAirport').value = f.departureAirport;
                            flightForm.querySelector('#arrivalAirport').value = f.arrivalAirport;
                            flightForm.querySelector('#departureTime').value = formatDateTimeForInput(f.departureTime);
                            flightForm.querySelector('#arrivalTime').value = formatDateTimeForInput(f.arrivalTime);
                            flightForm.querySelector('#terminalNo').value = f.terminalNo;
                            flightForm.querySelector('#routeType').value = f.routeType;
                            flightModalTitle.textContent = '항공편 수정';
                            openFlightCrudModal();
                        } else {
                            alert('항공편 정보를 불러오는 데 실패했습니다: ' + res.message);
                        }
                    })
                    .catch(err => console.error("Error fetching flight for edit:", err));
            }
            // Delete Flight button
            else if (e.target.classList.contains('delete-flight-btn')) {
                const flightId = e.target.dataset.id;
                if (confirm('정말 이 항공편을 삭제하시겠습니까? (연결된 특가가 있을 경우 삭제되지 않을 수 있습니다.)')) {
                    fetch(`\${pageContext.request.contextPath}/admin/api/flights/\${flightId}`, { method: 'DELETE' })
                        .then(res => res.json())
                        .then(result => {
                            if (result.success) {
                                alert('항공편이 삭제되었습니다.');
                                fetchFlights(); // Refresh flight list
                                fetchPromotions(); // Refresh promotions as a flight might be deleted
                            } else {
                                alert('항공편 삭제 실패: ' + (result.message || '알 수 없는 오류'));
                            }
                        })
                        .catch(err => console.error("Error deleting flight:", err));
                }
            }
        });

        // --- Event Listeners (Bottom Panel: Promotions) ---
        promotionListBody.addEventListener('click', function(e) {
            if (e.target.classList.contains('promo-delete-btn')) {
                const id = e.target.dataset.id;
                if (confirm('정말 이 특가 상품을 삭제하시겠습니까?')) {
                    fetch(`\${pageContext.request.contextPath}/admin/promotions/api/\${id}`, { method: 'DELETE' })
                        .then(res => res.json())
                        .then(result => {
                            if(result.success) {
                                alert('삭제되었습니다.');
                                fetchPromotions(); // Refresh promotion list
                            } else {
                                alert('오류: ' + (result.message || '알 수 없는 오류'));
                            }
                        });
                }
            }
        });

        // --- Form Submissions ---
        promotionForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(promotionForm);
            const data = Object.fromEntries(formData.entries());

            data.passengerCount = parseInt(data.passengerCount, 10);
            data.discountPercentage = parseInt(data.discountPercentage, 10);

            // Always POST for creation. No promotion edit functionality in this version.
            const url = '${pageContext.request.contextPath}/admin/promotions/api';
            const method = 'POST';

            fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            })
                .then(res => res.json())
                .then(result => {
                    if(result.success) {
                        alert('특가 상품이 생성되었습니다.');
                        closePromotionModal();
                        fetchPromotions(); // Refresh the promotion list
                    } else {
                        alert('오류: ' + (result.message || '알 수 없는 오류가 발생했습니다.'));
                    }
                })
                .catch(err => console.error("Error submitting promotion form:", err));
        });

        flightForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(flightForm);
            const data = Object.fromEntries(formData.entries());

            const flightId = data.flightId; // If flightId exists, it's an update
            const url = flightId
                ? `\${pageContext.request.contextPath}/admin/api/flights/\${flightId}`
                : '\${pageContext.request.contextPath}/admin/api/flights';
            const method = flightId ? 'PUT' : 'POST';

            fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            })
                .then(res => res.json())
                .then(result => {
                    if(result.success) {
                        alert('항공편이 성공적으로 저장되었습니다.');
                        closeFlightCrudModal();
                        fetchFlights(); // Refresh flight list
                        fetchPromotions(); // Refresh promotions as a flight might have been deleted/updated
                    } else {
                        alert('오류: ' + (result.message || '알 수 없는 오류가 발생했습니다.'));
                    }
                })
                .catch(err => console.error("Error submitting flight form:", err));
        });

        // --- Initial Load ---
        fetchFlights();
        fetchPromotions();
    });
</script>