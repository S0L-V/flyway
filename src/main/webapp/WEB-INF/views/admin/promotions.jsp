<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="layout/head.jsp" %>
<%@ include file="layout/sidebar.jsp" %>
<%@ include file="layout/topbar.jsp" %>

<style>
    /* 항공편 섹션이 특가 목록 위에 오도록 */
    #flight-section {
        position: relative;
        z-index: 200;
    }

    /* 필터 영역 */
    #flight-filters {
        position: relative;
        z-index: 9999;
    }

    /* 특가 목록 섹션은 낮은 z-index */
    #promotion-section {
        position: relative;
        z-index: 1;
    }

    /* 2단계 드롭다운 스타일 */
    .filter-dropdown .dropdown-panel { display: none; }
    .filter-dropdown.open .dropdown-panel { display: flex; }

    /* 글래스 테마용 드롭다운 */
    .filter-dropdown .dropdown-panel {
        background: linear-gradient(135deg, rgba(30, 41, 59, 0.98) 0%, rgba(15, 23, 42, 0.99) 100%);
        border: 1px solid rgba(255, 255, 255, 0.1);
        backdrop-filter: blur(20px);
        border-radius: 0.75rem;
        overflow: hidden;
    }

    /* 2단계 드롭다운 레이아웃 */
    .dropdown-panel {
        flex-direction: row;
        width: 420px;
    }

    /* 국가 패널 */
    .country-panel {
        width: 160px;
        border-right: 1px solid rgba(255, 255, 255, 0.08);
        display: flex;
        flex-direction: column;
    }

    .country-panel-header {
        padding: 0.75rem 1rem;
        font-size: 0.75rem;
        font-weight: 600;
        color: rgba(255, 255, 255, 0.5);
        text-transform: uppercase;
        letter-spacing: 0.05em;
        border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    }

    .country-list {
        flex: 1;
        overflow-y: auto;
        max-height: 280px;
    }

    .country-item {
        padding: 0.625rem 1rem;
        font-size: 0.875rem;
        color: rgba(255, 255, 255, 0.75);
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: space-between;
        transition: all 0.15s ease;
    }

    .country-item:hover {
        background: rgba(255, 255, 255, 0.08);
        color: rgba(255, 255, 255, 0.95);
    }

    .country-item.active {
        background: rgba(10, 132, 255, 0.2);
        color: #0a84ff;
    }

    .country-item .count {
        font-size: 0.75rem;
        color: rgba(255, 255, 255, 0.4);
        background: rgba(255, 255, 255, 0.1);
        padding: 0.125rem 0.5rem;
        border-radius: 999px;
    }

    .country-item.active .count {
        background: rgba(10, 132, 255, 0.3);
        color: rgba(10, 132, 255, 0.8);
    }

    /* 도시/공항 패널 */
    .airport-panel {
        flex: 1;
        display: flex;
        flex-direction: column;
    }

    .airport-panel-header {
        padding: 0.5rem 0.75rem;
        border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    }

    .airport-search {
        width: 100%;
        padding: 0.5rem 0.75rem;
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 0.5rem;
        color: rgba(255, 255, 255, 0.9);
        font-size: 0.875rem;
        outline: none;
        transition: all 0.2s ease;
    }

    .airport-search:focus {
        background: rgba(255, 255, 255, 0.08);
        border-color: rgba(10, 132, 255, 0.5);
    }

    .airport-search::placeholder {
        color: rgba(255, 255, 255, 0.4);
    }

    .airport-list {
        flex: 1;
        overflow-y: auto;
        max-height: 240px;
    }

    .airport-item {
        padding: 0.625rem 1rem;
        cursor: pointer;
        transition: all 0.15s ease;
        border-bottom: 1px solid rgba(255, 255, 255, 0.03);
    }

    .airport-item:hover {
        background: rgba(255, 255, 255, 0.08);
    }

    .airport-item:last-child {
        border-bottom: none;
    }

    .airport-item .city-name {
        font-size: 0.875rem;
        font-weight: 500;
        color: rgba(255, 255, 255, 0.9);
    }

    .airport-item .airport-code {
        font-size: 0.75rem;
        color: rgba(255, 255, 255, 0.5);
        margin-left: 0.5rem;
    }

    .airport-item .airport-name {
        font-size: 0.75rem;
        color: rgba(255, 255, 255, 0.4);
        margin-top: 0.125rem;
    }

    /* 선택 안내 */
    .select-country-hint {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 200px;
        color: rgba(255, 255, 255, 0.4);
        font-size: 0.875rem;
        text-align: center;
        padding: 1rem;
    }

    .select-country-hint i {
        width: 2.5rem;
        height: 2.5rem;
        margin-bottom: 0.75rem;
        opacity: 0.5;
    }

    /* 빈 결과 */
    .no-results {
        padding: 1.5rem;
        text-align: center;
        color: rgba(255, 255, 255, 0.4);
        font-size: 0.875rem;
    }

    /* 커스텀 스크롤바 (드롭다운 내부) */
    .country-list::-webkit-scrollbar,
    .airport-list::-webkit-scrollbar {
        width: 4px;
    }

    .country-list::-webkit-scrollbar-thumb,
    .airport-list::-webkit-scrollbar-thumb {
        background: rgba(255, 255, 255, 0.2);
        border-radius: 2px;
    }

    body.admin-light .country-list::-webkit-scrollbar-thumb,
    body.admin-light .airport-list::-webkit-scrollbar-thumb {
        background: rgba(0, 0, 0, 0.15);
    }

    /* 드롭다운 열기 애니메이션 */
    .filter-dropdown .dropdown-panel {
        opacity: 0;
        transform: translateY(-8px);
        transition: opacity 0.2s ease, transform 0.2s ease;
        pointer-events: none;
        z-index: 99999 !important;
    }

    .filter-dropdown.open .dropdown-panel {
        opacity: 1;
        transform: translateY(0);
        pointer-events: auto;
    }

    /* 선택된 값 표시 스타일 */
    .dropdown-toggle [data-value].selected {
        color: #0a84ff;
        font-weight: 500;
    }

    body.admin-light .dropdown-toggle [data-value].selected {
        color: #0a84ff;
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

    /* Skeleton shimmer effect (라이트 테마) */
    body.admin-light .skeleton-shimmer {
        background: linear-gradient(90deg, #e2e8f0 25%, #f1f5f9 50%, #e2e8f0 75%);
        background-size: 200% 100%;
    }

    /* 라이트 모드 드롭다운 */
    body.admin-light .filter-dropdown .dropdown-panel {
        background: rgba(255, 255, 255, 0.98);
        border-color: rgba(0, 0, 0, 0.1);
    }

    body.admin-light .country-panel {
        border-right-color: rgba(0, 0, 0, 0.08);
    }

    body.admin-light .country-panel-header {
        color: rgba(15, 23, 42, 0.5);
        border-bottom-color: rgba(0, 0, 0, 0.05);
    }

    body.admin-light .country-item {
        color: rgba(15, 23, 42, 0.75);
    }

    body.admin-light .country-item:hover {
        background: rgba(0, 0, 0, 0.05);
        color: rgba(15, 23, 42, 0.95);
    }

    body.admin-light .country-item.active {
        background: rgba(10, 132, 255, 0.1);
        color: #0a84ff;
    }

    body.admin-light .country-item .count {
        color: rgba(15, 23, 42, 0.4);
        background: rgba(0, 0, 0, 0.08);
    }

    body.admin-light .airport-panel-header {
        border-bottom-color: rgba(0, 0, 0, 0.05);
    }

    body.admin-light .airport-search {
        background: rgba(0, 0, 0, 0.05);
        border-color: rgba(0, 0, 0, 0.1);
        color: rgba(15, 23, 42, 0.9);
    }

    body.admin-light .airport-search:focus {
        background: rgba(0, 0, 0, 0.08);
    }

    body.admin-light .airport-search::placeholder {
        color: rgba(15, 23, 42, 0.4);
    }

    body.admin-light .airport-item {
        border-bottom-color: rgba(0, 0, 0, 0.05);
    }

    body.admin-light .airport-item:hover {
        background: rgba(0, 0, 0, 0.05);
    }

    body.admin-light .airport-item .city-name {
        color: rgba(15, 23, 42, 0.9);
    }

    body.admin-light .airport-item .airport-code {
        color: rgba(15, 23, 42, 0.5);
    }

    body.admin-light .airport-item .airport-name {
        color: rgba(15, 23, 42, 0.4);
    }

    body.admin-light .select-country-hint,
    body.admin-light .no-results {
        color: rgba(15, 23, 42, 0.4);
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
        <div id="flight-section" class="glass-section">
            <div class="p-6 border-b border-white/5 flex items-center justify-between flex-wrap gap-4">
                <h2 class="text-lg font-bold text-glass-primary">항공편 목록</h2>
                <div id="flight-filters" class="flex items-center space-x-2 flex-wrap gap-y-2">
                    <!-- Departure Airport (2단계 선택) -->
                    <div class="filter-dropdown" data-field="from" style="position: relative; z-index: 9999;">
                        <button type="button" class="dropdown-toggle p-2 bg-white/5 border border-white/10 rounded-lg text-sm w-48 text-left flex justify-between items-center text-glass-secondary hover:bg-white/10 transition-colors">
                            <span class="flex items-center gap-2">
                                <i data-lucide="plane-takeoff" class="w-4 h-4 opacity-60"></i>
                                <span data-value>출발지 선택</span>
                            </span>
                            <i data-lucide="chevron-down" class="w-4 h-4"></i>
                        </button>
                        <div class="dropdown-panel shadow-xl" style="position: absolute; top: 100%; left: 0; margin-top: 4px;">
                            <!-- 국가 패널 -->
                            <div class="country-panel">
                                <div class="country-panel-header">국가 선택</div>
                                <div class="country-list" data-country-list></div>
                            </div>
                            <!-- 도시/공항 패널 -->
                            <div class="airport-panel">
                                <div class="airport-panel-header">
                                    <input class="airport-search" type="text" placeholder="도시 또는 공항 검색..." autocomplete="off">
                                </div>
                                <div class="airport-list" data-airport-list>
                                    <div class="select-country-hint">
                                        <i data-lucide="map-pin"></i>
                                        <span>좌측에서 국가를<br>먼저 선택해주세요</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- Arrival Airport (2단계 선택) -->
                    <div class="filter-dropdown" data-field="to" style="position: relative; z-index: 9999;">
                        <button type="button" class="dropdown-toggle p-2 bg-white/5 border border-white/10 rounded-lg text-sm w-48 text-left flex justify-between items-center text-glass-secondary hover:bg-white/10 transition-colors">
                            <span class="flex items-center gap-2">
                                <i data-lucide="plane-landing" class="w-4 h-4 opacity-60"></i>
                                <span data-value>도착지 선택</span>
                            </span>
                            <i data-lucide="chevron-down" class="w-4 h-4"></i>
                        </button>
                        <div class="dropdown-panel shadow-xl" style="position: absolute; top: 100%; right: 0; margin-top: 4px;">
                            <!-- 국가 패널 -->
                            <div class="country-panel">
                                <div class="country-panel-header">국가 선택</div>
                                <div class="country-list" data-country-list></div>
                            </div>
                            <!-- 도시/공항 패널 -->
                            <div class="airport-panel">
                                <div class="airport-panel-header">
                                    <input class="airport-search" type="text" placeholder="도시 또는 공항 검색..." autocomplete="off">
                                </div>
                                <div class="airport-list" data-airport-list>
                                    <div class="select-country-hint">
                                        <i data-lucide="map-pin"></i>
                                        <span>좌측에서 국가를<br>먼저 선택해주세요</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <button id="search-flights-btn" class="glass-btn px-3 py-2 text-white text-sm">
                        <i data-lucide="search" class="w-4 h-4 inline-block mr-1"></i> 검색
                    </button>
                    <button id="reset-filters-btn" class="px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-sm text-glass-muted hover:text-glass-secondary hover:bg-white/10 transition-colors" title="필터 초기화">
                        <i data-lucide="rotate-ccw" class="w-4 h-4"></i>
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
        <div id="promotion-section" class="glass-section">
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
<script src="${pageContext.request.contextPath}/resources/admin/promotions.js?v=<%= System.currentTimeMillis() %>"></script>
</body>
</html>
