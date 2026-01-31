<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="layout/head.jsp" %>
<%@ include file="layout/sidebar.jsp" %>
<%@ include file="layout/topbar.jsp" %>

<style>
    /* Add styles for the dropdown similar to search.jsp */
    .filter-dropdown .dropdown-panel { display: none; }
    .filter-dropdown.open .dropdown-panel { display: block; }

    /* 글래스 테마용 드롭다운 */
    .filter-dropdown .dropdown-panel {
        background: linear-gradient(135deg, rgba(30, 41, 59, 0.98) 0%, rgba(15, 23, 42, 0.99) 100%);
        border: 1px solid rgba(255, 255, 255, 0.1);
        backdrop-filter: blur(20px);
    }
    .filter-dropdown .dropdown-search {
        background: rgba(255, 255, 255, 0.05);
        border-color: rgba(255, 255, 255, 0.1);
        color: rgba(255, 255, 255, 0.9);
    }
    .filter-dropdown .dropdown-search::placeholder {
        color: rgba(255, 255, 255, 0.4);
    }
    .filter-dropdown .dropdown-list li {
        color: rgba(255, 255, 255, 0.8);
        padding: 0.5rem 1rem;
        cursor: pointer;
    }
    .filter-dropdown .dropdown-list li:hover {
        background: rgba(255, 255, 255, 0.1);
    }

    /* Drag and drop styles (글래스 테마) */
    .drag-handle { cursor: grab; }
    .drag-handle:active { cursor: grabbing; }
    .sortable-ghost { opacity: 0.4; background: rgba(59, 130, 246, 0.2); }
    .sortable-chosen { background: rgba(59, 130, 246, 0.1); }

    /* Skeleton shimmer effect (다크 테마) */
    @keyframes shimmer {
        0% { background-position: -200% 0; }
        100% { background-position: 200% 0; }
    }
    .skeleton-shimmer {
        background: linear-gradient(90deg, rgba(255,255,255,0.05) 25%, rgba(255,255,255,0.1) 50%, rgba(255,255,255,0.05) 75%);
        background-size: 200% 100%;
        animation: shimmer 1.5s infinite ease-in-out;
    }
</style>
<script src="https://cdn.jsdelivr.net/npm/sortablejs@1.15.0/Sortable.min.js"></script>

<main class="admin-bg pl-0 lg:pl-[72px] pt-16 min-h-screen transition-all duration-300 relative">
    <div class="p-8 max-w-[1600px] mx-auto space-y-8 relative z-10">
        <!-- Header -->
        <div class="flex items-center justify-between">
            <div>
                <h1 class="text-2xl font-bold text-glass-primary">항공편 및 특가 관리</h1>
                <p class="text-glass-muted">항공편을 조회, 생성/수정/삭제하고 특가 상품을 생성합니다.</p>
            </div>
            <button id="add-flight-btn" class="px-4 py-2 bg-emerald-500/80 text-white text-sm font-bold rounded-lg shadow-lg shadow-emerald-500/30 hover:bg-emerald-500 transition-all border border-emerald-400/30">
                <i data-lucide="plus" class="w-4 h-4 mr-2 inline-block"></i> 새 항공편 등록
            </button>
        </div>

        <!-- Top Panel: Flight Management -->
        <div class="glass-section">
            <div class="p-6 border-b border-white/5 flex items-center justify-between flex-wrap gap-4">
                <h2 class="text-lg font-bold text-glass-primary">항공편 목록</h2>
                <div id="flight-filters" class="flex items-center space-x-2">
                    <!-- Departure Airport -->
                    <div class="relative filter-dropdown" data-field="from">
                        <button type="button" class="dropdown-toggle p-2 bg-white/5 border border-white/10 rounded-lg text-sm w-44 text-left flex justify-between items-center text-glass-secondary hover:bg-white/10 transition-colors">
                            <span data-value>출발 공항</span> <i data-lucide="chevron-down" class="w-4 h-4"></i>
                        </button>
                        <div class="dropdown-panel absolute z-20 w-64 mt-1 rounded-lg shadow-xl">
                            <input class="dropdown-search w-full p-2 border-b border-white/10 rounded-t-lg" type="text" placeholder="공항 검색 (ICN, 인천)" autocomplete="off">
                            <ul class="dropdown-list max-h-60 overflow-y-auto rounded-b-lg" data-list></ul>
                        </div>
                    </div>
                    <!-- Arrival Airport -->
                    <div class="relative filter-dropdown" data-field="to">
                        <button type="button" class="dropdown-toggle p-2 bg-white/5 border border-white/10 rounded-lg text-sm w-44 text-left flex justify-between items-center text-glass-secondary hover:bg-white/10 transition-colors">
                            <span data-value>도착 공항</span> <i data-lucide="chevron-down" class="w-4 h-4"></i>
                        </button>
                        <div class="dropdown-panel absolute z-20 w-64 mt-1 rounded-lg shadow-xl">
                            <input class="dropdown-search w-full p-2 border-b border-white/10 rounded-t-lg" type="text" placeholder="공항 검색 (NRT, 나리타)" autocomplete="off">
                            <ul class="dropdown-list max-h-60 overflow-y-auto rounded-b-lg" data-list></ul>
                        </div>
                    </div>
                    <button id="search-flights-btn" class="glass-btn px-3 py-2 text-white text-sm">
                        <i data-lucide="search" class="w-4 h-4 inline-block mr-1"></i> 검색
                    </button>
                </div>
            </div>
            <div class="p-6">
                <div class="overflow-x-auto">
                    <table class="min-w-full divide-y divide-white/5">
                        <thead class="bg-white/5">
                        <tr>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase">항공편 번호</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase">경로</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase">출발 시각</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase">관리</th>
                        </tr>
                        </thead>
                        <tbody id="flight-list-body" class="divide-y divide-white/5"></tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Bottom Panel: Promotion Management -->
        <div class="glass-section">
            <div class="p-6 border-b border-white/5 flex items-center justify-between">
                <div>
                    <h2 class="text-lg font-bold text-glass-primary">생성된 특가 상품 목록</h2>
                    <p class="text-sm text-glass-muted mt-1">드래그하여 메인페이지 표시 순서를 변경하세요. 상위 항목이 먼저 표시됩니다.</p>
                </div>
            </div>
            <div class="p-6">
                <div class="overflow-x-auto">
                    <table class="min-w-full divide-y divide-white/5">
                        <thead class="bg-white/5">
                        <tr>
                            <th class="px-2 py-3 text-center text-xs font-semibold text-glass-muted uppercase w-12">순서</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase">제목</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase">항공편</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase">인원</th>
                            <th class="px-4 py-3 text-left text-xs font-semibold text-glass-muted uppercase">할인가(총)</th>
                            <th class="px-4 py-3 text-center text-xs font-semibold text-glass-muted uppercase">메인 노출</th>
                            <th class="px-4 py-3 text-center text-xs font-semibold text-glass-muted uppercase w-16">삭제</th>
                        </tr>
                        </thead>
                        <tbody id="promotion-list-body" class="divide-y divide-white/5"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</main>

<!-- Modals -->
<%@ include file="include/promotion_modal.jsp" %>
<%@ include file="include/flight_crud_modal.jsp" %>

<script>
    window.CONTEXT_PATH = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/resources/admin/promotions.js"></script>
</body>
</html>
