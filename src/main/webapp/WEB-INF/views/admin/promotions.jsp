<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="layout/head.jsp" %>
<%@ include file="layout/sidebar.jsp" %>
<%@ include file="layout/topbar.jsp" %>

<main class="pl-64 pt-16 min-h-screen">
    <div class="p-8 max-w-[1600px] mx-auto space-y-8">
        <!-- Header -->
        <div class="flex items-center justify-between">
            <div>
                <h1 class="text-2xl font-bold text-slate-900">특가 항공권 관리</h1>
                <p class="text-slate-500">메인페이지에 노출될 특가 항공권 상품을 관리합니다.</p>
            </div>
            <button id="add-promotion-btn" class="px-4 py-2 bg-blue-600 text-white text-sm font-bold rounded-lg shadow-lg shadow-blue-500/20 hover:bg-blue-700 transition-colors">
                <i data-lucide="plus" class="w-4 h-4 mr-2 inline-block"></i> 새 특가 상품 추가
            </button>
        </div>

        <!-- Promotions Table -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm">
            <div class="p-6 border-b border-slate-100 flex items-center justify-between">
                <h2 class="text-lg font-bold text-slate-800">특가 목록</h2>
                <!-- Search/Filter UI can be added here -->
            </div>
            <div class="p-6">
                <div class="overflow-x-auto">
                    <table class="min-w-full divide-y divide-slate-200">
                        <thead class="bg-slate-50">
                        <tr>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">제목</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">항공편 ID</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">인원</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">할인율</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">상태</th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase">관리</th>
                        </tr>
                        </thead>
                        <tbody id="promotion-list-body" class="bg-white divide-y divide-slate-100">
                        <!-- Data will be rendered here by JavaScript -->
                        </tbody>
                    </table>
                </div>
                <!-- Pagination can be added here -->
            </div>
        </div>
    </div>
</main>

<!-- Modal for Add/Edit Promotion -->
<div id="promotion-modal" class="fixed inset-0 z-50 hidden bg-black/50 backdrop-blur-sm">
    <div class="fixed inset-0 flex items-center justify-center p-4">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg">
            <form id="promotion-form">
                <input type="hidden" id="promotion-id" name="promotionId">
                <div class="p-6 border-b">
                    <h2 id="modal-title" class="text-lg font-bold">새 특가 상품 추가</h2>
                </div>
                <div class="p-6 space-y-4">
                    <div>
                        <label for="title" class="block text-sm font-medium text-slate-700">제목</label>
                        <input type="text" id="title" name="title" required class="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
                    </div>
                    <div class="relative">
                        <label for="flight-search" class="block text-sm font-medium text-slate-700">항공편 검색</label>
                        <input type="text" id="flight-search" autocomplete="off" placeholder="항공편 번호로 검색..." class="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
                        <input type="hidden" id="flightId" name="flightId" required>
                        <div id="flight-results" class="absolute z-10 w-full mt-1 bg-white border border-slate-300 rounded-md shadow-lg hidden max-h-60 overflow-y-auto">
                            <!-- Search results will be populated here -->
                        </div>
                    </div>
                    <div>
                        <label for="passengerCount" class="block text-sm font-medium text-slate-700">인원수</label>
                        <input type="number" id="passengerCount" name="passengerCount" required class="mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" min="1">
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

<script>
    document.addEventListener('DOMContentLoaded', function() {
        lucide.createIcons();

        const addBtn = document.getElementById('add-promotion-btn');
        const modal = document.getElementById('promotion-modal');
        const closeModalBtn = document.getElementById('modal-close-btn');
        const form = document.getElementById('promotion-form');
        const modalTitle = document.getElementById('modal-title');
        const promotionListBody = document.getElementById('promotion-list-body');

        // Flight Search elements
        const flightSearchInput = document.getElementById('flight-search');
        const flightResultsContainer = document.getElementById('flight-results');
        const flightIdInput = document.getElementById('flightId');
        let debounceTimer;

        const openModal = () => modal.classList.remove('hidden');
        const closeModal = () => {
            modal.classList.add('hidden');
            flightResultsContainer.classList.add('hidden');
            flightSearchInput.value = '';
        };

        addBtn.addEventListener('click', () => {
            form.reset();
            flightIdInput.value = '';
            flightSearchInput.value = '';
            document.getElementById('promotion-id').value = '';
            modalTitle.textContent = '새 특가 상품 추가';
            openModal();
        });

        closeModalBtn.addEventListener('click', closeModal);

        // --- Flight Search Logic ---
        flightSearchInput.addEventListener('input', (e) => {
            const query = e.target.value;
            flightIdInput.value = ''; // Clear hidden flightId if user types again
            flightResultsContainer.classList.add('hidden');

            clearTimeout(debounceTimer);
            if (query.length < 2) {
                return;
            }

            debounceTimer = setTimeout(() => {
                fetch(`\${pageContext.request.contextPath}/api/flights?flightNumber=\${query}`)
                    .then(res => res.json())
                    .then(flights => {
                        flightResultsContainer.innerHTML = '';
                        if (flights && flights.length > 0) {
                            flights.forEach(flight => {
                                const item = document.createElement('div');
                                item.className = 'p-2 hover:bg-blue-50 cursor-pointer text-sm';
                                item.textContent = `\${flight.flightNumber} (\${flight.departureAirport} → \${flight.arrivalAirport})`;
                                item.dataset.id = flight.flightId;
                                item.dataset.display = item.textContent;
                                flightResultsContainer.appendChild(item);
                            });
                            flightResultsContainer.classList.remove('hidden');
                        } else {
                            const noResult = document.createElement('div');
                            noResult.className = 'p-2 text-sm text-slate-500';
                            noResult.textContent = '검색 결과가 없습니다.';
                            flightResultsContainer.appendChild(noResult);
                            flightResultsContainer.classList.remove('hidden');
                        }
                    });
            }, 300); // 300ms debounce
        });

        flightResultsContainer.addEventListener('click', (e) => {
            if (e.target.dataset.id) {
                flightIdInput.value = e.target.dataset.id;
                flightSearchInput.value = e.target.dataset.display;
                flightResultsContainer.classList.add('hidden');
            }
        });
        // --- End of Flight Search Logic ---

        function fetchPromotions() {
            // Fetch and render logic will go here
            // This is a simplified version, full implementation would require pagination, search, etc.
            fetch('${pageContext.request.contextPath}/admin/promotions/api/list')
                .then(res => res.json())
                .then(data => {
                    if(data.success) {
                        renderTable(data.data.list);
                    }
                });
        }

        function renderTable(promotions) {
            promotionListBody.innerHTML = '';
            if (!promotions || promotions.length === 0) {
                promotionListBody.innerHTML = '<tr><td colspan="6" class="text-center py-10 text-slate-500">특가 상품이 없습니다.</td></tr>';
                return;
            }

            promotions.forEach(p => {
                const row = document.createElement('tr');
                row.innerHTML = `
                <td class="px-4 py-3 text-sm font-medium text-slate-900">\${p.title}</td>
                <td class="px-4 py-3 text-sm text-slate-500 font-mono">\${p.flightId}</td>
                <td class="px-4 py-3 text-sm text-slate-500">\${p.passengerCount}명</td>
                <td class="px-4 py-3 text-sm text-slate-500">\${p.discountPercentage}%</td>
                <td class="px-4 py-3 text-sm">
                    <span class="px-2 py-1 text-xs font-medium rounded-full \${p.isActive === 'Y' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
                        \${p.isActive === 'Y' ? '활성' : '비활성'}
                    </span>
                </td>
                <td class="px-4 py-3 text-sm font-medium space-x-2">
                    <button class="text-blue-600 hover:text-blue-800 edit-btn" data-id="\${p.promotionId}">수정</button>
                    <button class="text-red-600 hover:text-red-800 delete-btn" data-id="\${p.promotionId}">삭제</button>
                    <button class="text-gray-600 hover:text-gray-800 toggle-btn" data-id="\${p.promotionId}">상태변경</button>
                </td>
            `;
                promotionListBody.appendChild(row);
            });
            lucide.createIcons();
        }

        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(form);
            const data = Object.fromEntries(formData.entries());
            const id = data.promotionId;

            const url = id
                ? `\${pageContext.request.contextPath}/admin/promotions/api/\${id}`
                : '\${pageContext.request.contextPath}/admin/promotions/api';

            const method = id ? 'PUT' : 'POST';

            fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            })
                .then(res => res.json())
                .then(result => {
                    if(result.success) {
                        alert(result.message);
                        closeModal();
                        fetchPromotions();
                    } else {
                        alert('오류: ' + result.message);
                    }
                });
        });

        promotionListBody.addEventListener('click', function(e) {
            const id = e.target.dataset.id;
            if (!id) return;

            if (e.target.classList.contains('edit-btn')) {
                // Fetch promotion details and populate form
                fetch(`\${pageContext.request.contextPath}/admin/promotions/api/\${id}`)
                    .then(res => res.json())
                    .then(data => {
                        if (data.success) {
                            const p = data.data;
                            document.getElementById('promotion-id').value = p.promotionId;
                            document.getElementById('title').value = p.title;

                            // For editing, populate the search box and hidden ID
                            flightIdInput.value = p.flightId;
                            flightSearchInput.value = `\${p.flightNumber || p.flightId} (\${p.departureAirportName} → \${p.arrivalAirportName})`;

                            document.getElementById('passengerCount').value = p.passengerCount;
                            document.getElementById('discountPercentage').value = p.discountPercentage;
                            document.getElementById('tags').value = p.tags;
                            modalTitle.textContent = '특가 상품 수정';
                            openModal();
                        }
                    });
            } else if (e.target.classList.contains('delete-btn')) {
                if (confirm('정말 이 특가 상품을 삭제하시겠습니까?')) {
                    fetch(`\${pageContext.request.contextPath}/admin/promotions/api/\${id}`, { method: 'DELETE' })
                        .then(res => res.json())
                        .then(result => {
                            alert(result.message);
                            fetchPromotions();
                        });
                }
            } else if (e.target.classList.contains('toggle-btn')) {
                fetch(`\${pageContext.request.contextPath}/admin/promotions/api/\${id}/toggle`, { method: 'POST' })
                    .then(res => res.json())
                    .then(result => {
                        if (result.success) {
                            fetchPromotions();
                        } else {
                            alert('상태 변경 실패: ' + result.message);
                        }
                    });
            }
        });

        // Initial load
        fetchPromotions();
    });
</script>
</body>
</html>